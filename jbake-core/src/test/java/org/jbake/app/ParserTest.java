package org.jbake.app;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeProperty;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private DefaultJBakeConfiguration config;
    private Parser parser;
    private File rootPath;

    private File validHTMLFile;
    private File invalidHTMLFile;
    private File validMarkdownFileWithCustomHeader;
    private File validMarkdownFileWithDefaultStatus;
    private File validMarkdownFileWithDefaultTypeAndStatus;
    private File invalidMarkdownFileWithoutDefaultStatus;
    private File invalidMDFile;
    private File invalidExtensionFile;
    private File validHTMLWithJSONFile;
    private File validAsciiDocWithJSONFile;
    private File validAsciiDocWithADHeaderJSONFile;
    private File validaAsciidocWithUnsanitizedHeader;

    private String validHeader = "title=This is a Title = This is a valid Title\nstatus=draft\ntype=post\ndate=2013-09-02\n~~~~~~";
    private String invalidHeader = "title=This is a Title\n~~~~~~";
    private String sampleJsonData = "{\"numberValue\": 42, \"stringValue\": \"Answer to live, the universe and everything\", \"nullValue\": null, \"arrayValue\": [1, 2], \"objectValue\": {\"val1\": 1, \"val2\": 2}}";

    private String unsanitizedKeys = " title= Title \n status= draft \n   type= post   \ndate=2020-02-30\ncustom=custom without bom's\ntags= jbake, java    , tag with space   \n~~~~~~";

    private String customHeaderSeparator;


    @Before
    public void createSampleFile() throws Exception {
        rootPath = TestUtils.getTestResourcesAsSourceFolder();
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(rootPath);
        parser = new Parser(config);

        validHTMLFile = folder.newFile("valid.html");
        PrintWriter out = new PrintWriter(validHTMLFile);
        out.println(validHeader);
        out.println("<p>This is a test.</p>");
        out.close();

        invalidHTMLFile = folder.newFile("invalid.html");
        out = new PrintWriter(invalidHTMLFile);
        out.println(invalidHeader);
        out.close();

        validMarkdownFileWithCustomHeader = folder.newFile("validMdCustomHeader.md");

        customHeaderSeparator = "---------------------------------------";
        out = new PrintWriter(validMarkdownFileWithCustomHeader);
        out.println("title=Custom Header separator");
        out.println("type=post");
        out.println("status=draft");
        out.println(customHeaderSeparator);
        out.println("# Hello Markdown!");
        out.println("");
        out.println("A paragraph");
        out.println("");
        out.println("* And");
        out.println("* A");
        out.println("* List");
        out.close();

        validMarkdownFileWithDefaultStatus = folder.newFile("validMdDefaultStatus.md");

        out = new PrintWriter(validMarkdownFileWithDefaultStatus);
        out.println("title=Custom Header separator");
        out.println("type=post");
        out.println(config.getHeaderSeparator());
        out.println("# Hello Markdown!");
        out.println("");
        out.println("A paragraph");
        out.println("");
        out.println("* And");
        out.println("* A");
        out.println("* List");
        out.close();

        validMarkdownFileWithDefaultTypeAndStatus = folder.newFile("validMdDefaultTypeAndStatus.md");

        out = new PrintWriter(validMarkdownFileWithDefaultTypeAndStatus);
        out.println("title=Custom Header separator");
        out.println(config.getHeaderSeparator());
        out.println("# Hello Markdown!");
        out.println("");
        out.println("A paragraph");
        out.println("");
        out.println("* And");
        out.println("* A");
        out.println("* List");
        out.close();

        invalidMarkdownFileWithoutDefaultStatus = folder.newFile("invalidMdWithoutDefaultStatus.md");

        out = new PrintWriter(invalidMarkdownFileWithoutDefaultStatus);
        out.println("title=Custom Header separator");
        out.println("type=page");
        out.println(config.getHeaderSeparator());
        out.println("# Hello Markdown!");
        out.println("");
        out.println("A paragraph");
        out.println("");
        out.println("* And");
        out.println("* A");
        out.println("* List");
        out.close();

        invalidMDFile = folder.newFile("invalidMd.md");

        out = new PrintWriter(invalidMDFile);
        out.println(invalidHeader);
        out.println("# Hello Markdown!");
        out.println("");
        out.println("A paragraph");
        out.println("");
        out.println("* And");
        out.println("* A");
        out.println("* List");
        out.close();

        invalidExtensionFile = folder.newFile("invalid.invalid");
        out = new PrintWriter(invalidExtensionFile);
        out.println("invalid content");
        out.close();

        validHTMLWithJSONFile = folder.newFile("validHTMLWithJSONFile.html");
        out = new PrintWriter(validHTMLWithJSONFile);
        out.println("title=This is a Title = This is a valid Title");
        out.println("status=draft");
        out.println("type=post");
        out.println("date=2013-09-02");
        out.print("jsondata=");
        out.println(sampleJsonData);
        out.println("~~~~~~");
        out.println("Sample Body");
        out.close();

        validAsciiDocWithJSONFile = folder.newFile("validAsciiDocWithJSONFile.ad");
        out = new PrintWriter(validAsciiDocWithJSONFile);
        out.println("title=This is a Title = This is a valid Title");
        out.println("status=draft");
        out.println("type=post");
        out.println("date=2013-09-02");
        out.print("jsondata=");
        out.println(sampleJsonData);
        out.println("~~~~~~");
        out.println("= Hello, AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        validAsciiDocWithADHeaderJSONFile = folder.newFile("validAsciiDocWithADHeaderJSONFile.ad");
        out = new PrintWriter(validAsciiDocWithADHeaderJSONFile);
        out.println("= Hello: AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println(":jbake-status: published");
        out.println(":jbake-type: page");
        out.print(":jbake-jsondata: ");
        out.println(sampleJsonData);
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        validaAsciidocWithUnsanitizedHeader = folder.newFile("validAsciidocWithUnsanitizedHeader.adoc");
        out = new PrintWriter(validaAsciidocWithUnsanitizedHeader, "UTF-8");
        // Simulating a \uFEFF Byte order Marker in utf-8
        out.print("\uFEFF");
        out.println(unsanitizedKeys);
        out.println("= Hello: AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println(":jbake-status: published");
        out.println(":jbake-type: page");
        out.print(":jbake-jsondata: ");
        out.println(sampleJsonData);
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

    }

    @Test
    public void parseValidHTMLFile() {
        Map<String, Object> map = parser.processFile(validHTMLFile);
        Assert.assertNotNull(map);
        Assert.assertEquals("draft", map.get("status"));
        Assert.assertEquals("post", map.get("type"));
        Assert.assertEquals("This is a Title = This is a valid Title", map.get("title"));
        Assert.assertNotNull(map.get("date"));
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) map.get("date"));
        Assert.assertEquals(8, cal.get(Calendar.MONTH));
        Assert.assertEquals(2, cal.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(2013, cal.get(Calendar.YEAR));

    }

    @Test
    public void parseInvalidHTMLFile() {
        Map<String, Object> map = parser.processFile(invalidHTMLFile);
        Assert.assertNull(map);
    }

    @Test
    public void parseInvalidExtension(){
        Map<String, Object> map = parser.processFile(invalidExtensionFile);
        Assert.assertNull(map);
    }


    @Test
    public void parseMarkdownFileWithCustomHeaderSeparator() {
        config.setHeaderSeparator(customHeaderSeparator);

        Map<String, Object> map = parser.processFile(validMarkdownFileWithCustomHeader);
        Assert.assertNotNull(map);
        Assert.assertEquals("draft", map.get("status"));
        Assert.assertEquals("post", map.get("type"));
        assertThat(map.get("body").toString())
                .contains("<p>A paragraph</p>");

    }

    @Test
    public void parseMarkdownFileWithDefaultStatus() {
        config.setDefaultStatus("published");

        Map<String, Object> map = parser.processFile(validMarkdownFileWithDefaultStatus);
        Assert.assertNotNull(map);
        Assert.assertEquals("published", map.get("status"));
        Assert.assertEquals("post", map.get("type"));
    }

    @Test
    public void parseMarkdownFileWithDefaultTypeAndStatus() {
        config.setDefaultStatus("published");
        config.setDefaultType("page");

        Map<String, Object> map = parser.processFile(validMarkdownFileWithDefaultTypeAndStatus);
        Assert.assertNotNull(map);
        Assert.assertEquals("published", map.get("status"));
        Assert.assertEquals("page", map.get("type"));
    }

    @Test
    public void parseInvalidMarkdownFileWithoutDefaultStatus() {
        config.setDefaultStatus("");
        config.setDefaultType("page");

        Map<String, Object> map = parser.processFile(invalidMarkdownFileWithoutDefaultStatus);
        Assert.assertNull(map);
    }

    @Test
    public void parseInvalidMarkdownFile() {
        Map<String, Object> map = parser.processFile(invalidMDFile);
        Assert.assertNull(map);
    }

    @Test
    public void sanitizeKeysAndValues() {
        Map<String, Object> map = parser.processFile(validaAsciidocWithUnsanitizedHeader);

        assertThat(map.get("status")).isEqualTo("draft");
        assertThat(map.get("title")).isEqualTo("Title");
        assertThat(map.get("type")).isEqualTo("post");
        assertThat(map.get("custom")).isEqualTo("custom without bom's");
        assertThat(map.get("tags")).isEqualTo(Arrays.asList("jbake", "java", "tag with space").toArray());
    }

    @Test
    public void sanitizeTags() {
        config.setProperty(JBakeProperty.TAG_SANITIZE, true);
        Map<String, Object> map = parser.processFile(validaAsciidocWithUnsanitizedHeader);

        assertThat(map.get("tags")).isEqualTo(Arrays.asList("jbake", "java", "tag-with-space").toArray());
    }


    @Test
    public void parseValidHTMLWithJSONFile() {
        Map<String, Object> map = parser.processFile(validHTMLWithJSONFile);
        assertJSONExtracted(map.get("jsondata"));
    }

    @Test
    public void parseValidAsciiDocWithJSONFile() {
        Map<String, Object> map = parser.processFile(validAsciiDocWithJSONFile);
        assertJSONExtracted(map.get("jsondata"));
    }

    @Test
    public void testValidAsciiDocWithADHeaderJSONFile() {
        Map<String, Object> map = parser.processFile(validAsciiDocWithADHeaderJSONFile);
        assertJSONExtracted(map.get("jsondata"));
    }

    private void assertJSONExtracted(Object jsonDataEntry) {
        assertThat(jsonDataEntry).isInstanceOf(JSONObject.class);
        JSONObject jsonData = (JSONObject) jsonDataEntry;
        assertThat(jsonData.containsKey("numberValue")).isTrue();
        assertThat(jsonData.get("numberValue")).isInstanceOf(Number.class);
        assertThat(((Number)jsonData.get("numberValue")).intValue()).isEqualTo(42);
        assertThat(jsonData.containsKey("stringValue")).isTrue();
        assertThat(jsonData.get("stringValue")).isInstanceOf(String.class);
        assertThat((String)jsonData.get("stringValue")).isEqualTo("Answer to live, the universe and everything");
        assertThat(jsonData.containsKey("nullValue")).isTrue();
        assertThat(jsonData.get("nullValue")).isNull();
        assertThat(jsonData.containsKey("arrayValue")).isTrue();
        assertThat(jsonData.get("arrayValue")).isInstanceOf(JSONArray.class);
        assertThat((JSONArray)jsonData.get("arrayValue")).contains(1L,2L);
        assertThat(jsonData.containsKey("objectValue")).isTrue();
        assertThat(jsonData.get("objectValue")).isInstanceOf(JSONObject.class);
        assertThat((JSONObject)jsonData.get("objectValue")).contains(new SimpleEntry("val1", 1L), new SimpleEntry("val2", 2L));
    }
}
