package org.jbake.template

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.NoModelExtractorException
import org.jbake.app.RenderingException
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.ModelAttributes.DOC_DATE
import org.jbake.template.model.JbakeTemplateModel
import org.jbake.util.Logging.logger
import java.io.Writer
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Renders documents using the GroovyMarkupTemplateEngine.
 * The file extension to activate this Engine is .tpl
 *
 * @see [Groovy MarkupTemplateEngine Documentation](http://groovy-lang.org/templating.html._the_markuptemplateengine)
 */
class GroovyMarkupTemplateEngine(config: JBakeConfiguration, db: ContentStore) : AbstractTemplateEngine(config, db) {
    private var templateConfiguration: TemplateConfiguration? = null
    private var templateEngine: MarkupTemplateEngine? = null
    private val log by logger()

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
    override fun renderDocument(model: JbakeTemplateModel, templateName: String, writer: Writer) {
        try {
            val template = templateEngine!!.createTemplateByPath(templateName)
            val wrappedModel = wrap(model)
            val writable = template.make(wrappedModel)
            writable.writeTo(writer)
        }
        catch (e: Exception) {
            log.error("Groovy: ${e.message}")
            throw RenderingException(e.message ?: "",e)
        }
    }

    private fun wrap(model: JbakeTemplateModel): JbakeTemplateModel {

        return object : JbakeTemplateModel(model) {

            override fun get(key: String): Any? {
                try {
                    // Pass a plain map to avoid invoking the overridden get and causing recursion.
                    //val plainMap = (model as? Map<String, Any>)?.toMutableMap() ?: return null
                    val plainMap = (model as Map<String, Any>).toMutableMap()
                    val extracted = extractors.extractAndTransform(db, key, plainMap,
                        //TemplateEngineAdapter.NoopAdapter())
                        GroovyTemplateModelAdapter())

                    return transformForGroovy(extracted)
                }
                catch (e: NoModelExtractorException) {
                    log.error("NoModelExtractorException: ${e.message}")
                    return model[key]
                }
            }
        }
    }

    /** Wrapper that prevents NPE when Groovy templates call date.format() on null dates */
    interface SafeGroovyTemporal {
        fun format(pattern: String): String
        companion object {
            fun wrap(it: OffsetDateTime?) = SafeOffsetDateTime(it)
            fun wrap(it: Date?) = SafeJavaUtilDate(it)
        }
    }
    class SafeOffsetDateTime(private val dateTime: OffsetDateTime?) : SafeGroovyTemporal {
        // TBD: Test the pattern at the start.
        override fun format(pattern: String) = runCatching { dateTime?.let { DateTimeFormatter.ofPattern(pattern).format(it) } }.getOrNull() ?: ""
        override fun toString() = dateTime?.toString() ?: ""
    }

    class SafeJavaUtilDate(private val javaUtilDate: Date?) : SafeGroovyTemporal {
        override fun format(pattern: String) = runCatching { javaUtilDate?.let { SimpleDateFormat(pattern).format(it) } }.getOrNull() ?: ""
        override fun toString() = javaUtilDate?.toString() ?: ""
    }

    /** Recursively wrap date fields in SafeDate to prevent NPE in Groovy templates */
    private fun transformForGroovy(value: Any?): Any? =
        when (value) {
            null -> null
            is org.jbake.model.DocumentModel -> HashMap(value).apply { put(DOC_DATE, SafeGroovyTemporal.wrap(value.date)) }
            is org.jbake.model.BaseModel -> HashMap(value).apply { put(DOC_DATE, SafeGroovyTemporal.wrap(value[DOC_DATE] as? OffsetDateTime)) }
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val map = value as? Map<String, Any> ?: throw Exception("Can't convert map to Map<String, Any>: $value")
                HashMap(map).apply { put(DOC_DATE, SafeOffsetDateTime(map[DOC_DATE] as OffsetDateTime)) }
            }
            is Collection<*> -> value.map(::transformForGroovy)
            else -> value
        }
}


class GroovyTemplateModelAdapter(private val offset: ZoneOffset = ZoneOffset.UTC) : TemplateEngineAdapter<Any> {
    override fun adapt(key: String, extractedValue: Any): Any {
        return convertTemporalsToDate(extractedValue, offset)
    }
}

private fun convertTemporalsToDate(extractedValue: Any, offset: ZoneOffset = ZoneOffset.UTC): Any {
    when (extractedValue) {
        is OffsetDateTime -> return Date.from(extractedValue.toInstant())
        is ZonedDateTime -> return Date.from(extractedValue.toInstant())
        is Instant -> return Date.from(extractedValue)
        is LocalDateTime -> return Date.from(extractedValue.atOffset(offset).toInstant())
        is LocalDate -> return Date.from(extractedValue.atTime(0,0,0).atOffset(offset).toInstant())
        is Date -> return extractedValue
        else -> return extractedValue
    }
}
