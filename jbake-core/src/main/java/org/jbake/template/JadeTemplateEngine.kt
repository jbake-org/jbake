package org.jbake.template

import de.neuland.jade4j.Jade4J
import de.neuland.jade4j.JadeConfiguration
import de.neuland.jade4j.filter.CDATAFilter
import de.neuland.jade4j.filter.CssFilter
import de.neuland.jade4j.filter.JsFilter
import de.neuland.jade4j.model.JadeModel
import de.neuland.jade4j.template.FileTemplateLoader
import de.neuland.jade4j.template.JadeTemplate
import de.neuland.jade4j.template.TemplateLoader
import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.TemplateEngineAdapter.NoopAdapter
import org.jbake.template.model.TemplateModel
import java.io.File
import java.io.IOException
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*


/**
 * Renders pages using the [Jade](http://jade.org/) template language.
 * @author Mariusz Smykuła
 */
class JadeTemplateEngine : AbstractTemplateEngine {
    private val jadeConfiguration = JadeConfiguration()

    @Deprecated("")
    constructor(config: CompositeConfiguration, db: ContentStore, destination: File, templatesPath: File)
        : super(config, db, destination, templatesPath)

    constructor(config: JBakeConfiguration, db: ContentStore) : super(config, db) {
        val loader: TemplateLoader =
            FileTemplateLoader(config.templateFolder.path + File.separatorChar, config.templateEncoding)
        jadeConfiguration.templateLoader = loader
        jadeConfiguration.mode = Jade4J.Mode.XHTML
        jadeConfiguration.isPrettyPrint = true
        jadeConfiguration.setFilter(FILTER_CDATA, CDATAFilter())
        jadeConfiguration.setFilter(FILTER_SCRIPT, JsFilter())
        jadeConfiguration.setFilter(FILTER_STYLE, CssFilter())
        jadeConfiguration.sharedVariables["formatter"] = FormatHelper()
    }

    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        try {
            val template = jadeConfiguration.getTemplate(templateName)

            renderTemplate(template, model, writer)
        } catch (e: IOException) {
            throw RenderingException(e)
        }
    }

    fun renderTemplate(template: JadeTemplate, model: TemplateModel, writer: Writer) {
        val jadeModel = wrap(model)
        jadeModel.putAll(jadeConfiguration.sharedVariables)
        template.process(jadeModel, writer)
    }

    private fun wrap(model: TemplateModel): JadeModel {

        return object : JadeModel(model) {
            override fun get(key: String): Any? {
                return try {
                    extractors.extractAndTransform(db, key, this, NoopAdapter())
                }
                // super.get() which would recurse
                catch (e: NoModelExtractorException) { model[key] }
            }
        }
    }

    class FormatHelper {
        private val formatters: MutableMap<String, SimpleDateFormat> = HashMap<String, SimpleDateFormat>()

        fun format(date: Date?, pattern: String?): String {
            if (date == null || pattern == null)
                return ""

            var df = formatters[pattern]
            if (df == null) {
                df = SimpleDateFormat(pattern)
                formatters[pattern] = df
            }

            return df.format(date)
        }
    }

    companion object {
        private const val FILTER_CDATA = "cdata"
        private const val FILTER_STYLE = "css"
        private const val FILTER_SCRIPT = "js"
    }
}
