package org.jbake.app;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests basic Markdown syntax and the extensions supported by the Markdown
 * processor (Pegdown).
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 * @author Kevin S. Clarke <ksclarke@gmail.com>
 */
public class MdParserTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public DefaultJBakeConfiguration config;

    private File validMdFileBasic;

    private File invalidMdFileBasic;

    private File mdFileHardWraps;

    private File mdFileAbbreviations;

    private File mdFileAutolinks;

    private File mdFileDefinitions;

    private File mdFileFencedCodeBlocks;

    private File mdFileQuotes;

    private File mdFileSmarts;

    private File mdFileSmartypants;

    private File mdFileSuppressAllHTML;

    private File mdFileSuppressHTMLBlocks;

    private File mdFileSuppressInlineHTML;

    private File mdFileTables;

    private File mdFileWikilinks;

    private File mdFileAtxheaderspace;

    private File mdFileForcelistitempara;

    private File mdFileRelaxedhrules;

    private File mdTasklistitems;

    private File mdExtanchorlinks;
    
    private File mdFootnote;

    private File mdAttributes;
    
    private File mdAdmonition;
    
    private File mdAsside;
    
    private File mdEmoji;
    
    private File mdEnumeratedReference;

    private String validHeader = "title=Title\nstatus=draft\ntype=post\n~~~~~~";

    private String invalidHeader = "title=Title\n~~~~~~";

    @Before
    public void createSampleFile() throws Exception {

        File configFile = TestUtils.getTestResourcesAsSourceFolder();
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(configFile);

        validMdFileBasic = folder.newFile("validBasic.md");
        PrintWriter out = new PrintWriter(validMdFileBasic);
        out.println(validHeader);
        out.println("# This is a test");
        out.close();

        invalidMdFileBasic = folder.newFile("invalidBasic.md");
        out = new PrintWriter(invalidMdFileBasic);
        out.println(invalidHeader);
        out.println("# This is a test");
        out.close();

        mdFileHardWraps = folder.newFile("hardWraps.md");
        out = new PrintWriter(mdFileHardWraps);
        out.println(validHeader);
        out.println("First line");
        out.println("Second line");
        out.close();

        mdFileAbbreviations = folder.newFile("abbreviations.md");
        out = new PrintWriter(mdFileAbbreviations);
        out.println(validHeader);
        out.println("*[HTML]: Hyper Text Markup Language");
        out.println("HTML");
        out.close();

        mdFileAutolinks = folder.newFile("autolinks.md");
        out = new PrintWriter(mdFileAutolinks);
        out.println(validHeader);
        out.println("http://github.com");
        out.close();

        mdFileDefinitions = folder.newFile("definitions.md");
        out = new PrintWriter(mdFileDefinitions);
        out.println(validHeader);
        out.println("Apple");
        out.println(":   Pomaceous fruit");
        out.close();

        mdFileFencedCodeBlocks = folder.newFile("fencedCodeBlocks.md");
        out = new PrintWriter(mdFileFencedCodeBlocks);
        out.println(validHeader);
        out.println("```");
        out.println("function test() {");
        out.println("  console.log(\"!\");");
        out.println("}");
        out.println("```");
        out.close();

        mdFileQuotes = folder.newFile("quotes.md");
        out = new PrintWriter(mdFileQuotes);
        out.println(validHeader);
        out.println("\"quotes\"");
        out.close();

        mdFileSmarts = folder.newFile("smarts.md");
        out = new PrintWriter(mdFileSmarts);
        out.println(validHeader);
        out.println("...");
        out.close();

        mdFileSmartypants = folder.newFile("smartypants.md");
        out = new PrintWriter(mdFileSmartypants);
        out.println(validHeader);
        out.println("\"...\"");
        out.close();

        mdFileSuppressAllHTML = folder.newFile("suppressAllHTML.md");
        out = new PrintWriter(mdFileSuppressAllHTML);
        out.println(validHeader);
        out.println("<div>!</div><em>!</em>");
        out.close();

        mdFileSuppressHTMLBlocks = folder.newFile("suppressHTMLBlocks.md");
        out = new PrintWriter(mdFileSuppressHTMLBlocks);
        out.println(validHeader);
        out.println("<div>!</div><em>!</em>");
        out.close();

        mdFileSuppressInlineHTML = folder.newFile("suppressInlineHTML.md");
        out = new PrintWriter(mdFileSuppressInlineHTML);
        out.println(validHeader);
        out.println("This is the first paragraph. <span> with </span> inline html");
        out.close();

        mdFileTables = folder.newFile("tables.md");
        out = new PrintWriter(mdFileTables);
        out.println(validHeader);
        out.println("First Header|Second Header");
        out.println("-------------|-------------");
        out.println("Content Cell|Content Cell");
        out.println("Content Cell|Content Cell");
        out.close();

        mdFileWikilinks = folder.newFile("wikilinks.md");
        out = new PrintWriter(mdFileWikilinks);
        out.println(validHeader);
        out.println("[[Wiki-style links]]");
        out.close();

        mdFileAtxheaderspace = folder.newFile("atxheaderspace.md");
        out = new PrintWriter(mdFileAtxheaderspace);
        out.println(validHeader);
        out.println("#Test");
        out.close();

        mdFileForcelistitempara = folder.newFile("forcelistitempara.md");
        out = new PrintWriter(mdFileForcelistitempara);
        out.println(validHeader);
        out.println("1. Item 1");
        out.println("Item 1 lazy continuation");
        out.println("");
        out.println("    Item 1 paragraph 1");
        out.println("Item 1 paragraph 1 lazy continuation");
        out.println("    Item 1 paragraph 1 continuation");
        out.close();

        mdFileRelaxedhrules = folder.newFile("releaxedhrules.md");
        out = new PrintWriter(mdFileRelaxedhrules);
        out.println(validHeader);
        out.println("Hello World");
        out.println("---");
        out.println("***");
        out.println("___");
        out.println("");
        out.println("Hello World");
        out.println("***");
        out.println("---");
        out.println("___");
        out.println("");
        out.println("Hello World");
        out.println("___");
        out.println("---");
        out.println("***");
        out.close();

        mdTasklistitems = folder.newFile("tasklistsitem.md");
        out = new PrintWriter(mdTasklistitems);
        out.println(validHeader);
        out.println("* loose bullet item 3");
        out.println("* [ ] open task item");
        out.println("* [x] closed task item");
        out.close();

        mdExtanchorlinks = folder.newFile("mdExtanchorlinks.md");
        out = new PrintWriter(mdExtanchorlinks);
        out.println(validHeader);
        out.println("# header & some *formatting* ~~chars~~");
        out.close();
        
        mdFootnote = folder.newFile("mdFootnote.md");
        out = new PrintWriter(mdFootnote);
        out.println(validHeader);
        out.println("Paragraph with a footnote reference[^1]");
        out.println("");
        out.println("[^1]: Footnote text added at the bottom of the document");
        out.close();
        
        mdAttributes = folder.newFile("mdAttributes.md");
        out = new PrintWriter(mdAttributes);
        out.println(validHeader);
        out.println("Paragraph with a **bold**{#IdForThisBold} reference{.test}");
        out.println("![an image](images/nothing.jpg){width='250px' height='100px'}");
        out.close();
        
        mdAdmonition = folder.newFile("mdAdmonition.md");
        out = new PrintWriter(mdAdmonition);
        out.println(validHeader);
        out.println("!!! caution \"Optional Title\"\n" +
        		"    block content sideBar with header\n");
        out.println("??? example \"Optional Title\"\n" + 
        		"    collapsible block content close by default\n");
        out.println(" !!! danger \"\"\n" + 
        		"        **danger** block content (without title)\n");
        out.close();
        
        mdAsside = folder.newFile("mdAsside.md");
        out = new PrintWriter(mdAsside);
        out.println(validHeader);
        out.println("Paragraph as intro\n");
        out.println("| an asside block\n"+
        			"| on multiple lines\n");
        out.close();
        
        mdEmoji = folder.newFile("mdEmoji.md");
        out = new PrintWriter(mdEmoji);
        out.println(validHeader);
        out.println("a bug :bug: and a pencil :pencil:\n"
        		+ "and not mapped due to missing last space :mango:");
        out.close();
        
        mdEnumeratedReference = folder.newFile("mdEnumeratedReference.md");
        out = new PrintWriter(mdEnumeratedReference);
        out.println(validHeader);
        out.println("![Flexmark Icon Logo](https://github.com/vsch/flexmark-java/raw/master/assets/images/flexmark-icon-logo%402x.png){#fig:test}\n"
        		+ "[#fig:test]\n"
        		+ "\n"
        		+ "![Flexmark Icon Logo](https://github.com/vsch/flexmark-java/raw/master/assets/images/flexmark-icon-logo%402x.png){#fig:test2}\n"
        		+ "[#fig:test2]\n"
        		+ "\n"
        		+ "| heading | heading | heading |\n"
        		+ "|---------|---------|---------|\n"
        		+ "| data    | data    |         |\n"
        		+ "[[#tbl:test] caption]\n"
        		+ "{#tbl:test}\n"
        		+ "\n"
        		+ "See [@fig:test2]\n"
        		+ "\n"
        		+ "See [@fig:test]\n"
        		+ "\n"
        		+ "See [@tbl:test]\n"
        		+ "\n"
        		+ "[@tbl]: Table [#].\n"
        		+ "\n"
        		+ "[@fig]: Figure [#].");
        out.close();
    }

    @Test
    public void parseValidMarkdownFileBasic() {
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(validMdFileBasic);
        Assert.assertNotNull(documentModel);
        Assert.assertEquals("draft", documentModel.getStatus());
        Assert.assertEquals("post", documentModel.getType());
        Assert.assertEquals("<h1>This is a test</h1>\n", documentModel.getBody());
    }

    @Test
    public void parseInvalidMarkdownFileBasic() {
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(invalidMdFileBasic);
        Assert.assertNull(documentModel);
    }

    @Test
    public void parseValidMdFileHardWraps() {
        config.setMarkdownExtensions("HARDWRAPS");

        // Test with HARDWRAPS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileHardWraps);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>First line<br />\nSecond line</p>\n");

        // Test without HARDWRAPS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileHardWraps);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>First line Second line</p>");
    }

    @Test
    public void parseWithInvalidExtension() {
        config.setMarkdownExtensions("HARDWRAPS,UNDEFINED_EXTENSION");

        // Test with HARDWRAPS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileHardWraps);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>First line<br />\nSecond line</p>\n");
    }

    @Test
    public void parseValidMdFileAbbreviations() {
        config.setMarkdownExtensions("ABBREVIATIONS");

        // Test with ABBREVIATIONS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileAbbreviations);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
            "<p><abbr title=\"Hyper Text Markup Language\">HTML</abbr></p>"
        );

        // Test without ABBREVIATIONS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileAbbreviations);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>*[HTML]: Hyper Text Markup Language HTML</p>");
    }

    @Test
    public void parseValidMdFileAutolinks() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("AUTOLINKS");

        // Test with AUTOLINKS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileAutolinks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
            "<p><a href=\"http://github.com\">http://github.com</a></p>"
        );

        // Test without AUTOLINKS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileAutolinks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>http://github.com</p>");
    }

    @Test
    public void parseValidMdFileDefinitions() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("DEFINITIONS");

        // Test with DEFINITIONS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileDefinitions);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
            "<dl>\n<dt>Apple</dt>\n<dd>Pomaceous fruit</dd>\n</dl>"
        );

        // Test without DEFNITIONS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileDefinitions);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>Apple :   Pomaceous fruit</p>");
    }

    @Test
    public void parseValidMdFileFencedCodeBlocks() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("FENCED_CODE_BLOCKS");

        // Test with FENCED_CODE_BLOCKS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileFencedCodeBlocks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
            "<pre><code>function test() {\n  console.log(&quot;!&quot;);\n}\n</code></pre>"
        );

        // Test without FENCED_CODE_BLOCKS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileFencedCodeBlocks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
            "<p><code>function test() { console.log(&quot;!&quot;); }</code></p>"
        );
    }

    @Test
    public void parseValidMdFileQuotes() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("QUOTES");

        // Test with QUOTES
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileQuotes);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>&ldquo;quotes&rdquo;</p>");

        // Test without QUOTES
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileQuotes);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>&quot;quotes&quot;</p>");
    }

    @Test
    public void parseValidMdFileSmarts() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("SMARTS");

        // Test with SMARTS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileSmarts);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>&hellip;</p>");

        // Test without SMARTS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileSmarts);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>...</p>");
    }

    @Test
    public void parseValidMdFileSmartypants() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("SMARTYPANTS");

        // Test with SMARTYPANTS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileSmartypants);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>&ldquo;&hellip;&rdquo;</p>");

        // Test without SMARTYPANTS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileSmartypants);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>&quot;...&quot;</p>");
    }

    @Test
    public void parseValidMdFileSuppressAllHTML() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("SUPPRESS_ALL_HTML");

        // Test with SUPPRESS_ALL_HTML
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileSuppressAllHTML);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("");

        // Test without SUPPRESS_ALL_HTML
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileSuppressAllHTML);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<div>!</div><em>!</em>");
    }

    @Test
    public void parseValidMdFileSuppressHTMLBlocks() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("SUPPRESS_HTML_BLOCKS");

        // Test with SUPPRESS_HTML_BLOCKS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileSuppressHTMLBlocks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("");

        // Test without SUPPRESS_HTML_BLOCKS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileSuppressHTMLBlocks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<div>!</div><em>!</em>");
    }

    @Test
    public void parseValidMdFileSuppressInlineHTML() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("SUPPRESS_INLINE_HTML");

        // Test with SUPPRESS_INLINE_HTML
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileSuppressInlineHTML);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>This is the first paragraph.  with  inline html</p>");

        // Test without SUPPRESS_INLINE_HTML
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileSuppressInlineHTML);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>This is the first paragraph. <span> with </span> inline html</p>");
    }

    @Test
    public void parseValidMdFileTables() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("TABLES");

        // Test with TABLES
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileTables);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
                "<table>\n" +
                    "<thead>\n" +
                    "<tr><th>First Header</th><th>Second Header</th></tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr><td>Content Cell</td><td>Content Cell</td></tr>\n" +
                    "<tr><td>Content Cell</td><td>Content Cell</td></tr>\n" +
                    "</tbody>\n" +
                    "</table>"
        );

        // Test without TABLES
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileTables);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
            "<p>First Header|Second Header -------------|------------- Content Cell|Content Cell Content Cell|Content Cell</p>"
        );
    }

    @Test
    public void parseValidMdFileWikilinks() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("WIKILINKS");

        // Test with WIKILINKS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileWikilinks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
            "<p><a href=\"Wiki-style-links\">Wiki-style links</a></p>"
        );

        // Test without WIKILINKS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileWikilinks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>[[Wiki-style links]]</p>");
    }

    @Test
    public void parseValidMdFileAtxheaderspace() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("ATXHEADERSPACE");

        // Test with ATXHEADERSPACE
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileAtxheaderspace);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>#Test</p>");

        // Test without ATXHEADERSPACE
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileAtxheaderspace);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<h1>Test</h1>");
    }

    @Test
    public void parseValidMdFileForcelistitempara() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("FORCELISTITEMPARA");

        // Test with FORCELISTITEMPARA
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileForcelistitempara);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
                "<ol>\n" +
                    "<li>\n" +
                    "<p>Item 1 Item 1 lazy continuation</p>\n" +
                    "<p>Item 1 paragraph 1 Item 1 paragraph 1 lazy continuation Item 1 paragraph 1 continuation</p>\n" +
                    "</li>\n" +
                    "</ol>");

        // Test without FORCELISTITEMPARA
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileForcelistitempara);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
                "<ol>\n" +
                    "<li>Item 1 Item 1 lazy continuation\n" +
                    "<p>Item 1 paragraph 1 Item 1 paragraph 1 lazy continuation Item 1 paragraph 1 continuation</p>\n" +
                    "</li>\n" +
                    "</ol>"
        );
    }

    @Test
    public void parseValidMdFileRelaxedhrules() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("RELAXEDHRULES");

        // Test with RELAXEDHRULES
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFileRelaxedhrules);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
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
        );

        // Test without RELAXEDHRULES
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFileRelaxedhrules);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
                "<h2>Hello World</h2>\n" +
                    "<hr />\n" +
                    "<hr />\n" +
                    "<h2>Hello World ***</h2>\n" +
                    "<hr />\n" +
                    "<h2>Hello World ___</h2>\n" +
                    "<hr />"
        );
    }

    @Test
    public void parseValidMdFileTasklistitems() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("TASKLISTITEMS");

        // Test with TASKLISTITEMS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdTasklistitems);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
                "<ul>\n" +
                    "<li>loose bullet item 3</li>\n" +
                    "<li class=\"task-list-item\"><input type=\"checkbox\" class=\"task-list-item-checkbox\" disabled=\"disabled\" readonly=\"readonly\" />&nbsp;open task item</li>\n" +
                    "<li class=\"task-list-item\"><input type=\"checkbox\" class=\"task-list-item-checkbox\" checked=\"checked\" disabled=\"disabled\" readonly=\"readonly\" />&nbsp;closed task item</li>\n" +
                    "</ul>"
        );

        // Test without TASKLISTITEMS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdTasklistitems);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
                "<ul>\n" +
                    "<li>loose bullet item 3</li>\n" +
                    "<li>[ ] open task item</li>\n" +
                    "<li>[x] closed task item</li>\n" +
                    "</ul>");
    }

    @Test
    public void parseValidMdFileExtanchorlinks() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("EXTANCHORLINKS");

        // Test with EXTANCHORLINKS
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdExtanchorlinks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains(
            "<h1><a href=\"#header-some-formatting-chars\" id=\"header-some-formatting-chars\"></a>header &amp; some <em>formatting</em> ~~chars~~</h1>"
        );

        // Test without EXTANCHORLINKS
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdExtanchorlinks);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<h1>header &amp; some <em>formatting</em> ~~chars~~</h1>");
    }
    
    @Test
    public void parseValidMdFileFootnoteExtension() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("Footnote");

        // Test with Footnote
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdFootnote);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<sup id=\"fnref-1\"><a class=\"footnote-ref\" href=\"#fn-1\">1</a>");
        
        assertThat(documentModel.getBody()).contains(
        		"<div class=\"footnotes\">\n" + 
        		"<hr />\n" +
                "<ol>\n" + 
                "<li id=\"fn-1\">\n" + 
                "<p>Footnote text added at the bottom of the document</p>\n" +
                "<a href=\"#fnref-1\" class=\"footnote-backref\">&#8617;</a>\n" +
                "</li>\n" +
                "</ol>\n" + 
                "</div>"
        );

        // Test without Footnote
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdFootnote);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("Paragraph with a footnote reference[^1]");
    }

    @Test
    public void parseValidMdFileAttributesExtension() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("Attributes");

        // Test with attributes
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdAttributes);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<strong id=\"IdForThisBold\">bold</strong>");
        assertThat(documentModel.getBody()).contains("<span class=\"test\">");
        assertThat(documentModel.getBody()).contains("<img src=\"images/nothing.jpg\" alt=\"an image\" width=\"250px\" height=\"100px\" />");

        // Test without attributes
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdAttributes);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>Paragraph with a <strong>bold</strong>{#IdForThisBold} reference{.test}");
        assertThat(documentModel.getBody()).contains("<img src=\"images/nothing.jpg\" alt=\"an image\" />{width='250px' height='100px'}");
    }
    
    @Test
    public void parseValidMdFileAdmonitionExtension() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("Admonition");

        // Test with admonition
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdAdmonition);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<symbol id=\"adm-warning\">");
        assertThat(documentModel.getBody()).contains("<div class=\"adm-block adm-warning\">\n" +
        		 "<div class=\"adm-heading\">\n" +
        		 "<svg class=\"adm-icon\"><use xlink:href=\"#adm-warning\" /></svg><span>Optional Title</span>\n" +
        		 "</div>\n" +
        		 "<div class=\"adm-body\">\n" +
        		 "<p>block content sideBar with header</p>");
        assertThat(documentModel.getBody()).contains("<div class=\"adm-block adm-example adm-collapsed\">\n" +
        		 "<div class=\"adm-heading\">\n" +
        		 "<svg class=\"adm-icon\"><use xlink:href=\"#adm-example\" /></svg><span>Optional Title</span>\n" +
        		 "</div>\n" + 
        		 "<div class=\"adm-body\">\n" +
        		 "<p>collapsible block content close by default</p>");
        assertThat(documentModel.getBody()).contains("<div class=\"adm-block adm-danger\">\n" +
        		 "<div class=\"adm-body\">\n" +
        		 "<p><strong>danger</strong> block content (without title)</p>\n" +
        		 "</div>");

        // Test without admonition
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdAdmonition);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("!!! caution &quot;Optional Title&quot; block content sideBar with header");
        assertThat(documentModel.getBody()).contains("??? example &quot;Optional Title&quot; collapsible block content close by default");
        assertThat(documentModel.getBody()).contains("!!! danger &quot;&quot; <strong>danger</strong> block content (without title)");
    }
    
    @Test
    public void parseValidMdFileAssideExtension() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("Aside");

        // Test with Asside
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdAsside);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<aside>\n"+ 
        		"<p>an asside block on multiple lines</p>\n"+
        		"</aside>\n");

        // Test without Asside
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdAsside);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("| an asside block | on multiple lines");
    }
    
    @Test
    public void parseValidMdFileEmojiExtension() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("Emoji");

        // Test with Asside
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdEmoji);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>a bug <img src=\"/img/bug.png\" alt=\"emoji nature:bug\" height=\"20\" width=\"20\" align=\"absmiddle\" /> and a pencil <img src=\"/img/pencil.png\" alt=\"emoji objects:pencil\" height=\"20\" width=\"20\" align=\"absmiddle\" /> and not mapped due to missing last space :mango:</p>");

        // Test without Asside
        config.setMarkdownExtensions("");
        parser = new Parser(config);
        documentModel = parser.processFile(mdEmoji);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p>a bug :bug: and a pencil :pencil: and not mapped due to missing last space :mango:</p>");
        assertThat(documentModel.getBody()).contains("");
        
    }
    
    @Test
    public void parseValidMdFilemdEnumeratedReferenceExtension() {
        config.setMarkdownExtensions("");
        config.setMarkdownExtensions("EnumeratedReference,Attributes,TABLES");

        // Test with EnumeratedReference
        Parser parser = new Parser(config);
        DocumentModel documentModel = parser.processFile(mdEnumeratedReference);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p><img src=\"https://github.com/vsch/flexmark-java/raw/master/assets/images/flexmark-icon-logo%402x.png\" alt=\"Flexmark Icon Logo\" id=\"fig:test\" /> Figure 1.</p>\n"
        		+ "<p><img src=\"https://github.com/vsch/flexmark-java/raw/master/assets/images/flexmark-icon-logo%402x.png\" alt=\"Flexmark Icon Logo\" id=\"fig:test2\" /> Figure 2.</p>\n"
        		+ "<table id=\"tbl:test\">\n"
        		+ "<thead>\n"
        		+ "<tr><th> heading </th><th> heading </th><th> heading </th></tr>\n"
        		+ "</thead>\n"
        		+ "<tbody>\n"
        		+ "<tr><td> data    </td><td> data    </td><td>         </td></tr>\n"
        		+ "</tbody>\n"
        		+ "<caption>Table 1. caption</caption>\n"
        		+ "</table>\n"
        		+ "<p>See <a href=\"#fig:test2\" title=\"Figure 2.\">Figure 2.</a></p>\n"
        		+ "<p>See <a href=\"#fig:test\" title=\"Figure 1.\">Figure 1.</a></p>\n"
        		+ "<p>See <a href=\"#tbl:test\" title=\"Table 1.\">Table 1.</a></p>\n"
        		);

        // Test without EnumeratedReference
        config.setMarkdownExtensions("Attributes,TABLES");
        parser = new Parser(config);
        documentModel = parser.processFile(mdEnumeratedReference);
        Assert.assertNotNull(documentModel);
        assertThat(documentModel.getBody()).contains("<p><img src=\"https://github.com/vsch/flexmark-java/raw/master/assets/images/flexmark-icon-logo%402x.png\" alt=\"Flexmark Icon Logo\" id=\"fig:test\" /> [#fig:test]</p>\n"
        		+ "<p><img src=\"https://github.com/vsch/flexmark-java/raw/master/assets/images/flexmark-icon-logo%402x.png\" alt=\"Flexmark Icon Logo\" id=\"fig:test2\" /> [#fig:test2]</p>\n"
        		+ "<table id=\"tbl:test\">\n"
        		+ "<thead>\n"
        		+ "<tr><th> heading </th><th> heading </th><th> heading </th></tr>\n"
        		+ "</thead>\n"
        		+ "<tbody>\n"
        		+ "<tr><td> data    </td><td> data    </td><td>         </td></tr>\n"
        		+ "</tbody>\n"
        		+ "<caption>[#tbl:test] caption</caption>\n"
        		+ "</table>\n"
        		+ "<p>See [@fig:test2]</p>\n"
        		+ "<p>See [@fig:test]</p>\n"
        		+ "<p>See [@tbl:test]</p>\n"
        		+ "<p>[@tbl]: Table [#].</p>\n"
        		+ "<p>[@fig]: Figure [#].</p>\n");
    }
    
}
