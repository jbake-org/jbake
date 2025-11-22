package org.jbake.app

import org.assertj.core.api.Assertions
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.PrintWriter

/**
 * Tests basic Markdown syntax and the extensions supported by the Markdown
 * processor (Pegdown).
 *
 * @author Jonathan Bullock <jonbullock></jonbullock>@gmail.com>
 * @author Kevin S. Clarke <ksclarke></ksclarke>@gmail.com>
 */
class MdParserTest {
    @Rule @JvmField
    var folder: TemporaryFolder = TemporaryFolder()

    lateinit var config: DefaultJBakeConfiguration

    private lateinit var validMdFileBasic: File

    private lateinit var invalidMdFileBasic: File

    private lateinit var mdFileHardWraps: File

    private lateinit var mdFileAbbreviations: File

    private lateinit var mdFileAutolinks: File

    private lateinit var mdFileDefinitions: File

    private lateinit var mdFileFencedCodeBlocks: File

    private lateinit var mdFileQuotes: File

    private lateinit var mdFileSmarts: File

    private lateinit var mdFileSmartypants: File

    private lateinit var mdFileSuppressAllHTML: File

    private lateinit var mdFileSuppressHTMLBlocks: File

    private lateinit var mdFileSuppressInlineHTML: File

    private lateinit var mdFileTables: File

    private lateinit var mdFileWikilinks: File

    private lateinit var mdFileAtxheaderspace: File

    private lateinit var mdFileForcelistitempara: File

    private lateinit var mdFileRelaxedhrules: File

    private lateinit var mdTasklistitems: File

    private lateinit var mdExtanchorlinks: File

    private val validHeader = "title=Title\nstatus=draft\ntype=post\n~~~~~~"

    private val invalidHeader = "title=Title\n~~~~~~"

