package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import java.io.File
import java.io.PrintWriter

class MdParserTest : StringSpec({
    lateinit var tempDir: File

    lateinit var config: DefaultJBakeConfiguration

    lateinit var validMdFileBasic: File

    lateinit var invalidMdFileBasic: File

    lateinit var mdFileHardWraps: File

    lateinit var mdFileAbbreviations: File

    lateinit var mdFileAutolinks: File

    lateinit var mdFileDefinitions: File

    lateinit var mdFileFencedCodeBlocks: File

    lateinit var mdFileQuotes: File

    lateinit var mdFileSmarts: File

    lateinit var mdFileSmartypants: File

    lateinit var mdFileSuppressAllHTML: File

    lateinit var mdFileSuppressHTMLBlocks: File

    lateinit var mdFileSuppressInlineHTML: File

    lateinit var mdFileTables: File

    lateinit var mdFileWikilinks: File

    lateinit var mdFileAtxheaderspace: File

    lateinit var mdFileForcelistitempara: File

    lateinit var mdFileRelaxedhrules: File

    lateinit var mdTasklistitems: File

    lateinit var mdExtanchorlinks: File

    val validHeader = "title=Title\nstatus=draft\ntype=post\n~~~~~~"

    val invalidHeader = "title=Title\n~~~~~~"

    beforeTest {
        tempDir = java.nio.file.Files.createTempDirectory("jbake-test").toFile()
        val configFile = TestUtils.testResourcesAsSourceDir
        config = ConfigUtil().loadConfig(configFile) as DefaultJBakeConfiguration

        validMdFileBasic = File(tempDir, "validBasic.md").apply { createNewFile() }
        var out = PrintWriter(validMdFileBasic)
        out.println(validHeader)
        out.println("# This is a test")
        out.close()

        invalidMdFileBasic = File(tempDir, "invalidBasic.md").apply { createNewFile() }
        out = PrintWriter(invalidMdFileBasic)
        out.println(invalidHeader)
        out.println("# This is a test")
        out.close()

        mdFileHardWraps = File(tempDir, "hardWraps.md").apply { createNewFile() }
        out = PrintWriter(mdFileHardWraps)
        out.println(validHeader)
        out.println("First line")
        out.println("Second line")
        out.close()

        mdFileAbbreviations = File(tempDir, "abbreviations.md").apply { createNewFile() }
        out = PrintWriter(mdFileAbbreviations)
        out.println(validHeader)
        out.println("*[HTML]: Hyper Text Markup Language")
        out.println("HTML")
        out.close()

        mdFileAutolinks = File(tempDir, "autolinks.md").apply { createNewFile() }
        out = PrintWriter(mdFileAutolinks)
        out.println(validHeader)
        out.println("http://github.com")
        out.close()

        mdFileDefinitions = File(tempDir, "definitions.md").apply { createNewFile() }
        out = PrintWriter(mdFileDefinitions)
        out.println(validHeader)
        out.println("Apple")
        out.println(":   Pomaceous fruit")
        out.close()

        mdFileFencedCodeBlocks = File(tempDir, "fencedCodeBlocks.md").apply { createNewFile() }
        out = PrintWriter(mdFileFencedCodeBlocks)
        out.println(validHeader)
        out.println("```")
        out.println("function test() {")
        out.println("  console.log(\"!\");")
        out.println("}")
        out.println("```")
        out.close()

        mdFileQuotes = File(tempDir, "quotes.md").apply { createNewFile() }
        out = PrintWriter(mdFileQuotes)
        out.println(validHeader)
        out.println("\"quotes\"")
        out.close()

        mdFileSmarts = File(tempDir, "smarts.md").apply { createNewFile() }
        out = PrintWriter(mdFileSmarts)
        out.println(validHeader)
        out.println("...")
        out.close()

        mdFileSmartypants = File(tempDir, "smartypants.md").apply { createNewFile() }
        out = PrintWriter(mdFileSmartypants)
        out.println(validHeader)
        out.println("\"...\"")
        out.close()

        mdFileSuppressAllHTML = File(tempDir, "suppressAllHTML.md").apply { createNewFile() }
        out = PrintWriter(mdFileSuppressAllHTML)
        out.println(validHeader)
        out.println("<div>!</div><em>!</em>")
        out.close()

        mdFileSuppressHTMLBlocks = File(tempDir, "suppressHTMLBlocks.md").apply { createNewFile() }
        out = PrintWriter(mdFileSuppressHTMLBlocks)
        out.println(validHeader)
        out.println("<div>!</div><em>!</em>")
        out.close()

        mdFileSuppressInlineHTML = File(tempDir, "suppressInlineHTML.md").apply { createNewFile() }
        out = PrintWriter(mdFileSuppressInlineHTML)
        out.println(validHeader)
        out.println("This is the first paragraph. <span> with </span> inline html")
        out.close()

        mdFileTables = File(tempDir, "tables.md").apply { createNewFile() }
        out = PrintWriter(mdFileTables)
        out.println(validHeader)
        out.println("First Header|Second Header")
        out.println("-------------|-------------")
        out.println("Content Cell|Content Cell")
        out.println("Content Cell|Content Cell")
        out.close()

        mdFileWikilinks = File(tempDir, "wikilinks.md").apply { createNewFile() }
        out = PrintWriter(mdFileWikilinks)
        out.println(validHeader)
        out.println("[[Wiki-style links]]")
        out.close()

        mdFileAtxheaderspace = File(tempDir, "atxheaderspace.md").apply { createNewFile() }
        out = PrintWriter(mdFileAtxheaderspace)
        out.println(validHeader)
        out.println("#Test")
        out.close()

        mdFileForcelistitempara = File(tempDir, "forcelistitempara.md").apply { createNewFile() }
        out = PrintWriter(mdFileForcelistitempara)
        out.println(validHeader)
        out.println("1. Item 1")
        out.println("Item 1 lazy continuation")
        out.println("")
        out.println("    Item 1 paragraph 1")
        out.println("Item 1 paragraph 1 lazy continuation")
        out.println("    Item 1 paragraph 1 continuation")
        out.close()

        mdFileRelaxedhrules = File(tempDir, "releaxedhrules.md").apply { createNewFile() }
        out = PrintWriter(mdFileRelaxedhrules)
        out.println(validHeader)
        out.println("Hello World")
        out.println("---")
        out.println("***")
        out.println("___")
        out.println("")
        out.println("Hello World")
        out.println("***")
        out.println("---")
        out.println("___")
        out.println("")
        out.println("Hello World")
        out.println("___")
        out.println("---")
        out.println("***")
        out.close()

        mdTasklistitems = File(tempDir, "tasklistsitem.md").apply { createNewFile() }
        out = PrintWriter(mdTasklistitems)
        out.println(validHeader)
        out.println("* loose bullet item 3")
        out.println("* [ ] open task item")
        out.println("* [x] closed task item")
        out.close()

        mdExtanchorlinks = File(tempDir, "mdExtanchorlinks.md").apply { createNewFile() }
        out = PrintWriter(mdExtanchorlinks)
        out.println(validHeader)
        out.println("# header & some *formatting* ~~chars~~")
        out.close()
    }

    "parseValidMarkdownFileBasic" {
        val parser = Parser(config)
        val documentModel = parser.processFile(validMdFileBasic)
        documentModel.shouldNotBeNull()
        documentModel.status shouldBe "draft"
        documentModel.type shouldBe "post"
        documentModel.body shouldBe "<h1>This is a test</h1>\n"
    }

    "parseInvalidMarkdownFileBasic" {
        val parser = Parser(config)
        val documentModel = parser.processFile(invalidMdFileBasic)
        documentModel.shouldBeNull()
    }

    "parseValidMdFileHardWraps" {
        config.setMarkdownExtensions("HARDWRAPS")

        // Test with HARDWRAPS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileHardWraps)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>First line<br />\nSecond line</p>\n"

        // Test without HARDWRAPS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileHardWraps)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>First line Second line</p>"
    }

    "parseWithInvalidExtension" {
        config.setMarkdownExtensions("HARDWRAPS,UNDEFINED_EXTENSION")

        // Test with HARDWRAPS
        val parser = Parser(config)
        val documentModel = parser.processFile(mdFileHardWraps)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>First line<br />\nSecond line</p>\n"
    }

    "parseValidMdFileAbbreviations" {
        config.setMarkdownExtensions("ABBREVIATIONS")

        // Test with ABBREVIATIONS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileAbbreviations)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p><abbr title=\"Hyper Text Markup Language\">HTML</abbr></p>"

        // Test without ABBREVIATIONS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileAbbreviations)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>*[HTML]: Hyper Text Markup Language HTML</p>"
    }

    "parseValidMdFileAutolinks" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("AUTOLINKS")

        // Test with AUTOLINKS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileAutolinks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p><a href=\"http://github.com\">http://github.com</a></p>"

        // Test without AUTOLINKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileAutolinks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>http://github.com</p>"
    }

    "parseValidMdFileDefinitions" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("DEFINITIONS")

        // Test with DEFINITIONS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileDefinitions)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<dl>\n<dt>Apple</dt>\n<dd>Pomaceous fruit</dd>\n</dl>"


        // Test without DEFNITIONS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileDefinitions)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>Apple :   Pomaceous fruit</p>"
    }

    "parseValidMdFileFencedCodeBlocks" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("FENCED_CODE_BLOCKS")

        // Test with FENCED_CODE_BLOCKS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileFencedCodeBlocks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<pre><code>function test() {\n  console.log(&quot;!&quot;);\n}\n</code></pre>"

        // Test without FENCED_CODE_BLOCKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileFencedCodeBlocks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p><code>function test() { console.log(&quot;!&quot;); }</code></p>"
    }

    "parseValidMdFileQuotes" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("QUOTES")

        // Test with QUOTES
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileQuotes)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&ldquo;quotes&rdquo;</p>"

        // Test without QUOTES
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileQuotes)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&quot;quotes&quot;</p>"
    }

    "parseValidMdFileSmarts" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SMARTS")

        // Test with SMARTS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSmarts)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&hellip;</p>"

        // Test without SMARTS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSmarts)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>...</p>"
    }

    "parseValidMdFileSmartypants" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SMARTYPANTS")

        // Test with SMARTYPANTS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSmartypants)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&ldquo;&hellip;&rdquo;</p>"

        // Test without SMARTYPANTS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSmartypants)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>&quot;...&quot;</p>"
    }

    "parseValidMdFileSuppressAllHTML" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SUPPRESS_ALL_HTML")

        // Test with SUPPRESS_ALL_HTML
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSuppressAllHTML)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain ""

        // Test without SUPPRESS_ALL_HTML
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSuppressAllHTML)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<div>!</div><em>!</em>"
    }

    "parseValidMdFileSuppressHTMLBlocks" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SUPPRESS_HTML_BLOCKS")

        // Test with SUPPRESS_HTML_BLOCKS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSuppressHTMLBlocks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain ""

        // Test without SUPPRESS_HTML_BLOCKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSuppressHTMLBlocks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<div>!</div><em>!</em>"
    }

    "parseValidMdFileSuppressInlineHTML" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SUPPRESS_INLINE_HTML")

        // Test with SUPPRESS_INLINE_HTML
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSuppressInlineHTML)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>This is the first paragraph.  with  inline html</p>"

        // Test without SUPPRESS_INLINE_HTML
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSuppressInlineHTML)
        documentModel.shouldNotBeNull()
        documentModel.body
            .contains("<p>This is the first paragraph. <span> with </span> inline html</p>")
    }

    "parseValidMdFileTables" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("TABLES")

        // Test with TABLES
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileTables)
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
        documentModel = parser.processFile(mdFileTables)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<p>First Header|Second Header -------------|------------- Content Cell|Content Cell Content Cell|Content Cell</p>"

    }

    "parseValidMdFileWikilinks" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("WIKILINKS")

        // Test with WIKILINKS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileWikilinks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<p><a href=\"Wiki-style-links\">Wiki-style links</a></p>"


        // Test without WIKILINKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileWikilinks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>[[Wiki-style links]]</p>"
    }

    "parseValidMdFileAtxheaderspace" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("ATXHEADERSPACE")

        // Test with ATXHEADERSPACE
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileAtxheaderspace)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<p>#Test</p>"

        // Test without ATXHEADERSPACE
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileAtxheaderspace)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<h1>Test</h1>"
    }

    "parseValidMdFileForcelistitempara" {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("FORCELISTITEMPARA")

        // Test with FORCELISTITEMPARA
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileForcelistitempara)
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
        documentModel = parser.processFile(mdFileForcelistitempara)
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
        var documentModel = parser.processFile(mdFileRelaxedhrules)
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
        documentModel = parser.processFile(mdFileRelaxedhrules)
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
        var documentModel = parser.processFile(mdTasklistitems)
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
        documentModel = parser.processFile(mdTasklistitems)
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
        var documentModel = parser.processFile(mdExtanchorlinks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain
            "<h1><a href=\"#header-some-formatting-chars\" id=\"header-some-formatting-chars\"></a>header &amp; some <em>formatting</em> ~~chars~~</h1>"


        // Test without EXTANCHORLINKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdExtanchorlinks)
        documentModel.shouldNotBeNull()
        documentModel.body shouldContain "<h1>header &amp; some <em>formatting</em> ~~chars~~</h1>"
    }


    afterTest {
        tempDir.deleteRecursively()
    }
})
