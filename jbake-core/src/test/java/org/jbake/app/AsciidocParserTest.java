package org.jbake.app;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AsciidocParserTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private DefaultJBakeConfiguration config;
    private Parser parser;
    private File rootPath;

    private File asciidocWithSource;
    private File validAsciidocFile;
    private File invalidAsciiDocFile;
    private File validAsciiDocFileWithoutHeader;
    private File invalidAsciiDocFileWithoutHeader;
    private File validAsciiDocFileWithHeaderInContent;
    private File validAsciiDocFileWithoutJBakeMetaData;

    private String validHeader = "title=This is a Title = This is a valid Title\nstatus=draft\ntype=post\ndate=2013-09-02\n~~~~~~";
    private String invalidHeader = "title=This is a Title\n~~~~~~";

    @Before
    public void createSampleFile() throws Exception {
        rootPath = TestUtils.getTestResourcesAsSourceFolder();
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(rootPath);
        parser = new Parser(config);

        asciidocWithSource = folder.newFile("asciidoc-with-source.ad");
        PrintWriter out = new PrintWriter(asciidocWithSource);
        out.println(validHeader);
        out.println("= Hello, AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.println("");
        out.println("```");
        out.println("#!/bin/bash");
        out.println("");
        out.println("echo 'hello world!'");
        out.println("```");
        out.println("");
        out.println("{testattribute}");

        out.close();

        validAsciidocFile = folder.newFile("valid.ad");
        out = new PrintWriter(validAsciidocFile);
        out.println(validHeader);
        out.println("= Hello, AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        invalidAsciiDocFile = folder.newFile("invalid.ad");
        out = new PrintWriter(invalidAsciiDocFile);
        out.println(invalidHeader);
        out.println("= Hello, AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        validAsciiDocFileWithoutHeader = folder.newFile("validwoheader.ad");
        out = new PrintWriter(validAsciiDocFileWithoutHeader);
        out.println("= Hello: AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println(":jbake-status: published");
        out.println(":jbake-type: page");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        invalidAsciiDocFileWithoutHeader = folder.newFile("invalidwoheader.ad");
        out = new PrintWriter(invalidAsciiDocFileWithoutHeader);
        out.println("= Hello, AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println(":jbake-status: published");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        validAsciiDocFileWithHeaderInContent = folder.newFile("validheaderincontent.ad");
        out = new PrintWriter(validAsciiDocFileWithHeaderInContent);
        out.println("= Hello, AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println(":jbake-status: published");
        out.println(":jbake-type: page");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.println("");
        out.println("----");
        out.println("title=Example Header");
        out.println("date=2013-02-01");
        out.println("type=post");
        out.println("tags=tag1, tag2");
        out.println("status=published");
        out.println("~~~~~~");
        out.println("----");
        out.close();

        validAsciiDocFileWithoutJBakeMetaData = folder.newFile("validwojbakemetadata.ad");
        out = new PrintWriter(validAsciiDocFileWithoutJBakeMetaData);
        out.println("= Hello: AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println("");
        out.println("JBake now supports AsciiDoc documents without JBake meta data.");
        out.close();
    }


    @Test
    public void parseAsciidocFileWithPrettifyAttribute() {

        config.setProperty(JBakeProperty.ASCIIDOCTOR_ATTRIBUTES,"source-highlighter=prettify");
        Map<String, Object> map = parser.processFile(asciidocWithSource);
        Assert.assertNotNull(map);
        Assert.assertEquals("draft", map.get("status"));
        Assert.assertEquals("post", map.get("type"));
        assertThat(map.get("body").toString())
                .contains("class=\"paragraph\"")
                .contains("<p>JBake now supports AsciiDoc.</p>")
                .contains("class=\"prettyprint highlight\"");

        assertThat(map.get("body").toString()).doesNotContain("I Love Jbake");
        System.out.println(map.get("body").toString());
    }

    @Test
    public void parseAsciidocFileWithCustomAttribute() {

        config.setProperty(JBakeProperty.ASCIIDOCTOR_ATTRIBUTES,"source-highlighter=prettify,testattribute=I Love Jbake");
        Map<String, Object> map = parser.processFile(asciidocWithSource);
        Assert.assertNotNull(map);
        Assert.assertEquals("draft", map.get("status"));
        Assert.assertEquals("post", map.get("type"));
        assertThat(map.get("body").toString())
                .contains("I Love Jbake")
                .contains("class=\"prettyprint highlight\"");

        System.out.println(map.get("body").toString());
    }

    @Test
    public void parseValidAsciiDocFile() {
        Map<String, Object> map = parser.processFile(validAsciidocFile);
        Assert.assertNotNull(map);
        Assert.assertEquals("draft", map.get("status"));
        Assert.assertEquals("post", map.get("type"));
        assertThat(map.get("body").toString())
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>");
    }

    @Test
    public void parseInvalidAsciiDocFile() {
        Map<String, Object> map = parser.processFile(invalidAsciiDocFile);
        Assert.assertNull(map);
    }

    @Test
    public void parseValidAsciiDocFileWithoutHeader() {
        Map<String, Object> map = parser.processFile(validAsciiDocFileWithoutHeader);
        Assert.assertNotNull(map);
        Assert.assertEquals("Hello: AsciiDoc!", map.get("title"));
        Assert.assertEquals("published", map.get("status"));
        Assert.assertEquals("page", map.get("type"));
        assertThat(map.get("body").toString())
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>");
    }

    @Test
    public void parseInvalidAsciiDocFileWithoutHeader() {
        Map<String, Object> map = parser.processFile(invalidAsciiDocFileWithoutHeader);
        Assert.assertNull(map);
    }

    @Test
    public void parseValidAsciiDocFileWithExampleHeaderInContent() {
        Map<String, Object> map = parser.processFile(validAsciiDocFileWithHeaderInContent);
        Assert.assertNotNull(map);
        Assert.assertEquals("published", map.get("status"));
        Assert.assertEquals("page", map.get("type"));
        assertThat(map.get("body").toString())
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>")
            .contains("class=\"listingblock\"")
            .contains("class=\"content\"")
            .contains("<pre>")
            .contains("title=Example Header")
            .contains("date=2013-02-01")
            .contains("tags=tag1, tag2");
    }

    @Test
    public void parseValidAsciiDocFileWithoutJBakeMetaDataUsingDefaultTypeAndStatus() {
        config.setDefaultStatus("published");
        config.setDefaultType("page");
        Parser parser = new Parser(config);
        Map<String, Object> map = parser.processFile(validAsciiDocFileWithoutJBakeMetaData);
        Assert.assertNotNull(map);
        Assert.assertEquals("published", map.get("status"));
        Assert.assertEquals("page", map.get("type"));
        assertThat(map.get("body").toString())
            .contains("<p>JBake now supports AsciiDoc documents without JBake meta data.</p>");
    }

}
