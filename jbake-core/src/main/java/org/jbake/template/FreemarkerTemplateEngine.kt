package org.jbake.template

import freemarker.ext.beans.BeansWrapperBuilder
import freemarker.template.*
import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.ModelAttributes
import org.jbake.template.model.TemplateModel
import org.jbake.util.DataFileUtil
import java.io.File
import java.io.IOException
import java.io.Writer
import java.util.*


/** Renders pages using the [Freemarker](http://freemarker.org/) template engine. */
class FreemarkerTemplateEngine : AbstractTemplateEngine {
    lateinit var templateCfg: Configuration

    @Deprecated("")
    constructor(config: CompositeConfiguration, db: ContentStore, destination: File, templatesPath: File)
        : super(config, db, destination, templatesPath)
    {
        createTemplateConfiguration()
    }

    constructor(config: JBakeConfiguration, db: ContentStore) : super(config, db) {
        createTemplateConfiguration()
    }

    private fun createTemplateConfiguration() {
        templateCfg = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
        templateCfg.setDefaultEncoding(config.renderEncoding)
        templateCfg.setOutputEncoding(config.outputEncoding)
        templateCfg.setTimeZone(config.freemarkerTimeZone)
        templateCfg.setSQLDateAndTimeTimeZone(config.freemarkerTimeZone)
        try {
            templateCfg.setDirectoryForTemplateLoading(config.templateDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        try {
            val template = templateCfg.getTemplate(templateName)
            template.process(LazyLoadingModel(templateCfg.objectWrapper, model, db, config), writer)
        } catch (e: IOException) {
            throw RenderingException(e)
        } catch (e: TemplateException) {
            throw RenderingException(e)
        }
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

                if (key == ModelAttributes.DB) {
                    val bwb = BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
                    val bw = bwb.build()
                    return bw.wrap(db)
                }
                if (key == ModelAttributes.DATA) {
                    val bwb = BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
                    val bw = bwb.build()
                    return bw.wrap(DataFileUtil(db, config.dataFileDocType))
                }

                // Provide a merged config map to templates so both legacy underscore keys (feed_file)
                // and dotted keys are available. Prefer values from configuration.asHashMap().
                if (key == ModelAttributes.CONFIG) {
                    val merged: MutableMap<String, Any> = HashMap()

                    // Base from configuration (underscore-style keys)
                    runCatching { merged.putAll(config.asHashMap()) }

                    // Overlay any values present in the eager model's config
                    @Suppress("UNCHECKED_CAST")
                    val map = eagerModel.toMap() as? MutableMap<String, Any> ?: mutableMapOf()
                    val cfgAny = map[ModelAttributes.CONFIG]
                    if (cfgAny is Map<*, *>)
                        @Suppress("UNCHECKED_CAST")
                        (cfgAny as? Map<String, Any>)?.let { merged.putAll(it) }
                    return wrapper.wrap(merged)
                }

                 val adapter = object : TemplateEngineAdapter<freemarker.template.TemplateModel> {

                     override fun adapt(key: String, extractedValue: Any): freemarker.template.TemplateModel {
                        return when (key) {
                            ModelAttributes.ALLTAGS -> SimpleCollection(extractedValue as MutableCollection<*>?, wrapper)
                            ModelAttributes.PUBLISHED_DATE -> SimpleDate(extractedValue as Date?, TemplateDateModel.UNKNOWN)
                            // All other cases, as far as I know, are document collections
                            else -> SimpleSequence(extractedValue as MutableCollection<*>?, wrapper)
                        }
                    }
                }
                @Suppress("UNCHECKED_CAST")
                val map = eagerModel.toMap() as MutableMap<String, Any> // TBD converter function to check the types.
                @Suppress("UNCHECKED_CAST")
                val adapterTyped = adapter as TemplateEngineAdapter<freemarker.template.TemplateModel?>
                return extractors.extractAndTransform(db, key, map, adapterTyped)
            }
            catch (_: NoModelExtractorException) {
                return eagerModel.get(key)
            }
        }

        override fun isEmpty(): Boolean {
            return false
        }
    }
}
