package org.jbake.template

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldContain
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.ConfigUtil
import org.jbake.model.DocumentModel
import org.jbake.template.model.RenderContext
import java.io.File
import java.nio.file.Files
import java.time.*
import java.util.*

// Added by AI, possibly dumb; TODO: Review and reduce / adjust.

class FreemarkerThroughJBakeDateTypesTest : StringSpec({
    "render template via JBake Renderer with various date types" {
        val root = Files.createTempDirectory("jbake-test-fm").toFile()
        val templates = File(root, "templates").apply { mkdirs() }
        val contentDir = File(root, "content").apply { mkdirs() }
        val outputDir = File(root, "output").apply { mkdirs() }

        // Write minimal jbake.properties
        val props = File(root, "jbake.properties")
        props.writeText("template.folder=templates\ncontent.folder=content\ndestination.folder=output\n")

        // Create Freemarker template that formats different date/time types from content map
        val templateFile = File(templates, "test.ftl")
        templateFile.writeText($$"""
            ${content.odt?string('yyyy-MM-dd HH:mm:ssXXX')}
            ${content.zdt?string('yyyy-MM-dd HH:mm:ss Z')}
            ${content.ldt?string('yyyy-MM-dd HH:mm')}
            ${content.ld?string('yyyy-MM-dd')}
            ${content.inst?string('yyyy-MM-dd')}
            ${content.ud?string('yyyy-MM-dd')}
            ${content.sqld?string('yyyy-MM-dd')}
            """.trimIndent()
        )

        val config = ConfigUtil().loadConfig(root)
        val cs = ContentStore("memory", "documents-${System.currentTimeMillis()}")
        val renderer = Renderer(cs, config)

        // Prepare a DocumentModel with various date/time types as attributes.
        val doc = DocumentModel().apply {
            type = "post"
            uri = "date-test.html"
            put("odt", OffsetDateTime.of(2025,12,10,14,30,0,0, ZoneOffset.ofHours(1)))
            put("zdt", ZonedDateTime.of(2025,12,10,14,30,0,0, ZoneId.of("Europe/Prague")))
            put("ldt", LocalDateTime.of(2025,12,10,8,15,5))
            put("ld", LocalDate.of(2025,12,10))
            val instant = Instant.parse("2025-12-10T12:00:00Z")
            put("inst", instant)
            put("ud", Date.from(instant))
            put("sqld", java.sql.Date.from(instant))
        }

        val context = RenderContext(config, cs, doc)
        val outFile = File(outputDir, "date-test.html")
        renderer.renderWithContext(context, outFile, "test.ftl")

        val rendered = outFile.readText()
        rendered.shouldContain("2025-12-10")
        // Time component may vary by zone, check at least one of them.
        (rendered.contains("14:30") || rendered.contains("09:00") || rendered.contains("08:15")).shouldBeTrue()
    }
})