    @Before
    fun createSampleFile() {
        val configFile = TestUtils.testResourcesAsSourceFolder
        config = ConfigUtil().loadConfig(configFile) as DefaultJBakeConfiguration

        validMdFileBasic = folder.newFile("validBasic.md")
        var out = PrintWriter(validMdFileBasic)
        out.println(validHeader)
        out.println("# This is a test")
        out.close()

        invalidMdFileBasic = folder.newFile("invalidBasic.md")
        out = PrintWriter(invalidMdFileBasic)
        out.println(invalidHeader)
        out.println("# This is a test")
        out.close()

        mdFileHardWraps = folder.newFile("hardWraps.md")
        out = PrintWriter(mdFileHardWraps)
        out.println(validHeader)
        out.println("First line")
        out.println("Second line")
        out.close()

        mdFileAbbreviations = folder.newFile("abbreviations.md")
        out = PrintWriter(mdFileAbbreviations)
        out.println(validHeader)
        out.println("*[HTML]: Hyper Text Markup Language")
        out.println("HTML")
        out.close()

        mdFileAutolinks = folder.newFile("autolinks.md")
        out = PrintWriter(mdFileAutolinks)
        out.println(validHeader)
        out.println("http://github.com")
        out.close()

        mdFileDefinitions = folder.newFile("definitions.md")
        out = PrintWriter(mdFileDefinitions)
        out.println(validHeader)
        out.println("Apple")
        out.println(":   Pomaceous fruit")
        out.close()

        mdFileFencedCodeBlocks = folder.newFile("fencedCodeBlocks.md")
        out = PrintWriter(mdFileFencedCodeBlocks)
        out.println(validHeader)
        out.println("```")
        out.println("function test() {")
        out.println("  console.log(\"!\");")
        out.println("}")
        out.println("```")
        out.close()

        mdFileQuotes = folder.newFile("quotes.md")
        out = PrintWriter(mdFileQuotes)
        out.println(validHeader)
        out.println("\"quotes\"")
        out.close()

        mdFileSmarts = folder.newFile("smarts.md")
        out = PrintWriter(mdFileSmarts)
        out.println(validHeader)
        out.println("...")
        out.close()

        mdFileSmartypants = folder.newFile("smartypants.md")
        out = PrintWriter(mdFileSmartypants)
        out.println(validHeader)
        out.println("\"...\"")
        out.close()

        mdFileSuppressAllHTML = folder.newFile("suppressAllHTML.md")
        out = PrintWriter(mdFileSuppressAllHTML)
        out.println(validHeader)
        out.println("<div>!</div><em>!</em>")
        out.close()

        mdFileSuppressHTMLBlocks = folder.newFile("suppressHTMLBlocks.md")
        out = PrintWriter(mdFileSuppressHTMLBlocks)
        out.println(validHeader)
        out.println("<div>!</div><em>!</em>")
        out.close()

        mdFileSuppressInlineHTML = folder.newFile("suppressInlineHTML.md")
        out = PrintWriter(mdFileSuppressInlineHTML)
        out.println(validHeader)
        out.println("This is the first paragraph. <span> with </span> inline html")
        out.close()

        mdFileTables = folder.newFile("tables.md")
        out = PrintWriter(mdFileTables)
        out.println(validHeader)
        out.println("First Header|Second Header")
        out.println("-------------|-------------")
        out.println("Content Cell|Content Cell")
        out.println("Content Cell|Content Cell")
        out.close()

        mdFileWikilinks = folder.newFile("wikilinks.md")
        out = PrintWriter(mdFileWikilinks)
        out.println(validHeader)
        out.println("[[Wiki-style links]]")
        out.close()

        mdFileAtxheaderspace = folder.newFile("atxheaderspace.md")
        out = PrintWriter(mdFileAtxheaderspace)
        out.println(validHeader)
        out.println("#Test")
        out.close()

        mdFileForcelistitempara = folder.newFile("forcelistitempara.md")
        out = PrintWriter(mdFileForcelistitempara)
        out.println(validHeader)
        out.println("1. Item 1")
        out.println("Item 1 lazy continuation")
        out.println("")
        out.println("    Item 1 paragraph 1")
        out.println("Item 1 paragraph 1 lazy continuation")
        out.println("    Item 1 paragraph 1 continuation")
        out.close()

        mdFileRelaxedhrules = folder.newFile("releaxedhrules.md")
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

        mdTasklistitems = folder.newFile("tasklistsitem.md")
        out = PrintWriter(mdTasklistitems)
        out.println(validHeader)
        out.println("* loose bullet item 3")
        out.println("* [ ] open task item")
        out.println("* [x] closed task item")
        out.close()

        mdExtanchorlinks = folder.newFile("mdExtanchorlinks.md")
        out = PrintWriter(mdExtanchorlinks)
        out.println(validHeader)
        out.println("# header & some *formatting* ~~chars~~")
        out.close()
    }

    @Test
    fun parseValidMarkdownFileBasic() {
        val parser = Parser(config)
        val documentModel = parser.processFile(validMdFileBasic)
        Assert.assertNotNull(documentModel)
        Assert.assertEquals("draft", documentModel!!.status)
        Assert.assertEquals("post", documentModel.type)
        Assert.assertEquals("<h1>This is a test</h1>\n", documentModel.body)
    }

    @Test
    fun parseInvalidMarkdownFileBasic() {
        val parser = Parser(config)
        val documentModel = parser.processFile(invalidMdFileBasic)
        Assert.assertNull(documentModel)
    }

    @Test
    fun parseValidMdFileHardWraps() {
        config.setMarkdownExtensions("HARDWRAPS")

        // Test with HARDWRAPS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileHardWraps)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>First line<br />\nSecond line</p>\n")

        // Test without HARDWRAPS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileHardWraps)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>First line Second line</p>")
    }

    @Test
    fun parseWithInvalidExtension() {
        config.setMarkdownExtensions("HARDWRAPS,UNDEFINED_EXTENSION")

        // Test with HARDWRAPS
        val parser = Parser(config)
        val documentModel = parser.processFile(mdFileHardWraps)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>First line<br />\nSecond line</p>\n")
    }

