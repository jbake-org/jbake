package org.jbake.template

import freemarker.ext.beans.BeansWrapperBuilder
import freemarker.template.*
import org.jbake.app.ContentStore
import org.jbake.app.NoModelExtractorException
import org.jbake.app.RenderingException
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.ModelAttributes
import org.jbake.template.model.JbakeTemplateModel
import org.jbake.util.DataFileUtil
import org.jbake.util.Logging.logger
import org.jbake.util.ValueTracer
import org.jbake.util.convertTemporalsInModelToJavaUtilDate
import org.jbake.util.debug
import java.io.Writer
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*


/**
 * Renders pages using the [Freemarker](http://freemarker.org/) template engine.
 */
class FreemarkerTemplateEngine(config: JBakeConfiguration, db: ContentStore) : AbstractTemplateEngine(config, db) {

    lateinit var templateCfg: FmConfiguration
    private val log by logger()

    init {
        setupTemplateConfiguration()
    }

    private fun setupTemplateConfiguration() {
        templateCfg = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
        //templateCfg.setObjectWrapper(Java8ObjectWrapper(Configuration.VERSION_2_3_34))
        templateCfg.setObjectWrapper(TemporalsObjectWrapper(Configuration.VERSION_2_3_34))
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
    override fun renderDocument(model: JbakeTemplateModel, templateName: String, writer: Writer) {
        try {
            val template = templateCfg.getTemplate(templateName)

            val zoneToUse: ZoneId = config.freemarkerTimeZone.toZoneId() ?: ZoneId.systemDefault()
            val modelWithDates: JbakeTemplateModel = convertTemporalsInModelToJavaUtilDate(model, zoneToUse)

            log.debug {
                "Freemarking template $templateName; model: ${modelWithDates.entries.filter { it.key != "config" }.joinToString { "\n * $it" }}" +
                    "\n  Config: " + modelWithDates.config.toSortedMap().entries.filter { it.key !in setOf("java.class.path", "java_class_path") }.joinToString { "\n\t* $it" }
            }
            template.process(LazyLoadingModel(templateCfg.objectWrapper, modelWithDates, db, config), writer)
        }
        catch (e: Exception) { throw RenderingException("Failed rendering ${model} for $templateName: ${e.message}",e) }
    }


    /**
     * A custom Freemarker model that avoids loading the whole documents into memory if not necessary.
     *
     * This is an ugly class mixing several concerns into a single hard-to-reason-about blob. Should be split per concerns.
     */
    class LazyLoadingModel(
        private val freeMarkerWrapper: ObjectWrapper,
        private val jbakeTemplateModel: JbakeTemplateModel,
        private val db: ContentStore,
        private val jbakeConfig: JBakeConfiguration
    )
        : TemplateHashModel
    {
        private val beansWrapper = BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build()

        override fun get(contentMapKey: String): FmTemplateModel? {

            when (contentMapKey) {

                // Make the methods of ContentWrapper accessible in the template - e.g.: `${db.getPublishedPostsByTag(tagName).size()}`
                ModelAttributes.TMPL_DB_ACCESS -> return beansWrapper.wrap(db)

                ModelAttributes.DATA_FILES -> return beansWrapper.wrap(DataFileUtil(db, jbakeConfig.dataFileDocType))

                //ModelAttributes.DOC_DATE -> return freeMarkerWrapper.wrap(jbakeTemplateModel.get(ModelAttributes.DOC_DATE) ?: Date())
                ModelAttributes.DOC_DATE -> {
                    val odt: OffsetDateTime =
                        // OffsetDateTime.now()
                        jbakeTemplateModel.get(ModelAttributes.DOC_DATE) as? OffsetDateTime ?: OffsetDateTime.now()

                    return OffsetDateTimeModel(odt.truncatedTo(ChronoUnit.SECONDS))
                }

                // JBake config. Merged from engine's jbakeConfig and templateModel's config.
                ModelAttributes.TMPL_JBAKE_CONFIG -> {
                    val merged = mutableMapOf<String, Any>()
                    merged.putAll(jbakeConfig.asHashMap())
                    merged.putAll(jbakeTemplateModel.config)
                    return freeMarkerWrapper.wrap(merged)
                }
                else -> try {
                    ValueTracer.trace("freemarker-eager-model", jbakeTemplateModel.content, "LazyLoadingModel['$contentMapKey']")
                    val zoneForAdapter = jbakeConfig.freemarkerTimeZone.toZoneId() ?: ZoneId.systemDefault()
                    val adapter = FreemarkerTemplateModelAdapter(freeMarkerWrapper, zoneForAdapter)
                    val result: FmTemplateModel = extractors.extractAndTransform(db, contentMapKey, jbakeTemplateModel, adapter)

                    // Wrap Map results (especially document models like "content") with NullSafeMapModel.
                    // This ensures ${content.author} returns null instead of throwing InvalidReferenceException when the document doesn't have an author field.
                    // Combined with classicCompatible=true, null values are treated as empty strings in templates.
                    //if (result is SimpleHash)
                    //    return NullSafeMapModel(result, wrapper)
                    // Not needed as it turns out - no change in tests.

                    return result
                }
                catch (_: NoModelExtractorException) {
                    return freeMarkerWrapper.wrap(jbakeTemplateModel[contentMapKey])
                }
            }
        }

        override fun isEmpty() = false
    }
}


class FreemarkerTemplateModelAdapter(private val wrapper: ObjectWrapper, private val zoneId: ZoneId) : TemplateEngineAdapter<FmTemplateModel> {
    override fun adapt(key: String, extractedValue: Any): FmTemplateModel {
        // If the extracted value is a Java 8 time type, convert to java.util.Date and let the wrapper produce a TemplateDateModel.
        when (extractedValue) {
            is OffsetDateTime -> return wrapper.wrap(Date.from(extractedValue.toInstant()))
            is ZonedDateTime  -> return wrapper.wrap(Date.from(extractedValue.toInstant()))
            is Instant        -> return wrapper.wrap(Date.from(extractedValue))

            is LocalDateTime  -> return wrapper.wrap(Date.from(extractedValue.atZone(zoneId).toInstant()))
            is LocalDate      -> return wrapper.wrap(Date.from(extractedValue.atStartOfDay(zoneId).toInstant()))

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

typealias FmTemplateModel = TemplateModel
typealias FmConfiguration = Configuration
