package org.jbake.template

import de.neuland.pug4j.Pug4J
import de.neuland.pug4j.PugConfiguration
import de.neuland.pug4j.filter.CDATAFilter
import de.neuland.pug4j.filter.CssFilter
import de.neuland.pug4j.filter.JsFilter
import de.neuland.pug4j.model.PugModel
import de.neuland.pug4j.template.FileTemplateLoader
import de.neuland.pug4j.template.PugTemplate
import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.TemplateEngineAdapter.NoopAdapter
import org.jbake.template.model.TemplateModel
import java.io.File
import java.io.IOException
import java.io.Writer
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*


/**
 * Renders pages using the [Pug](https://pugjs.org/) template language (formerly known as Jade).
 * Migrated to pug4j 2.x which uses de.neuland.pug4j package names.
 */
class PugTemplateEngine : AbstractTemplateEngine {

    private val pugConfiguration = PugConfiguration()

    @Deprecated("")
    constructor(config: CompositeConfiguration, db: ContentStore, destination: File, templatesPath: File)
            : super(config, db, destination, templatesPath) {
        initPugConfiguration()
    }

    constructor(config: JBakeConfiguration, db: ContentStore) : super(config, db) {
        initPugConfiguration()
    }

    private fun initPugConfiguration() {
        // pug4j 2.x only supports .pug extension (not .jade)
        val templatePathStr = config.templateFolder.absolutePath
        val charset = Charset.forName(config.templateEncoding ?: "UTF-8")
        val loader = FileTemplateLoader(templatePathStr, charset, "pug")

        pugConfiguration.templateLoader = loader
        pugConfiguration.mode = Pug4J.Mode.XHTML
        pugConfiguration.isPrettyPrint = true
        pugConfiguration.setFilter(FILTER_CDATA, CDATAFilter())
        pugConfiguration.setFilter(FILTER_SCRIPT, JsFilter())
        pugConfiguration.setFilter(FILTER_STYLE, CssFilter())
        pugConfiguration.sharedVariables["formatter"] = FormatHelper()
    }

    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        log.debug("Rendering document with template: $templateName")
        try {
            val template = pugConfiguration.getTemplate(templateName)
            renderTemplate(template, model, writer)
        } catch (e: IOException) {
            throw RenderingException(e)
        }
    }

    fun renderTemplate(template: PugTemplate, model: TemplateModel, writer: Writer) {
        val pugModel = wrap(model)
        pugModel.putAll(pugConfiguration.sharedVariables)
        template.process(pugModel, writer, pugConfiguration)
    }

    private fun wrap(model: TemplateModel): PugModel {
        return object : PugModel(model) {
            override fun get(key: String): Any? {
                // First check if it's in the PugModel map itself (e.g., shared variables like formatter)
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

        // Provide an HTML-escaping helper method accessible from Pug/Jade templates as formatter.escape(...)
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

    private val log = org.slf4j.LoggerFactory.getLogger(PugTemplateEngine::class.java)
}