    @Test
    fun parseValidMdFileAbbreviations() {
        config.setMarkdownExtensions("ABBREVIATIONS")

        // Test with ABBREVIATIONS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileAbbreviations)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<p><abbr title=\"Hyper Text Markup Language\">HTML</abbr></p>"
        )

        // Test without ABBREVIATIONS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileAbbreviations)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>*[HTML]: Hyper Text Markup Language HTML</p>")
    }

    @Test
    fun parseValidMdFileAutolinks() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("AUTOLINKS")

        // Test with AUTOLINKS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileAutolinks)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<p><a href=\"http://github.com\">http://github.com</a></p>"
        )

        // Test without AUTOLINKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileAutolinks)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>http://github.com</p>")
    }

    @Test
    fun parseValidMdFileDefinitions() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("DEFINITIONS")

        // Test with DEFINITIONS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileDefinitions)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<dl>\n<dt>Apple</dt>\n<dd>Pomaceous fruit</dd>\n</dl>"
        )

        // Test without DEFNITIONS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileDefinitions)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>Apple :   Pomaceous fruit</p>")
    }

    @Test
    fun parseValidMdFileFencedCodeBlocks() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("FENCED_CODE_BLOCKS")

        // Test with FENCED_CODE_BLOCKS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileFencedCodeBlocks)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<pre><code>function test() {\n  console.log(&quot;!&quot;);\n}\n</code></pre>"
        )

        // Test without FENCED_CODE_BLOCKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileFencedCodeBlocks)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<p><code>function test() { console.log(&quot;!&quot;); }</code></p>"
        )
    }

    @Test
    fun parseValidMdFileQuotes() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("QUOTES")

        // Test with QUOTES
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileQuotes)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>&ldquo;quotes&rdquo;</p>")

        // Test without QUOTES
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileQuotes)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>&quot;quotes&quot;</p>")
    }

    @Test
    fun parseValidMdFileSmarts() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SMARTS")

        // Test with SMARTS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSmarts)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>&hellip;</p>")

        // Test without SMARTS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSmarts)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>...</p>")
    }

    @Test
    fun parseValidMdFileSmartypants() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SMARTYPANTS")

        // Test with SMARTYPANTS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSmartypants)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>&ldquo;&hellip;&rdquo;</p>")

        // Test without SMARTYPANTS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSmartypants)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>&quot;...&quot;</p>")
    }

    @Test
    fun parseValidMdFileSuppressAllHTML() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SUPPRESS_ALL_HTML")

        // Test with SUPPRESS_ALL_HTML
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSuppressAllHTML)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("")

        // Test without SUPPRESS_ALL_HTML
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSuppressAllHTML)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<div>!</div><em>!</em>")
    }

    @Test
    fun parseValidMdFileSuppressHTMLBlocks() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SUPPRESS_HTML_BLOCKS")

        // Test with SUPPRESS_HTML_BLOCKS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSuppressHTMLBlocks)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("")

        // Test without SUPPRESS_HTML_BLOCKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSuppressHTMLBlocks)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<div>!</div><em>!</em>")
    }

    @Test
    fun parseValidMdFileSuppressInlineHTML() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("SUPPRESS_INLINE_HTML")

        // Test with SUPPRESS_INLINE_HTML
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileSuppressInlineHTML)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>This is the first paragraph.  with  inline html</p>")

        // Test without SUPPRESS_INLINE_HTML
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileSuppressInlineHTML)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body)
            .contains("<p>This is the first paragraph. <span> with </span> inline html</p>")
    }

    @Test
    fun parseValidMdFileTables() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("TABLES")

        // Test with TABLES
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileTables!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<table>\n" +
                    "<thead>\n" +
                    "<tr><th>First Header</th><th>Second Header</th></tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr><td>Content Cell</td><td>Content Cell</td></tr>\n" +
                    "<tr><td>Content Cell</td><td>Content Cell</td></tr>\n" +
                    "</tbody>\n" +
                    "</table>"
        )

        // Test without TABLES
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileTables!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<p>First Header|Second Header -------------|------------- Content Cell|Content Cell Content Cell|Content Cell</p>"
        )
    }

    @Test
    fun parseValidMdFileWikilinks() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("WIKILINKS")

        // Test with WIKILINKS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileWikilinks!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<p><a href=\"Wiki-style-links\">Wiki-style links</a></p>"
        )

        // Test without WIKILINKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileWikilinks!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>[[Wiki-style links]]</p>")
    }

    @Test
    fun parseValidMdFileAtxheaderspace() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("ATXHEADERSPACE")

        // Test with ATXHEADERSPACE
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileAtxheaderspace!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<p>#Test</p>")

        // Test without ATXHEADERSPACE
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileAtxheaderspace!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<h1>Test</h1>")
    }

    @Test
    fun parseValidMdFileForcelistitempara() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("FORCELISTITEMPARA")

        // Test with FORCELISTITEMPARA
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileForcelistitempara!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<ol>\n" +
                    "<li>\n" +
                    "<p>Item 1 Item 1 lazy continuation</p>\n" +
                    "<p>Item 1 paragraph 1 Item 1 paragraph 1 lazy continuation Item 1 paragraph 1 continuation</p>\n" +
                    "</li>\n" +
                    "</ol>"
        )

        // Test without FORCELISTITEMPARA
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileForcelistitempara!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<ol>\n" +
                    "<li>Item 1 Item 1 lazy continuation\n" +
                    "<p>Item 1 paragraph 1 Item 1 paragraph 1 lazy continuation Item 1 paragraph 1 continuation</p>\n" +
                    "</li>\n" +
                    "</ol>"
        )
    }

    @Test
    fun parseValidMdFileRelaxedhrules() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("RELAXEDHRULES")

        // Test with RELAXEDHRULES
        var parser = Parser(config)
        var documentModel = parser.processFile(mdFileRelaxedhrules!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
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
        )

        // Test without RELAXEDHRULES
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdFileRelaxedhrules!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<h2>Hello World</h2>\n" +
                    "<hr />\n" +
                    "<hr />\n" +
                    "<h2>Hello World ***</h2>\n" +
                    "<hr />\n" +
                    "<h2>Hello World ___</h2>\n" +
                    "<hr />"
        )
    }

    @Test
    fun parseValidMdFileTasklistitems() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("TASKLISTITEMS")

        // Test with TASKLISTITEMS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdTasklistitems!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<ul>\n" +
                    "<li>loose bullet item 3</li>\n" +
                    "<li class=\"task-list-item\"><input type=\"checkbox\" class=\"task-list-item-checkbox\" disabled=\"disabled\" readonly=\"readonly\" />&nbsp;open task item</li>\n" +
                    "<li class=\"task-list-item\"><input type=\"checkbox\" class=\"task-list-item-checkbox\" checked=\"checked\" disabled=\"disabled\" readonly=\"readonly\" />&nbsp;closed task item</li>\n" +
                    "</ul>"
        )

        // Test without TASKLISTITEMS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdTasklistitems!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<ul>\n" +
                    "<li>loose bullet item 3</li>\n" +
                    "<li>[ ] open task item</li>\n" +
                    "<li>[x] closed task item</li>\n" +
                    "</ul>"
        )
    }

    @Test
    fun parseValidMdFileExtanchorlinks() {
        config.setMarkdownExtensions("")
        config.setMarkdownExtensions("EXTANCHORLINKS")

        // Test with EXTANCHORLINKS
        var parser = Parser(config)
        var documentModel = parser.processFile(mdExtanchorlinks!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains(
            "<h1><a href=\"#header-some-formatting-chars\" id=\"header-some-formatting-chars\"></a>header &amp; some <em>formatting</em> ~~chars~~</h1>"
        )

        // Test without EXTANCHORLINKS
        config.setMarkdownExtensions("")
        parser = Parser(config)
        documentModel = parser.processFile(mdExtanchorlinks!!)
        Assert.assertNotNull(documentModel)
        Assertions.assertThat(documentModel!!.body).contains("<h1>header &amp; some <em>formatting</em> ~~chars~~</h1>")
    }
}
