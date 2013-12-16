
package org.jbake.app;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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

    public CompositeConfiguration config;

    private File configFile;

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

    private String validHeader = "title=Title\nstatus=draft\ntype=post\n~~~~~~";

    private String invalidHeader = "title=Title\n~~~~~~";

    private String extensions = "markdown.extensions";

    @Before
    public void createSampleFile() throws Exception {
        ConfigUtil.reset();

        configFile = new File(this.getClass().getResource(".").getFile());
        config = ConfigUtil.load(configFile);

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
        // TODO (ksclarke): this looks like an upstream bug (not suppressed):
        // out.println("<div>!</div><em>!</em>");
        // But this works:
        out.println("<div>!</div>");
        out.println("<em>!</em>");
        out.close();

        mdFileSuppressInlineHTML = folder.newFile("suppressInlineHTML.md");
        out = new PrintWriter(mdFileSuppressInlineHTML);
        out.println(validHeader);
        out.println("<div>!</div>");
        out.println("<em>!</em>");
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
    }

    @Test
    public void parseValidMarkdownFileBasic() throws Exception {
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(validMdFileBasic);
        Assert.assertNotNull(map);
        Assert.assertEquals("draft", map.get("status"));
        Assert.assertEquals("post", map.get("type"));
        Assert.assertEquals("<h1>This is a test</h1>", map.get("body"));
    }

    @Test
    public void parseInvalidMarkdownFileBasic() {
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(invalidMdFileBasic);
        Assert.assertNull(map);
    }

    @Test
    public void parseValidMdFileHardWraps() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "HARDWRAPS");

        // Test with HARDWRAPS
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileHardWraps);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>First line<br/>Second line</p>", map
                .get("body"));

        // Test without HARDWRAPS
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileHardWraps);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>First line Second line</p>", map.get("body"));
    }

    @Test
    public void parseValidMdFileAbbreviations() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "ABBREVIATIONS");

        // Test with ABBREVIATIONS
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileAbbreviations);
        Assert.assertNotNull(map);
        Assert.assertEquals(
                "<p><abbr title=\"Hyper Text Markup Language\">HTML</abbr></p>",
                map.get("body"));

        // Test without ABBREVIATIONS
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileAbbreviations);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>*[HTML]: Hyper Text Markup Language HTML</p>",
                map.get("body"));
    }

    @Test
    public void parseValidMdFileAutolinks() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "AUTOLINKS");

        // Test with AUTOLINKS
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileAutolinks);
        Assert.assertNotNull(map);
        Assert.assertEquals(
                "<p><a href=\"http://github.com\">http://github.com</a></p>",
                map.get("body"));

        // Test without AUTOLINKS
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileAutolinks);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>http://github.com</p>", map.get("body"));
    }

    @Test
    public void parseValidMdFileDefinitions() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "DEFINITIONS");

        // Test with DEFINITIONS
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileDefinitions);
        Assert.assertNotNull(map);
        Assert.assertEquals(
                "<dl><dt>Apple</dt><dd>Pomaceous fruit</dd>\n</dl>", map
                        .get("body"));

        // Test without DEFNITIONS
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileDefinitions);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>Apple : Pomaceous fruit</p>", map.get("body"));
    }

    @Test
    public void parseValidMdFileFencedCodeBlocks() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "FENCED_CODE_BLOCKS");

        // Test with FENCED_CODE_BLOCKS
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileFencedCodeBlocks);
        Assert.assertNotNull(map);
        Assert.assertEquals(
                "<pre><code>function test() {\n  console.log(&quot;!&quot;);\n}\n</code></pre>",
                map.get("body"));

        // Test without FENCED_CODE_BLOCKS
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileFencedCodeBlocks);
        Assert.assertNotNull(map);
        Assert.assertEquals(
                "<p><code>\nfunction test() {\n  console.log(&quot;!&quot;);\n}\n</code></p>",
                map.get("body"));
    }

    @Test
    public void parseValidMdFileQuotes() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "QUOTES");

        // Test with QUOTES
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileQuotes);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>&ldquo;quotes&rdquo;</p>", map.get("body"));

        // Test without QUOTES
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileQuotes);
        Assert.assertNotNull(map);
        // TODO: Shouldn't these be &quot; (report/fix upstream?)
        Assert.assertEquals("<p>\"quotes\"</p>", map.get("body"));
    }

    @Test
    public void parseValidMdFileSmarts() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "SMARTS");

        // Test with SMARTS
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileSmarts);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>&hellip;</p>", map.get("body"));

        // Test without SMARTS
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileSmarts);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>...</p>", map.get("body"));
    }

    @Test
    public void parseValidMdFileSmartypants() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "SMARTYPANTS");

        // Test with SMARTYPANTS
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileSmartypants);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>&ldquo;&hellip;&rdquo;</p>", map.get("body"));

        // Test without SMARTYPANTS
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileSmartypants);
        Assert.assertNotNull(map);
        // TODO: Shouldn't these be &quot; (report/fix upstream?)
        Assert.assertEquals("<p>\"...\"</p>", map.get("body"));
    }

    @Test
    public void parseValidMdFileSuppressAllHTML() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "SUPPRESS_ALL_HTML");

        // Test with SUPPRESS_ALL_HTML
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileSuppressAllHTML);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>!!</p>", map.get("body"));

        // Test without SUPPRESS_ALL_HTML
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileSuppressAllHTML);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p><div>!</div><em>!</em></p>", map.get("body"));
    }

    @Test
    public void parseValidMdFileSuppressHTMLBlocks() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "SUPPRESS_HTML_BLOCKS");

        // Test with SUPPRESS_HTML_BLOCKS
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileSuppressHTMLBlocks);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p><em>!</em></p>", map.get("body"));

        // Test without SUPPRESS_HTML_BLOCKS
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileSuppressHTMLBlocks);
        Assert.assertNotNull(map);
        Assert.assertEquals("<div>!</div><p><em>!</em></p>", map.get("body"));
    }

    @Test
    public void parseValidMdFileSuppressInlineHTML() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "SUPPRESS_INLINE_HTML");

        // Test with SUPPRESS_INLINE_HTML
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileSuppressInlineHTML);
        Assert.assertNotNull(map);
        Assert.assertEquals("<div>!</div><p>!</p>", map.get("body"));

        // Test without SUPPRESS_INLINE_HTML
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileSuppressInlineHTML);
        Assert.assertNotNull(map);
        Assert.assertEquals("<div>!</div><p><em>!</em></p>", map.get("body"));
    }

    @Test
    public void parseValidMdFileTables() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "TABLES");

        // Test with TABLES
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileTables);
        Assert.assertNotNull(map);
        Assert.assertEquals(
                "<table>\n  <thead>\n    <tr>\n      <th>First Header</th>\n      <th>Second Header</th>\n    </tr>\n  </thead>\n  <tbody>\n    <tr>\n      <td>Content Cell</td>\n      <td>Content Cell</td>\n    </tr>\n    <tr>\n      <td>Content Cell</td>\n      <td>Content Cell</td>\n    </tr>\n  </tbody>\n</table>",
                map.get("body"));

        // Test without TABLES
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileTables);
        Assert.assertNotNull(map);
        Assert.assertEquals(
                "<p>First Header|Second Header -------------|------------- Content Cell|Content Cell Content Cell|Content Cell</p>",
                map.get("body"));
    }

    @Test
    public void parseValidMdFileWikilinks() throws Exception {
        config.clearProperty(extensions);
        config.setProperty(extensions, "WIKILINKS");

        // Test with WIKILINKS
        Parser parser = new Parser(config, configFile.getPath());
        Map<String, Object> map = parser.processFile(mdFileWikilinks);
        Assert.assertNotNull(map);
        Assert.assertEquals(
                "<p><a href=\"./Wiki-style-links.html\">Wiki-style links</a></p>",
                map.get("body"));

        // Test without WIKILINKS
        config.clearProperty(extensions);
        parser = new Parser(config, configFile.getPath());
        map = parser.processFile(mdFileWikilinks);
        Assert.assertNotNull(map);
        Assert.assertEquals("<p>[[Wiki-style links]]</p>", map.get("body"));
    }

}
