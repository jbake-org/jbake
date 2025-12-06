package org.jbake.template

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.NoModelExtractorException
import org.jbake.app.RenderingException
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.ModelAttributes.DOC_DATE
import org.jbake.template.TemplateEngineAdapter.NoopAdapter
import org.jbake.template.model.TemplateModel
import java.io.Writer
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Renders documents using the GroovyMarkupTemplateEngine.
 * The file extension to activate this Engine is .tpl
 *
 * @see [Groovy MarkupTemplateEngine Documentation](http://groovy-lang.org/templating.html._the_markuptemplateengine)
 */
class GroovyMarkupTemplateEngine(config: JBakeConfiguration, db: ContentStore) : AbstractTemplateEngine(config, db) {
    private var templateConfiguration: TemplateConfiguration? = null
    private var templateEngine: MarkupTemplateEngine? = null

    init {
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
            throw RenderingException(e.message ?: "",e)
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
    private class SafeDate(private val date: OffsetDateTime?) {
        fun format(pattern: String) = date?.let { DateTimeFormatter.ofPattern(pattern).format(it) } ?: ""
        override fun toString() = date?.toString() ?: ""
    }

    /** Recursively wrap date fields in SafeDate to prevent NPE in Groovy templates */
    private fun transformForGroovy(value: Any?): Any? = when (value) {
        null -> null
        is org.jbake.model.DocumentModel -> HashMap(value).apply { put(DOC_DATE, SafeDate(value.date)) }
        is org.jbake.model.BaseModel -> HashMap(value).apply { put(DOC_DATE, SafeDate(value[DOC_DATE] as? OffsetDateTime)) }
        is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            val map = value as? Map<String, Any?> ?: return value
            HashMap(map).apply { put(DOC_DATE, SafeDate(map[DOC_DATE] as? OffsetDateTime)) }
        }
        is Collection<*> -> value.map(::transformForGroovy)
        else -> value
    }
}
