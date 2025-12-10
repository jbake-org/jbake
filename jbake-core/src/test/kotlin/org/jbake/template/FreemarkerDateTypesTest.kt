package org.jbake.template

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import no.api.freemarker.java8.Java8ObjectWrapper
import org.jbake.util.convertDatesInModel
import java.io.StringWriter
import java.time.*
import java.util.*

/**
 * Minimal tests to ensure Freemarker receives different Java date/time types and the templates can format them.
 */
class FreemarkerDateTypesTest : StringSpec({

    "freemarker formats Date with string pattern" {
        @Suppress("DEPRECATION") val date = Date(2025-1900, 12-1, 10, 14, 30, 0)
        val map = mutableMapOf<String, Any>("content" to mapOf("mydate" to date))
        val template = $$"${content.mydate?string('yyyy-MM-dd HH:mm:ssXXX')}"

        val out = renderInlineTemplate(map, template)
        out.shouldContain("2025-12-10 14:30:00+01:00")
    }

    // Error: For "...(...)" callee: Expected a method or function, but this has evaluated to a string (wrapper: f.t.SimpleScalar):
    "freemarker formats OffsetDateTime, wrapped" {
        val odt = OffsetDateTime.of(2025, 12, 10, 14, 30, 0, 0, ZoneOffset.ofHours(1))
        val map = mutableMapOf<String, Any>("content" to mapOf("date" to OffsetDateTimeModel(odt)))
        val template = $$"${content.date?string('yyyy-MM-dd HH:mm:ssXXX')}"

        val out = renderInlineTemplate(map, template)
        out.shouldContain("2025-12-10 14:30:00+01:00")
    }

    // Error: For "...(...)" callee: Expected a method or function, but this has evaluated to a string (wrapper: f.t.SimpleScalar):
    "freemarker formats OffsetDateTime with string pattern" {
        val odt = OffsetDateTime.of(2025, 12, 10, 14, 30, 0, 0, ZoneOffset.ofHours(1))
        val map = mutableMapOf<String, Any>("content" to mapOf("date" to odt))
        val template = $$"${content.date?string('yyyy-MM-dd HH:mm:ssXXX')}"

        val out = renderInlineTemplate(map, template)
        out.shouldContain("2025-12-10 14:30:00+01:00")
    }

    "freemarker formats ZonedDateTime with string pattern" {
        val zdt = ZonedDateTime.of(2025,12,10,14,30,0,0, ZoneId.of("Europe/Prague"))
        val map = mutableMapOf<String, Any>("content" to mapOf("date" to zdt))
        val template = $$"${content.date?string('yyyy-MM-dd HH:mm:ss Z')}"

        val out = renderInlineTemplate(map, template)
        out.shouldContain("2025-12-10 14:30:00")
    }

    "freemarker formats LocalDateTime and LocalDate" {
        val ldt = LocalDateTime.of(2025,12,10,8,15,5)
        val ld = LocalDate.of(2025,12,10)
        val map = mutableMapOf<String, Any>("content" to mapOf("ldt" to ldt, "ld" to ld))
        val t1 = $$"${content.ldt?string('yyyy/MM/dd HH:mm')}-${content.ld?string('yyyy/MM/dd')}"

        val out = renderInlineTemplate(map, t1)
        out.shouldContain("2025/12/10 08:15")
        out.shouldContain("2025/12/10")
    }

    "freemarker formats Instant and Date types" {
        val instant = Instant.parse("2025-12-10T12:00:00Z")
        val utilDate = Date.from(instant)
        val sqlDate = java.sql.Date.from(instant)
        val map = mutableMapOf<String, Any>("content" to mapOf("inst" to instant, "ud" to utilDate, "sd" to sqlDate))
        val t = $$"${content.inst?string('yyyy-MM-dd')},${content.ud?string('yyyy-MM-dd')},${content.sd?string('yyyy-MM-dd')}"

        val out = renderInlineTemplate(map, t)
        out.shouldContain("2025-12-10")
    }

    "freemarker date-time built-ins" {
        val odt = OffsetDateTime.of(2025,12,5,9,0,0,0, ZoneOffset.UTC)
        val map = mutableMapOf<String, Any>("content" to mapOf("date" to odt))
        val t = $$"${content.date?date?string('yyyy-MM-dd')}|${content.date?time?string('HH:mm:ss')}|${content.date?datetime?string('yyyy-MM-dd HH:mm:ssXXX')}"

        val out = renderInlineTemplate(map, t)
        out.shouldContain("2025-12-05")
        out.shouldContain("09:00:00")
        out.shouldContain("2025-12-05 09:00:00")
    }

})


// Shared helper for test rendering of inline Freemarker templates
fun renderInlineTemplate(modelMap: MutableMap<String, Any>, templateText: String): String {
    val cfg = Configuration(Configuration.VERSION_2_3_34)
    cfg.defaultEncoding = "UTF-8"
    cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    // See
    cfg.objectWrapper = Java8ObjectWrapper(Configuration.VERSION_2_3_34)

    // Use StringTemplateLoader to load template text under a name
    val loader = freemarker.cache.StringTemplateLoader()
    val templateName = "inline"
    loader.putTemplate(templateName, templateText)
    cfg.templateLoader = loader

    // Convert java.time types to java.util.Date for compatibility
    val converted = convertDatesInModel(modelMap) as? Map<*, *> ?: modelMap

    // Convert Kotlin maps/lists to Java maps/lists but DO NOT convert date/time types
    // TBD: Is this even needed? Kotlin types map to JDK types at runtime.
    fun toJavaObject(obj: Any?): Any? = when (obj) {
        is Map<*, *> -> HashMap<String, Any?>().apply { obj.forEach { (k, v) -> this[k.toString()] = toJavaObject(v) } }
        is Collection<*> -> ArrayList(obj.map { toJavaObject(it) })
        else -> obj
    }

    ///val javaModel = toJavaObject(modelMap) as? Map<*, *> ?: modelMap
    val javaModel = modelMap

    val template = cfg.getTemplate(templateName)
    val writer = StringWriter()
    template.process(javaModel, writer)
    return writer.toString()
}

