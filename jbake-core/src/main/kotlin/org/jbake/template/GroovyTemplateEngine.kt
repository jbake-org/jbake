package org.jbake.template

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.text.XmlTemplateEngine
import org.codehaus.groovy.runtime.MethodClosure
import org.jbake.app.ContentStore
import org.jbake.app.NoModelExtractorException
import org.jbake.app.RenderingException
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.model.TemplateModel
import org.xml.sax.SAXException
import java.io.IOException
import java.io.Writer
import javax.xml.parsers.ParserConfigurationException


/** Renders documents using a Groovy template engine - [SimpleTemplateEngine] or [XmlTemplateEngine] (.gxml). */
class GroovyTemplateEngine(config: JBakeConfiguration, db: ContentStore) : AbstractTemplateEngine(config, db) {
    private val cachedTemplates: MutableMap<String, Template?> = HashMap()


    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        try {
            val template = findTemplate(templateName)
            val writable = template.make(wrap(model))
            writable.writeTo(writer)
        }
        catch (e: Exception) { throw RenderingException(e.message?:"",e) }
    }

    @Throws(SAXException::class, ParserConfigurationException::class, ClassNotFoundException::class, IOException::class)
    private fun findTemplate(templateName: String): Template {
        val ste = if (templateName.endsWith(".gxml")) XmlTemplateEngine() else SimpleTemplateEngine()
        val sourceTemplate = config.templateDir.resolve(templateName)
        var template = cachedTemplates[templateName]
        if (template == null) {
            template = ste.createTemplate(sourceTemplate)
            cachedTemplates[templateName] = template
        }
        return template
    }

    private fun wrap(model: TemplateModel): TemplateModel {

        return object : TemplateModel(model) {

            override fun get(key: String): Any? {
                if ("include" == key)
                    return MethodClosure(this@GroovyTemplateEngine, "doInclude").curry(this)

                return try {
                    extractors.extractAndTransform(db, key, model, TemplateEngineAdapter.NoopAdapter())
                }
                // super.get() would recurse, so we directly access the backing map.
                catch (e: NoModelExtractorException) { model[key] }
            }
        }
    }

    private fun doInclude(model: TemplateModel, templateName: String) {
        val engine: AbstractTemplateEngine = model.renderer!!
        val out = model.writer!!
        engine.renderDocument(model, templateName, out)
    }
}
