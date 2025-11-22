package org.jbake.template

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.lang.LocaleUtils
import org.jbake.app.ContentStore
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.ModelAttributes
import org.jbake.template.model.TemplateModel
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.context.LazyContextVariable
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.File
import java.io.Writer
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 *
 * A template engine which renders pages using Thymeleaf.
 *
 *
 * This template engine is not recommended for large sites because the whole model
 * is loaded into memory due to Thymeleaf internal limitations.
 *
 *
 * The default rendering mode is "HTML", but it is possible to use another mode
 * for each document type, by adding a key in the configuration, for example:
 *
 * `
 * template.feed.thymeleaf.mode=XML
` *
 *
 * @author Cédric Champeau
 */
class ThymeleafTemplateEngine : AbstractTemplateEngine {
    private val lock = ReentrantLock()
    private var templateEngine: TemplateEngine? = null
    private val context: Context
    private var templateResolver: FileTemplateResolver? = null

    /**
     * @param config the [CompositeConfiguration] of jbake
     * @param db the [ContentStore]
     * @param destination the destination path
     * @param templatesPath the templates path
     */
    @Deprecated("""Use {@link #ThymeleafTemplateEngine(JBakeConfiguration, ContentStore)} instead """)
    constructor(config: CompositeConfiguration, db: ContentStore, destination: File, templatesPath: File)
        : super(config, db, destination, templatesPath)
    {
        this.context = Context()
        initializeTemplateEngine()
    }

    constructor(config: JBakeConfiguration, db: ContentStore) : super(config, db) {
        this.context = Context()
        initializeTemplateEngine()
    }

    private fun initializeTemplateEngine() {

        val resolver = FileTemplateResolver()
        templateResolver = resolver
        resolver.prefix = config.templateFolder!!.absolutePath + File.separatorChar
        resolver.characterEncoding = config.templateEncoding
        resolver.setTemplateMode(DefaultJBakeConfiguration.DEFAULT_TYHMELEAF_TEMPLATE_MODE)

        val engine = TemplateEngine()
        templateEngine = engine
        engine.setTemplateResolver(templateResolver)
        engine.clearTemplateCache()
    }

    private fun updateTemplateMode(model: TemplateModel) {
        templateResolver!!.setTemplateMode(getTemplateModeByModel(model))
    }

    private fun getTemplateModeByModel(model: TemplateModel): String {
        val content = model.content
        return config.getThymeleafModeByType(content.type)
    }

    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        val localeString = config.thymeleafLocale
        val locale = if (localeString != null) LocaleUtils.toLocale(localeString) else Locale.getDefault()

        lock.lock()
        try {
            initializeContext(locale, model)
            updateTemplateMode(model)
            val engine = templateEngine ?: error("templateEngine must not be null")
            engine.process(templateName, context, writer)
        } finally {
            lock.unlock()
        }
    }

    private fun initializeContext(locale: Locale, model: TemplateModel) {
        context.clearVariables()
        context.locale = locale
        context.setVariables(model)

        for (key in extractors.keySet()) {
            context.setVariable(key, ContextVariable(db, key, model))
        }
    }

    /**
     * Helper class to lazy load data form extractors by key.
     */
    private class ContextVariable(
        private val db: ContentStore,
        private val key: String,
        private val model: TemplateModel
    ) : LazyContextVariable<Any>() {

        override fun loadValue(): Any {
            try {
                val adapter = object : TemplateEngineAdapter<LazyContextVariable<*>> {

                    override fun adapt(key: String, extractedValue: Any): LazyContextVariable<*> {
                        return when (key) {
                            ModelAttributes.ALLTAGS -> object : LazyContextVariable<MutableSet<*>>() {
                                override fun loadValue() = extractedValue as MutableSet<*>?
                            }

                            ModelAttributes.PUBLISHED_DATE -> object : LazyContextVariable<Date>() {
                                override fun loadValue() = extractedValue as Date?
                            }

                            else -> object : LazyContextVariable<Any>() {
                                override fun loadValue(): Any = extractedValue
                            }
                        }
                    }
                }
                return extractors.extractAndTransform(db, key, model, adapter)?.getValue() ?: ""
            }
            catch (e: NoModelExtractorException) { return "" }
        }
    }
}
