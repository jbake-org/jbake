package org.jbake.parser

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import java.nio.file.Files

/**
 * Test to verify that the author attribute from Asciidoc documents
 * is correctly parsed from the document header (line 2).
 */
class AsciidoctorAuthorTest : FunSpec({

    test("should parse author from Asciidoc header line 2") {
        val tempDir = Files.createTempDirectory("jbake-author-test").toFile()
        try {
            val docFile = tempDir.resolve("test-post.adoc")
            docFile.writeText("""
                = Test Post Title
                John Doe
                2023-12-01
                :jbake-type: post
                :jbake-status: published

                This is the body of the post.
            """.trimIndent())

            // Parse the document using Parser (which internally uses AsciidoctorEngine)
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)

            // Verify author is present in parsed document
            println("Parsed document keys: ${parsedDoc!!.keys}")
            println("Author value: ${parsedDoc["author"]}")

            parsedDoc shouldContainKey "author"
            parsedDoc["author"] shouldBe "John Doe"
        }
        finally { tempDir.deleteRecursively() }
    }

    test("should parse author with email from Asciidoc header") {
        val tempDir = Files.createTempDirectory("jbake-author-email-test").toFile()
        try {
            val docFile = tempDir.resolve("test-post-email.adoc")
            docFile.writeText("""
                = Another Test Post
                Jane Smith <jane@example.com>
                2023-12-04
                :jbake-type: post
                :jbake-status: published

                Content goes here.
            """.trimIndent())

            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)

            println("Parsed document keys: ${parsedDoc!!.keys}")
            println("Author: ${parsedDoc["author"]}")
            println("Email: ${parsedDoc["email"]}")

            // Asciidoctor should parse both author name and email
            parsedDoc shouldContainKey "author"
            parsedDoc["author"] shouldBe "Jane Smith"

            // Email should also be extracted
            if (parsedDoc.containsKey("email")) parsedDoc["email"] shouldBe "jane@example.com"


        } finally {
            tempDir.deleteRecursively()
        }
    }
})

