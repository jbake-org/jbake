package org.jbake.template

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
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

    /**
     * @param templatesPath the templates path
     */
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
            config.templateFolder,
            templateConfiguration
        )
    }

    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        try {
            val template = templateEngine!!.createTemplateByPath(templateName)
            val wrappedModel: MutableMap<String, Any> = wrap(model)
            val writable = template.make(wrappedModel)
            writable.writeTo(writer)
        } catch (e: Exception) {
            throw RenderingException(e)
        }
    }

    private fun wrap(model: TemplateModel): TemplateModel {

        return object : TemplateModel(model) {

            override fun get(key: String): Any? {
                return try {
                    extractors.extractAndTransform(db, key, model, NoopAdapter())
                }
                // super.get() which would recurse
                catch (e: NoModelExtractorException) { model[key] }
            }
        }
    }
}
