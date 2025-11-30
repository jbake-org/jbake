package org.jbake.template

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.ModelAttributes.DATE
import org.jbake.template.TemplateEngineAdapter.NoopAdapter
import org.jbake.template.model.TemplateModel
import java.io.File
import java.io.Writer

/**
 * Renders documents using the GroovyMarkupTemplateEngine.
 * The file extension to activate this Engine is .tpl
 *
 * @see [Groovy MarkupTemplateEngine Documentation](http://groovy-lang.org/templating.html._the_markuptemplateengine)
 */
class GroovyMarkupTemplateEngine : AbstractTemplateEngine {
    private var templateConfiguration: TemplateConfiguration? = null
    private var templateEngine: MarkupTemplateEngine? = null

    @Deprecated("Use {@link #GroovyMarkupTemplateEngine(JBakeConfiguration, ContentStore)} instead")
    constructor(config: CompositeConfiguration, db: ContentStore, destination: File, templatesPath: File)
        : super(config, db, destination, templatesPath)
    {
        setupTemplateConfiguration()
        initializeTemplateEngine()
    }

    constructor(config: JBakeConfiguration, db: ContentStore) : super(config, db) {
        setupTemplateConfiguration()
        initializeTemplateEngine()
    }

    private fun setupTemplateConfiguration() {
        templateConfiguration = TemplateConfiguration()
        templateConfiguration!!.isUseDoubleQuotes = true
        templateConfiguration!!.isAutoIndent = true
        templateConfiguration!!.isAutoNewLine = true
        templateConfiguration!!.isAutoEscape = true
    }

    private fun initializeTemplateEngine() {
        templateEngine = MarkupTemplateEngine(
            MarkupTemplateEngine::class.java.getClassLoader(),
            config.templateDir,
            templateConfiguration
        )
    }

    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        try {
            val template = templateEngine!!.createTemplateByPath(templateName)
            val wrappedModel = wrap(model)
            val writable = template.make(wrappedModel)
            writable.writeTo(writer)
        } catch (e: Exception) {
            throw RenderingException(e)
        }
    }

    private fun wrap(model: TemplateModel): TemplateModel {

        return object : TemplateModel(model) {

            override fun get(key: String): Any? {
                try {
                    // Pass a plain map to avoid invoking the overridden get and causing recursion.
                    @Suppress("UNCHECKED_CAST")
                    val plainMap = (model as? Map<String, Any>)?.toMutableMap() ?: return null
                    val extracted = extractors.extractAndTransform(db, key, plainMap, NoopAdapter())
                    return transformForGroovy(extracted)
                } catch (e: NoModelExtractorException) {
                    return model[key]
                }
            }
        }
    }

    /** SafeDate wrapper that prevents NPE when Groovy templates call date.format() on null dates */
    private class SafeDate(private val date: java.util.Date?) {
        fun format(pattern: String) = date?.let { java.text.SimpleDateFormat(pattern).format(it) } ?: ""
        override fun toString() = date?.toString() ?: ""
    }

    /** Recursively wrap date fields in SafeDate to prevent NPE in Groovy templates */
    private fun transformForGroovy(value: Any?): Any? = when (value) {
        null -> null
        is org.jbake.model.DocumentModel -> HashMap(value).apply { put(DATE, SafeDate(value.date)) }
        is org.jbake.model.BaseModel -> HashMap(value).apply { put(DATE, SafeDate(value[DATE] as? java.util.Date)) }
        is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            val map = value as? Map<String, Any?> ?: return value
            HashMap(map).apply { put(DATE, SafeDate(map[DATE] as? java.util.Date)) }
        }
        is Collection<*> -> value.map(::transformForGroovy)
        else -> value
    }
}
