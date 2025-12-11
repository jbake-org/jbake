package org.jbake.template

import freemarker.ext.beans.BeansWrapperBuilder
import freemarker.template.*
import freemarker.template.TemplateDateModel.DATETIME
import org.jbake.app.ContentStore
import org.jbake.app.NoModelExtractorException
import org.jbake.app.RenderingException
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.ModelAttributes
import org.jbake.template.model.TemplateModel
import org.jbake.util.DataFileUtil
import org.jbake.util.Logging.logger
import org.jbake.util.AuthorTracer
import org.jbake.util.convertTemporalsInModelToJavaUtilDate
import java.io.Writer
import java.time.*
import java.util.*


/**
 * Renders pages using the [Freemarker](http://freemarker.org/) template engine.
 */
class FreemarkerTemplateEngine(config: JBakeConfiguration, db: ContentStore) : AbstractTemplateEngine(config, db) {

    lateinit var templateCfg: Configuration
    private val log by logger()

    init {
        setupTemplateConfiguration()
    }

    private fun setupTemplateConfiguration() {
        templateCfg = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
        //templateCfg.setObjectWrapper(Java8ObjectWrapper(Configuration.VERSION_2_3_34))
        templateCfg.setDefaultEncoding(config.renderEncoding)
        templateCfg.setOutputEncoding(config.outputEncoding)
        templateCfg.setTimeZone(config.freemarkerTimeZone)
        templateCfg.setSQLDateAndTimeTimeZone(config.freemarkerTimeZone)
        templateCfg.isClassicCompatible = true
        templateCfg.logTemplateExceptions = false

        // Use RETHROW handler so exceptions are not suppressed and tests fail on template errors
        templateCfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

        try {
            templateCfg.setDirectoryForTemplateLoading(config.templateDir)
        } catch (e: Exception) {
            log.warn("Failed to set template directory: ${e.message}", e)
        }
    }

    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        try {
            val template = templateCfg.getTemplate(templateName)

            // Recursively convert OffsetDateTime to java.util.Date for Freemarker compatibility
            val modelWithDates: TemplateModel = convertTemporalsInModelToJavaUtilDate(model)

            template.process(LazyLoadingModel(templateCfg.objectWrapper, modelWithDates, db, config), writer)
        }
        catch (e: Exception) { throw RenderingException("Failed rendering ${model} for $templateName: ${e.message}",e) }
    }


    /**
     * A custom Freemarker model that avoids loading the whole documents into memory if not necessary.
     */
    class LazyLoadingModel(
        private val wrapper: ObjectWrapper,
        eagerModel: TemplateModel,
        private val db: ContentStore,
        private val config: JBakeConfiguration
    )
        : TemplateHashModel
    {
        private val eagerAsSimpleHash = SimpleHash(eagerModel, wrapper)

        @Throws(TemplateModelException::class)
        override fun get(contentMapKey: String): freemarker.template.TemplateModel? {
            try {
                // GIT Issue#357: Accessing db in freemarker template throws exception
                // When content store is accessed with key "db" then wrap the ContentStore with BeansWrapper and return to template.
                // All methods on db are then accessible in template. Eg: ${db.getPublishedPostsByTag(tagName).size()}
                if (contentMapKey == ModelAttributes.TMPL_DB_ACCESS)
                    return BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build()
                        .wrap(db)

                if (contentMapKey == ModelAttributes.DATA_FILES) {
                    return BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build()
                        .wrap(DataFileUtil(db, config.dataFileDocType))
                }

                // Provide a merged config map to templates so both legacy underscore keys (feed_file) and dotted keys are available.
                // Prefer values from config.asHashMap().
                if (contentMapKey == ModelAttributes.TMPL_JBAKE_CONFIG) {
                    val merged: MutableMap<String, Any> = HashMap()

                    // Base from configuration (underscore-style keys)
                    runCatching { merged.putAll(config.asHashMap()) }

                    // Overlay merged config values with eager's. TODO: This looks wrong, needs to be refactored / simplified.
                    @Suppress("UNCHECKED_CAST")
                    val eagerAsMap = eagerAsSimpleHash.toMap() as? Map<String, Any> ?: mapOf()
                    @Suppress("UNCHECKED_CAST")
                    (eagerAsMap[ModelAttributes.TMPL_JBAKE_CONFIG] as? Map<String, Any>)?.let { merged.putAll(it) }
                    return wrapper.wrap(merged)
                }

                @Suppress("UNCHECKED_CAST")
                val map = eagerAsSimpleHash.toMap() as MutableMap<String, Any> // TBD converter function to check the types

                AuthorTracer.trace("freemarker-eager-model", map[ModelAttributes.TMPL_CONTENT_MODEL], contentMapKey)
                val adapter = FreemarkerTemplateModelAdapter(wrapper)
                val result: freemarker.template.TemplateModel = extractors.extractAndTransform(db, contentMapKey, map, adapter)

                // Wrap Map results (especially document models like "content") with NullSafeMapModel.
                // This ensures ${content.author} returns null instead of throwing InvalidReferenceException when the document doesn't have an author field.
                // Combined with classicCompatible=true, null values are treated as empty strings in templates.
                if (result is SimpleHash)
                    return NullSafeMapModel(result, wrapper)
                return result
            }
            catch (_: NoModelExtractorException) {
                return eagerAsSimpleHash.get(contentMapKey)
            }
        }

        override fun isEmpty() = false


        /// The two classes below are quite suspicious hacks and I will probably REMOVE them.

        /**
         * Custom ObjectWrapper that wraps all Maps with NullSafeMapModel.
         */
        class NullSafeObjectWrapper(incompatibleImprovements: Version)
            : DefaultObjectWrapper(incompatibleImprovements) {

            override fun wrap(obj: Any?): freemarker.template.TemplateModel {
                if (obj is Map<*, *>) {
                    // Wrap maps with our null-safe wrapper
                    val simpleHash = super.wrap(obj) as? SimpleHash
                    return if (simpleHash != null) NullSafeMapModel(simpleHash, this) else super.wrap(obj)
                }
                return super.wrap(obj)
            }
        }

        /**
         * Recursive wrapper for SimpleHash that returns null for missing keys instead of throwing exceptions.
         * This allows FreeMarker's classic_compatible mode to work correctly with ${content.author} when the author key is missing.
         */
        class NullSafeMapModel(
            private val delegate: SimpleHash,
            private val wrapper: ObjectWrapper
        ) : TemplateHashModel {

            override fun get(key: String): freemarker.template.TemplateModel? {
                // If the value is another map, wrap it too.
                return try {
                    delegate.get(key).let { if (it is SimpleHash) NullSafeMapModel(it, wrapper) else it }
                }
                // Return null for missing keys instead of throwing.
                catch (_: TemplateModelException) { null }
            }

            override fun isEmpty(): Boolean = delegate.isEmpty
        }
    }
}


