package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.parser.Parser
import java.io.File

class MdParserTest : StringSpec({
    lateinit var tempDir: File

    lateinit var config: DefaultJBakeConfiguration

    val validHeader = "title=Title\nstatus=draft\ntype=post\n~~~~~~"

    val invalidHeader = "title=Title\n~~~~~~"

    beforeTest {
        tempDir = java.nio.file.Files.createTempDirectory("jbake-test").toFile()
        val configFile = TestUtils.testResourcesAsSourceDir
        config = ConfigUtil().loadConfig(configFile) as DefaultJBakeConfiguration

        "validBasic.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |# This is a test""".trimMargin())

        "invalidBasic.md".let { tempDir.resolve(it) }.writeText("""
            |$invalidHeader
            |# This is a test""".trimMargin())

        "hardWraps.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |First line
            |Second line""".trimMargin())

        "abbreviations.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |*[HTML]: Hyper Text Markup Language
            |HTML""".trimMargin())

        "autolinks.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |http://github.com""".trimMargin())

        "definitions.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |Apple
            |:   Pomaceous fruit""".trimMargin())

        "fencedCodeBlocks.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |```
            |function test() {
            |  console.log("!");
            |}
            |```""".trimMargin())

        "quotes.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |"quotes"""".trimMargin())

        "smarts.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |...""".trimMargin())

        "smartypants.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |"..."""".trimMargin())

        "suppressAllHTML.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |<div>!</div><em>!</em>""".trimMargin())

        "suppressHTMLBlocks.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |<div>!</div><em>!</em>""".trimMargin())

        "suppressInlineHTML.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |This is the first paragraph. <span> with </span> inline html""".trimMargin())

        "tables.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |First Header|Second Header
            |-------------|-------------
            |Content Cell|Content Cell
            |Content Cell|Content Cell""".trimMargin())

        "wikilinks.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |[[Wiki-style links]]""".trimMargin())

        "atxheaderspace.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |#Test""".trimMargin())

        "forcelistitempara.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |1. Item 1
            |Item 1 lazy continuation
            |
            |    Item 1 paragraph 1
            |Item 1 paragraph 1 lazy continuation
            |    Item 1 paragraph 1 continuation""".trimMargin())

        "releaxedhrules.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |Hello World
            |---
            |***
            |___
            |
            |Hello World
            |***
            |---
            |___
            |
            |Hello World
            |___
            |---
            |***""".trimMargin())

        "tasklistsitem.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |* loose bullet item 3
            |* [ ] open task item
            |* [x] closed task item""".trimMargin())

        "mdExtanchorlinks.md".let { tempDir.resolve(it) }.writeText("""
            |$validHeader
            |# header & some *formatting* ~~chars~~""".trimMargin())
    }

    "parseValidMarkdownFileBasic" {
        val parser = Parser(config)
        val documentModel = parser.processFile(tempDir.resolve("validBasic.md"))
        documentModel.shouldNotBeNull()
        documentModel.status shouldBe "draft"
        documentModel.type shouldBe "post"
        documentModel.body shouldBe "<h1>This is a test</h1>\n"
    }

    "parseInvalidMarkdownFileBasic" {
        val parser = Parser(config)
        val documentModel = parser.processFile(tempDir.resolve("invalidBasic.md"))
        documentModel.shouldBeNull()
    }

    "parseValidMdFileHardWraps" {
        config.setMarkdownExtensions("HARDWRAPS")

        // Test with HARDWRAPS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("hardWraps.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>First line<br />\nSecond line</p>\n"

        // Test without HARDWRAPS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("hardWraps.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>First line Second line</p>"
    }

    "parseWithInvalidExtension" {
        config.setMarkdownExtensions("HARDWRAPS,UNDEFINED_EXTENSION")

        // Test with HARDWRAPS
        val parser = Parser(config)
        val documentModel = parser.processFile(tempDir.resolve("hardWraps.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>First line<br />\nSecond line</p>\n"
    }

    "parseValidMdFileAbbreviations" {
        config.setMarkdownExtensions("ABBREVIATIONS")

        // Test with ABBREVIATIONS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("abbreviations.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p><abbr title=\"Hyper Text Markup Language\">HTML</abbr></p>"

        // Test without ABBREVIATIONS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("abbreviations.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>*[HTML]: Hyper Text Markup Language HTML</p>"
    }

    "parseValidMdFileAutolinks" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("AUTOLINKS")

        // Test with AUTOLINKS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("autolinks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p><a href=\"http://github.com\">http://github.com</a></p>"

        // Test without AUTOLINKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("autolinks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>http://github.com</p>"
    }

    "parseValidMdFileDefinitions" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("DEFINITIONS")

        // Test with DEFINITIONS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("definitions.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<dl>\n<dt>Apple</dt>\n<dd>Pomaceous fruit</dd>\n</dl>"


        // Test without DEFNITIONS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("definitions.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>Apple :   Pomaceous fruit</p>"
    }

    "parseValidMdFileFencedCodeBlocks" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("FENCED_CODE_BLOCKS")

        // Test with FENCED_CODE_BLOCKS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("fencedCodeBlocks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<pre><code>function test() {\n  console.log(&quot;!&quot;);\n}\n</code></pre>"

        // Test without FENCED_CODE_BLOCKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("fencedCodeBlocks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p><code>function test() { console.log(&quot;!&quot;); }</code></p>"
    }

    "parseValidMdFileQuotes" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("QUOTES")

        // Test with QUOTES
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("quotes.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&ldquo;quotes&rdquo;</p>"

        // Test without QUOTES
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("quotes.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&quot;quotes&quot;</p>"
    }

    "parseValidMdFileSmarts" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SMARTS")

        // Test with SMARTS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("smarts.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&hellip;</p>"

        // Test without SMARTS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("smarts.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>...</p>"
    }

    "parseValidMdFileSmartypants" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SMARTYPANTS")

        // Test with SMARTYPANTS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("smartypants.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&ldquo;&hellip;&rdquo;</p>"

        // Test without SMARTYPANTS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("smartypants.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&quot;...&quot;</p>"
    }

    "parseValidMdFileSuppressAllHTML" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SUPPRESS_ALL_HTML")

        // Test with SUPPRESS_ALL_HTML
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("suppressAllHTML.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain ""

        // Test without SUPPRESS_ALL_HTML
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("suppressAllHTML.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<div>!</div><em>!</em>"
    }

    "parseValidMdFileSuppressHTMLBlocks" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SUPPRESS_HTML_BLOCKS")

        // Test with SUPPRESS_HTML_BLOCKS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("suppressHTMLBlocks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain ""

        // Test without SUPPRESS_HTML_BLOCKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("suppressHTMLBlocks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<div>!</div><em>!</em>"
    }

    "parseValidMdFileSuppressInlineHTML" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SUPPRESS_INLINE_HTML")

        // Test with SUPPRESS_INLINE_HTML
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("suppressInlineHTML.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>This is the first paragraph.  with  inline html</p>"

        // Test without SUPPRESS_INLINE_HTML
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("suppressInlineHTML.md"))
        documentModel.shouldNotBeNull()
        documentModel.body
            .contains("<p>This is the first paragraph. <span> with </span> inline html</p>")
    }

    "parseValidMdFileTables" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("TABLES")

        // Test with TABLES
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("tables.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<table>\n" +
                    "<thead>\n" +
                    "<tr><th>First Header</th><th>Second Header</th></tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr><td>Content Cell</td><td>Content Cell</td></tr>\n" +
                    "<tr><td>Content Cell</td><td>Content Cell</td></tr>\n" +
                    "</tbody>\n" +
                    "</table>"


        // Test without TABLES
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("tables.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<p>First Header|Second Header -------------|------------- Content Cell|Content Cell Content Cell|Content Cell</p>"

    }

    "parseValidMdFileWikilinks" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("WIKILINKS")

        // Test with WIKILINKS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("wikilinks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<p><a href=\"Wiki-style-links\">Wiki-style links</a></p>"


        // Test without WIKILINKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("wikilinks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>[[Wiki-style links]]</p>"
    }

    "parseValidMdFileAtxheaderspace" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("ATXHEADERSPACE")

        // Test with ATXHEADERSPACE
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("atxheaderspace.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>#Test</p>"

        // Test without ATXHEADERSPACE
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("atxheaderspace.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<h1>Test</h1>"
    }

    "parseValidMdFileForcelistitempara" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("FORCELISTITEMPARA")

        // Test with FORCELISTITEMPARA
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("forcelistitempara.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<ol>\n" +
                    "<li>\n" +
                    "<p>Item 1 Item 1 lazy continuation</p>\n" +
                    "<p>Item 1 paragraph 1 Item 1 paragraph 1 lazy continuation Item 1 paragraph 1 continuation</p>\n" +
                    "</li>\n" +
                    "</ol>"


        // Test without FORCELISTITEMPARA
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("forcelistitempara.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<ol>\n" +
                    "<li>Item 1 Item 1 lazy continuation\n" +
                    "<p>Item 1 paragraph 1 Item 1 paragraph 1 lazy continuation Item 1 paragraph 1 continuation</p>\n" +
                    "</li>\n" +
                    "</ol>"

    }

    "parseValidMdFileRelaxedhrules" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("RELAXEDHRULES")

        // Test with RELAXEDHRULES
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("releaxedhrules.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<h2>Hello World</h2>\n" +
                    "<hr />\n" +
                    "<hr />\n" +
                    "<p>Hello World</p>\n" +
                    "<hr />\n" +
                    "<hr />\n" +
                    "<hr />\n" +
                    "<p>Hello World</p>\n" +
                    "<hr />\n" +
                    "<hr />\n" +
                    "<hr />"


        // Test without RELAXEDHRULES
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("releaxedhrules.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<h2>Hello World</h2>\n" +
                    "<hr />\n" +
                    "<hr />\n" +
                    "<h2>Hello World ***</h2>\n" +
                    "<hr />\n" +
                    "<h2>Hello World ___</h2>\n" +
                    "<hr />"

    }

    "parseValidMdFileTasklistitems" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("TASKLISTITEMS")

        // Test with TASKLISTITEMS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("tasklistsitem.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<ul>\n" +
                    "<li>loose bullet item 3</li>\n" +
                    "<li class=\"task-list-item\"><input type=\"checkbox\" class=\"task-list-item-checkbox\" disabled=\"disabled\" readonly=\"readonly\" />&nbsp;open task item</li>\n" +
                    "<li class=\"task-list-item\"><input type=\"checkbox\" class=\"task-list-item-checkbox\" checked=\"checked\" disabled=\"disabled\" readonly=\"readonly\" />&nbsp;closed task item</li>\n" +
                    "</ul>"


        // Test without TASKLISTITEMS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("tasklistsitem.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<ul>\n" +
                    "<li>loose bullet item 3</li>\n" +
                    "<li>[ ] open task item</li>\n" +
                    "<li>[x] closed task item</li>\n" +
                    "</ul>"

    }

    "parseValidMdFileExtanchorlinks" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("EXTANCHORLINKS")

        // Test with EXTANCHORLINKS
        var parser = Parser(config)
        var documentModel = parser.processFile(tempDir.resolve("mdExtanchorlinks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<h1><a href=\"#header-some-formatting-chars\" id=\"header-some-formatting-chars\"></a>header &amp; some <em>formatting</em> ~~chars~~</h1>"


        // Test without EXTANCHORLINKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(tempDir.resolve("mdExtanchorlinks.md"))
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<h1>header &amp; some <em>formatting</em> ~~chars~~</h1>"
    }


    afterTest {
        tempDir.deleteRecursively()
    }
})
