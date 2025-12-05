package org.jbake.template

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.parser.Parser
import java.io.StringWriter
import java.nio.file.Files

/**
 * Minimal test case to verify Freemarker handles missing author field correctly.
 * Parses an Asciidoc document WITHOUT an author, then renders it with a Freemarker template that references `${content.author}`.
 */
class FreemarkerMissingAuthorTest : StringSpec({

    "should render Asciidoc document without author using Freemarker template with author reference" {
        // Create temp directory
        val tempDir = Files.createTempDirectory("freemarker-test-").toFile()
        try {
            // 1. Create Asciidoc document WITHOUT author (only title and date)
            val asciidocContent = """
                = Test Document
                2024-01-01
                :jbake-type: post
                :jbake-status: published

                This is a test document without an author.
            """.trimIndent()

            val docFile = tempDir.resolve("test.adoc")
            docFile.writeText(asciidocContent)

            // 2. Create Freemarker template that references author
            val templateContent = $$"""
                <html>
                    <head><title>${content.title}</title></head>
                    <body>
                        <h1>${content.title}</h1>
                        <p>Author: ${content.author!"Anonymous"}</p>
                        <p>Date: ${content.date}</p>
                        <div>${content.body}</div>
                    </body>
                </html>
            """.trimIndent()

            val templateFile = tempDir.resolve("post.ftl")
            templateFile.writeText(templateContent)

            // 3. Parse Asciidoc document
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) //as DefaultJBakeConfiguration
            val parser = Parser(config)
            val documentModel = parser.processFile(docFile)!!

            // Verify document was parsed (should NOT have author field)
            documentModel.title shouldBe "Test Document"
            documentModel.containsKey("author") shouldBe true // Should have default author now!

            // 4. Render with Freemarker
            val freemarkerConfig = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
            freemarkerConfig.setDirectoryForTemplateLoading(tempDir)
            freemarkerConfig.defaultEncoding = "UTF-8"
            freemarkerConfig.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

            val template = freemarkerConfig.getTemplate("post.ftl")
            val writer = StringWriter()

            // Create model with document
            val model = mapOf("content" to documentModel)

            // THIS IS WHERE IT SHOULD FAIL if Freemarker doesn't handle missing author
            template.process(model, writer)

            val output = writer.toString()

            // Verify output
            output shouldContain "<h1>Test Document</h1>"
            output shouldContain "Author: Anonymous" // Should use default
            output shouldContain "This is a test document"
            output shouldNotContain "\${content.author}" // Template variable should be resolved
        }
        finally {
            tempDir.deleteRecursively()
        }
    }

    "should demonstrate the actual failure with missing author" {
        // Create temp directory
        val tempDir = Files.createTempDirectory("freemarker-fail-test-").toFile()
        try {
            // Document with NO author
            val asciidocContent = """
                = No Author Document
                2024-01-01
                :jbake-type: post
                :jbake-status: published

                Content here.
            """.trimIndent()

            val contentFile = tempDir.resolve("test.adoc")
            contentFile.writeText(asciidocContent)

            // Template that unconditionally accesses author (like jbake.org templates)
            val templateContent = $$"<p>By: ${content.author}</p>"

            val templateFile = tempDir.resolve("post.ftl")
            templateFile.writeText(templateContent)

            // Parse
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            val parser = Parser(config)
            val documentModel = parser.processFile(contentFile)!!

            // Render
            val freemarkerConfig = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
            freemarkerConfig.setDirectoryForTemplateLoading(tempDir)
            freemarkerConfig.defaultEncoding = "UTF-8"
            freemarkerConfig.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

            val template = freemarkerConfig.getTemplate("post.ftl")
            val writer = StringWriter()
            val model = mapOf("content" to documentModel)

            // This SHOULD throw InvalidReferenceException
            val result = kotlin.runCatching {
                template.process(model, writer)
            }

            // Verify it failed
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldContain "author"

        } finally {
            tempDir.deleteRecursively()
        }
    }
})