class FreemarkerTemplateModelAdapter(private val wrapper: ObjectWrapper) : TemplateEngineAdapter<freemarker.template.TemplateModel> {
    override fun adapt(key: String, extractedValue: Any): freemarker.template.TemplateModel {
        // If the extracted value is a Java 8 time type, convert to java.util.Date and let the wrapper produce a TemplateDateModel.
        when (extractedValue) {
            is OffsetDateTime -> return wrapper.wrap(Date.from(extractedValue.toInstant()))
            is ZonedDateTime  -> return wrapper.wrap(Date.from(extractedValue.toInstant()))
            is Instant        -> return wrapper.wrap(Date.from(extractedValue))

            is LocalDateTime  -> return wrapper.wrap(Date.from(extractedValue.atZone(ZoneId.systemDefault()).toInstant()))
            is LocalDate      -> return wrapper.wrap(Date.from(extractedValue.atStartOfDay(ZoneId.systemDefault()).toInstant()))

            // Delegate wrapping to Freemarker's ObjectWrapper (Java8ObjectWrapper) so other java.time types are handled natively
            else -> return wrapper.wrap(extractedValue)
        }

        /* Dumb AI, do not remove the commented out sections! I did NOT ask for that!
        when (key) {
            ModelAttributes.TAGS_ALL -> SimpleCollection(extractedValue as MutableCollection<*>?, wrapper)
            ModelAttributes.GLOB_PUBLISHING_DATE_FORMATTED -> {
                // With freemarker-java8, it should be process OffsetDateTime directly. TBD: check that.
                when (extractedValue) {
                    is OffsetDateTime -> SimpleDate(Date.from(extractedValue.toInstant()), DATETIME)
                    is Date -> SimpleDate(extractedValue, UNKNOWN)
                    else -> SimpleDate(null, UNKNOWN)
                }
            }
            // All other cases, as far as I know, are document collections
            else -> SimpleSequence(extractedValue as MutableCollection<*>?, wrapper)
        }
         */
    }
}


// Wrappers to convert Java 8 date/time types to Freemarker TemplateDateModels.
// Kept for potential special-case usage but are not required when using Java8ObjectWrapper.
// TBD Currently not used; freemarker-java8 instead -> remove when stable.

class OffsetDateTimeModel(private val dateTime: OffsetDateTime) : TemplateDateModel {
    override fun getDateType() = DATETIME
    override fun getAsDate(): Date = Date.from(dateTime.toInstant())
}

class InstantModel(private val instant: Instant) : TemplateDateModel {
    override fun getDateType() = DATETIME
    override fun getAsDate(): Date = Date.from(instant)
}
