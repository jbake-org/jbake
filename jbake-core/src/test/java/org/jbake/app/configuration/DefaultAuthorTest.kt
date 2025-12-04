package org.jbake.app.configuration

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.jbake.TestUtils
import org.jbake.parser.Parser
import java.nio.file.Files

class DefaultAuthorTest : StringSpec({

    "should use default author when document has no author" {
        val tempDir = Files.createTempDirectory("jbake-default-author-test").toFile()
        try {
            // Create a document without author
            val docFile = tempDir.resolve("test-no-author.html")
            docFile.writeText("""
                title=Test Post
                date=2023-12-04
                type=post
                status=published
                ~~~~~~

                This is content without author.
            """.trimIndent())

            // Load config and set default author
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor("Default Author Name")

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)

            // Verify default author was applied
            parsedDoc!!["author"] shouldBe "Default Author Name"

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should not override explicit author with default" {
        val tempDir = Files.createTempDirectory("jbake-explicit-author-test").toFile()
        try {
            // Create a document WITH author
            val docFile = tempDir.resolve("test-with-author.html")
            docFile.writeText("""
                title=Test Post
                author=Explicit Author
                date=2023-12-04
                type=post
                status=published
                ~~~~~~

                This is content with author.
            """.trimIndent())

            // Load config and set default author
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor("Default Author Name")

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)

            // Verify explicit author is preserved
            parsedDoc!!["author"] shouldBe "Explicit Author"

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should use default author for Asciidoc documents without author header" {
        val tempDir = Files.createTempDirectory("jbake-asciidoc-default-author-test").toFile()
        try {
            // Create pure Asciidoc document without author (only title)
            val docFile = tempDir.resolve("test-asciidoc.adoc")
            docFile.writeText("""
                = Test Post Title
                :jbake-type: post
                :jbake-status: published
                :jbake-date: 2023-12-04

                This is an Asciidoc document without an author line.
            """.trimIndent())

            // Load config and set default author
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor("Asciidoc Default Author")

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)!!

            // Verify default author was applied
            parsedDoc["author"] shouldBe "Asciidoc Default Author"

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should use author from line 2 in Asciidoc native header format" {
        val tempDir = Files.createTempDirectory("jbake-asciidoc-native-author-test").toFile()
        try {
            // Create Asciidoc document with author on line 2 (standard Asciidoc format)
            val docFile = tempDir.resolve("test-asciidoc-native.adoc")
            docFile.writeText("""
                = Test Post Title
                Native Author Name
                2023-12-04
                :jbake-type: post
                :jbake-status: published

                This is an Asciidoc document with native author format.
            """.trimIndent())

            // Load config and set default author (should be ignored)
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor("Should Not Use This")

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)!!

            // Verify native author is used, not default
            parsedDoc["author"] shouldBe "Native Author Name"

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should handle empty author field in old JBake header" {
        val tempDir = Files.createTempDirectory("jbake-empty-author-test").toFile()
        try {
            // Create document with empty author field
            val docFile = tempDir.resolve("test-empty-author.html")
            docFile.writeText("""
                title=Test Post
                author=
                date=2023-12-04
                type=post
                status=published
                ~~~~~~

                This is content with empty author field.
            """.trimIndent())

            // Load config and set default author
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor("Default For Empty")

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)

            // Empty author field exists, so default should NOT be applied
            // The empty string should be preserved
            parsedDoc!!["author"] shouldBe ""

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should handle Asciidoc with both attribute and native author" {
        val tempDir = Files.createTempDirectory("jbake-double-author-test").toFile()
        try {
            // Create Asciidoc document with both :author: attribute and line 2 author
            val docFile = tempDir.resolve("test-double-author.adoc")
            docFile.writeText("""
                = Test Post Title
                Line Two Author
                2023-12-04
                :jbake-type: post
                :jbake-status: published
                :author: Attribute Author

                This has author in both places.
            """.trimIndent())

            // Load config
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor("Should Not Use This")

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)!!

            // :author: attribute overrides line 2 author in Asciidoctor
            // This is Asciidoctor's documented behavior - attributes override document header
            parsedDoc["author"] shouldBe "Attribute Author"

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should use default when no configuration is provided" {
        val tempDir = Files.createTempDirectory("jbake-no-default-test").toFile()
        try {
            // Create document without author
            val docFile = tempDir.resolve("test-no-default.html")
            docFile.writeText("""
                title=Test Post
                date=2023-12-04
                type=post
                status=published
                ~~~~~~

                This is content without author.
            """.trimIndent())

            // Load config WITHOUT setting default author
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            // Don't set default author

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)

            // No default, no author field -> should not have author key
            parsedDoc!!.containsKey("author") shouldBe false

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should handle Asciidoc with author attribute but no line 2 author" {
        val tempDir = Files.createTempDirectory("jbake-asciidoc-attr-only-test").toFile()
        try {
            // Create Asciidoc document with :author: attribute only
            val docFile = tempDir.resolve("test-attr-only.adoc")
            docFile.writeText("""
                = Test Post Title
                :jbake-type: post
                :jbake-status: published
                :jbake-date: 2023-12-04
                :author: Attribute Only Author

                This uses :author: attribute instead of line 2.
            """.trimIndent())

            // Load config with default author
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor("Should Not Use This")

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)!!

            // :author: attribute should be used
            parsedDoc["author"] shouldBe "Attribute Only Author"

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should handle whitespace-only author field" {
        val tempDir = Files.createTempDirectory("jbake-whitespace-author-test").toFile()
        try {
            // Create document with whitespace-only author field
            val docFile = tempDir.resolve("test-whitespace.html")
            docFile.writeText("""
                title=Test Post
                author=
                date=2023-12-04
                type=post
                status=published
                ~~~~~~

                This is content with whitespace author.
            """.trimIndent())

            // Load config and set default author
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor("Default For Whitespace")

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)

            // Whitespace gets trimmed, becomes empty string
            // Since key exists (even if empty), default should NOT be applied
            parsedDoc!!["author"] shouldBe ""

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should handle Asciidoc with email on line 2" {
        val tempDir = Files.createTempDirectory("jbake-asciidoc-email-test").toFile()
        try {
            // Create Asciidoc document with author and email on line 2
            val docFile = tempDir.resolve("test-email.adoc")
            docFile.writeText("""
                = Test Post Title
                John Doe <john@example.com>
                2023-12-04
                :jbake-type: post
                :jbake-status: published

                This has author with email.
            """.trimIndent())

            // Load config with default author
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor("Should Not Use This")

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)!!

            // Should extract author name
            parsedDoc["author"] shouldBe "John Doe"
            // Should also extract email
            parsedDoc["email"] shouldBe "john@example.com"

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should handle author with special characters in old JBake header" {
        val tempDir = Files.createTempDirectory("jbake-special-chars-test").toFile()
        try {
            // Create document with author containing various special characters
            val weirdAuthor = """O'Reilly & Sons "The Best" @company \path\to\file `backtick` [brackets] (parens) {braces} ${'$'}{template}"""
            val docFile = tempDir.resolve("test-special-chars.html")
            docFile.writeText("""
                title=Test Post
                author=$weirdAuthor
                date=2023-12-04
                type=post
                status=published
                ~~~~~~

                This is content with special characters in author.
            """.trimIndent())

            // Load config
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)

            // Verify special characters are preserved exactly as written
            // Old JBake header format preserves & as-is (no HTML encoding)
            parsedDoc!!["author"] shouldBe weirdAuthor

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should handle author with special characters in Asciidoc attribute" {
        val tempDir = Files.createTempDirectory("jbake-asciidoc-special-chars-test").toFile()
        try {
            // Create Asciidoc document with special characters in :author: attribute
            val weirdAuthor = """O'Reilly & Co. "Quotes" @mention \backslash `code` [link] (note) {var} ${'$'}{x}"""
            val docFile = tempDir.resolve("test-asciidoc-special.adoc")
            docFile.writeText("""
                = Test Post Title
                :jbake-type: post
                :jbake-status: published
                :jbake-date: 2023-12-04
                :author: $weirdAuthor

                This has special characters in author attribute.
            """.trimIndent())

            // Load config
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)!!

            // Verify special characters are preserved
            // Asciidoctor converts & to &amp; in attribute values
            parsedDoc["author"] shouldBe weirdAuthor.replace("&", "&amp;")

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should handle author with special characters on Asciidoc line 2" {
        val tempDir = Files.createTempDirectory("jbake-asciidoc-line2-special-test").toFile()
        try {
            // Create Asciidoc document with special characters on line 2
            // Note: Some characters may be interpreted by Asciidoctor, so we test practical cases
            val weirdAuthor = """O'Brien & Associates "The Publisher" @company"""
            val docFile = tempDir.resolve("test-line2-special.adoc")
            docFile.writeText("""
                = Test Post Title
                $weirdAuthor
                2023-12-04
                :jbake-type: post
                :jbake-status: published

                This has special characters in line 2 author.
            """.trimIndent())

            // Load config
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)!!

            // Verify special characters are preserved (Asciidoctor may normalize some)
            parsedDoc["author"] shouldBe weirdAuthor.replace("&", "&amp;")

        } finally {
            tempDir.deleteRecursively()
        }
    }

    "should handle default author with special characters" {
        val tempDir = Files.createTempDirectory("jbake-default-special-chars-test").toFile()
        try {
            // Create document without author
            val docFile = tempDir.resolve("test-default-special.html")
            docFile.writeText("""
                title=Test Post
                date=2023-12-04
                type=post
                status=published
                ~~~~~~

                This is content without author.
            """.trimIndent())

            // Load config and set default author with special characters
            val weirdDefaultAuthor = """Company's "Official" Author @2023 \n \t ${'$'}{var} [note] (tm)"""
            val rootPath = TestUtils.testResourcesAsSourceDir
            val config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
            config.setDefaultAuthor(weirdDefaultAuthor)

            // Parse document
            val parser = Parser(config)
            val parsedDoc = parser.processFile(docFile)

            // Verify default author with special characters is applied correctly
            parsedDoc!!["author"] shouldBe weirdDefaultAuthor

        } finally {
            tempDir.deleteRecursively()
        }
    }
})

