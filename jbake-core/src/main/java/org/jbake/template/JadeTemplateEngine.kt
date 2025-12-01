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
import org.jbake.util.Logging.logger
import org.jbake.util.PathUtils
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

/**
 *  Legacy. Jade is now Pug. Use PugTemplateEngine instead and migrate your templates.
 */
class JadeTemplateEngine : AbstractTemplateEngine {

    private val jadeConfiguration = JadeConfiguration()

    @Deprecated("")
    constructor(config: CompositeConfiguration, db: ContentStore, destination: File, templatesPath: File)
        : super(config, db, destination, templatesPath) {
        initJadeConfiguration()
    }

    constructor(config: JBakeConfiguration, db: ContentStore) : super(config, db) {
        initJadeConfiguration()
    }

    private fun initJadeConfiguration() {
        // Use PathUtil.dirPrefix to provide a proper directory prefix string (ensures trailing separator)
        val loader: TemplateLoader = FileTemplateLoader(PathUtils.ensureTrailingSeparatorForDirectory(config.templateDir), config.templateEncoding)
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
                // First check if it's in the JadeModel map itself (e.g., shared variables like formatter)
                super.get(key)?.let { return it }

                return try {
                    extractors.extractAndTransform(db, key, this, NoopAdapter())
                } catch (e: NoModelExtractorException) {
                    model[key]
                }
            }
        }
    }

    class FormatHelper {
        private val formatters: MutableMap<String, SimpleDateFormat> = HashMap()

        fun format(date: Date?, pattern: String?): String {
            if (date == null || pattern == null) return ""

            return formatters.getOrPut(pattern) { SimpleDateFormat(pattern) }.format(date)
        }

        // Provide an HTML-escaping helper method accessible from Jade/JEXL as formatter.escape(...)
        fun escape(input: Any?): String {
            val s = input?.toString() ?: ""
            return buildString(s.length) {
                s.forEach { ch ->
                    when (ch) {
                        '&' -> append("&amp;")
                        '<' -> append("&lt;")
                        '>' -> append("&gt;")
                        '"' -> append("&quot;")
                        '\'' -> append("&#39;")
                        else -> append(ch)
                    }
                }
            }
        }
    }

    companion object {
        private const val FILTER_CDATA = "cdata"
        private const val FILTER_STYLE = "css"
        private const val FILTER_SCRIPT = "js"
    }

    private val log: Logger by logger()
}
