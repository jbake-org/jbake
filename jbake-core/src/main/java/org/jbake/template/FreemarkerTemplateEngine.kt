package org.jbake.template

import freemarker.core.Environment
import freemarker.core.InvalidReferenceException
import freemarker.ext.beans.BeansWrapperBuilder
import freemarker.template.*
import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.ModelAttributes
import org.jbake.template.model.TemplateModel
import org.jbake.util.AuthorTracer
import org.jbake.util.DataFileUtil
import org.jbake.util.Logging.logger
import java.io.IOException
import java.io.Writer
import java.util.*


/**
 * Renders pages using the [Freemarker](http://freemarker.org/) template engine.
 */
class FreemarkerTemplateEngine(config: JBakeConfiguration, db: ContentStore) : AbstractTemplateEngine(config, db) {

    lateinit var templateCfg: Configuration

    init {
        createTemplateConfiguration()
    }

    private fun createTemplateConfiguration() {
        templateCfg = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
        templateCfg.setDefaultEncoding(config.renderEncoding)
        templateCfg.setOutputEncoding(config.outputEncoding)
        templateCfg.setTimeZone(config.freemarkerTimeZone)
        templateCfg.setSQLDateAndTimeTimeZone(config.freemarkerTimeZone)

        // Configure FreeMarker to handle missing map keys gracefully.
        // This makes it so that ${content.author} returns empty string when author key is missing.
        val objectWrapper = LazyLoadingModel.NullSafeObjectWrapper(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
        templateCfg.setObjectWrapper(objectWrapper)
        templateCfg.setClassicCompatible(true)

        // Custom exception handler that ignores InvalidReferenceException
        // Silently replace missing references with empty string.
        templateCfg.setTemplateExceptionHandler { ex: TemplateException, env: Environment, out ->
            System.err.println("FreemarkerExceptionHandler caught: ${ex.javaClass.simpleName}: ${ex.message}")
            if (ex is InvalidReferenceException) {
                System.err.println("  -> Replacing with empty string")/// DEBUG
                out.write("")
            } else {
                System.err.println("  -> Rethrowing")/// DEBUG
                throw ex
            }
        }

        try {
            templateCfg.setDirectoryForTemplateLoading(config.templateDir)
        } catch (e: IOException) {
            log.warn("Failed to set template directory: ${e.message}", e)
        }
    }

    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        try {
            val template = templateCfg.getTemplate(templateName)
            template.process(LazyLoadingModel(templateCfg.objectWrapper, model, db, config), writer)
        }
        catch (e: IOException) { throw RenderingException(e) }
        catch (e: TemplateException) { throw RenderingException(e) }
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
        private val eagerModel = SimpleHash(eagerModel, wrapper)

        @Throws(TemplateModelException::class)
        override fun get(key: String): freemarker.template.TemplateModel? {
            try {
                // GIT Issue#357: Accessing db in freemarker template throws exception
                // When content store is accessed with key "db" then wrap the ContentStore with BeansWrapper and return to template.
                // All methods on db are then accessible in template. Eg: ${db.getPublishedPostsByTag(tagName).size()}

                if (key == ModelAttributes.TMPL_DB_ACCESS) {
                    val bwb = BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
                    val bw = bwb.build()
                    return bw.wrap(db)
                }
                if (key == ModelAttributes.DATA_FILES) {
                    val bwb = BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
                    val bw = bwb.build()
                    return bw.wrap(DataFileUtil(db, config.dataFileDocType))
                }

                // Provide a merged config map to templates so both legacy underscore keys (feed_file)
                // and dotted keys are available. Prefer values from configuration.asHashMap().
                if (key == ModelAttributes.TMPL_JBAKE_CONFIG) {
                    val merged: MutableMap<String, Any> = HashMap()

                    // Base from configuration (underscore-style keys)
                    runCatching { merged.putAll(config.asHashMap()) }

                    // Overlay any values present in the eager model's config
                    @Suppress("UNCHECKED_CAST")
                    val map = eagerModel.toMap() as? MutableMap<String, Any> ?: mutableMapOf()
                    val cfgAny = map[ModelAttributes.TMPL_JBAKE_CONFIG]
                    if (cfgAny is Map<*, *>)
                        @Suppress("UNCHECKED_CAST")
                        (cfgAny as? Map<String, Any>)?.let { merged.putAll(it) }
                    return wrapper.wrap(merged)
                }

                val adapter = object : TemplateEngineAdapter<freemarker.template.TemplateModel> {
                    override fun adapt(key: String, extractedValue: Any): freemarker.template.TemplateModel {
                        return when (key) {
                            ModelAttributes.TAGS_ALL -> SimpleCollection(extractedValue as MutableCollection<*>?, wrapper)
                            ModelAttributes.GLOB_PUBLISHING_DATE_FORMATTED -> SimpleDate(extractedValue as Date?, TemplateDateModel.UNKNOWN)
                            // All other cases, as far as I know, are document collections
                            else -> SimpleSequence(extractedValue as MutableCollection<*>?, wrapper)
                        }
                    }
                }

                @Suppress("UNCHECKED_CAST")
                val map = eagerModel.toMap() as MutableMap<String, Any> // TBD converter function to check the types.
                @Suppress("UNCHECKED_CAST")
                val adapterTyped = adapter as TemplateEngineAdapter<freemarker.template.TemplateModel?>
                AuthorTracer.trace("freemarker-eager-model", map[ModelAttributes.TMPL_CONTENT_MODEL], key)
                return extractors.extractAndTransform(db, key, map, adapterTyped)

                /*
                val result = extractors.extractAndTransform(db, key, map, adapterTyped)
                val result = eagerModel.get(key)

                // Wrap Map results with NullSafeMapModel to handle missing keys gracefully
                if (result is SimpleHash)
                    return NullSafeMapModel(result, wrapper)
                return result
                */
            }
            catch (_: NoModelExtractorException) {
                return eagerModel.get(key)
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


    private val log by logger()
}
