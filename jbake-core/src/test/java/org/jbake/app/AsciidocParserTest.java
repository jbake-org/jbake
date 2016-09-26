package org.jbake.app;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbake.app.configuration.PropertyList.ASCIIDOCTOR_ATTRIBUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AsciidocParserTest {

    @TempDir
    private Path folder;

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

    @BeforeEach
    void createSampleFile() throws Exception {
        rootPath = TestUtils.getTestResourcesAsSourceFolder();
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(rootPath);
        parser = new Parser(config);

        asciidocWithSource = folder.resolve("asciidoc-with-source.ad").toFile();
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

        validAsciidocFile = folder.resolve("valid.ad").toFile();
        out = new PrintWriter(validAsciidocFile);
        out.println(validHeader);
        out.println("= Hello, AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        invalidAsciiDocFile = folder.resolve("invalid.ad").toFile();
        out = new PrintWriter(invalidAsciiDocFile);
        out.println(invalidHeader);
        out.println("= Hello, AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        validAsciiDocFileWithoutHeader = folder.resolve("validwoheader.ad").toFile();
        out = new PrintWriter(validAsciiDocFileWithoutHeader);
        out.println("= Hello: AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println(":jbake-status: published");
        out.println(":jbake-type: page");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        invalidAsciiDocFileWithoutHeader = folder.resolve("invalidwoheader.ad").toFile();
        out = new PrintWriter(invalidAsciiDocFileWithoutHeader);
        out.println("= Hello, AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println(":jbake-status: published");
        out.println("");
        out.println("JBake now supports AsciiDoc.");
        out.close();

        validAsciiDocFileWithHeaderInContent = folder.resolve("validheaderincontent.ad").toFile();
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

        validAsciiDocFileWithoutJBakeMetaData = folder.resolve("validwojbakemetadata.ad").toFile();
        out = new PrintWriter(validAsciiDocFileWithoutJBakeMetaData);
        out.println("= Hello: AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println("");
        out.println("JBake now supports AsciiDoc documents without JBake meta data.");
        out.close();
    }


    @Test
    void parseAsciidocFileWithPrettifyAttribute() {

        config.setProperty(ASCIIDOCTOR_ATTRIBUTES.getKey(), "source-highlighter=prettify");
        DocumentModel map = parser.processFile(asciidocWithSource);
        assertNotNull(map);
        assertEquals("draft", map.getStatus());
        assertEquals("post", map.getType());
        assertThat(map.getBody())
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>")
            .contains("class=\"prettyprint highlight\"");

        assertThat(map.getBody()).doesNotContain("I Love Jbake");
        System.out.println(map.getBody());
    }

    @Test
    void parseAsciidocFileWithCustomAttribute() {

        config.setProperty(ASCIIDOCTOR_ATTRIBUTES.getKey(), "source-highlighter=prettify,testattribute=I Love Jbake");
        DocumentModel map = parser.processFile(asciidocWithSource);
        assertNotNull(map);
        assertEquals("draft", map.getStatus());
        assertEquals("post", map.getType());
        assertThat(map.getBody())
            .contains("I Love Jbake")
            .contains("class=\"prettyprint highlight\"");

        System.out.println(map.getBody());
    }

    @Test
    void parseValidAsciiDocFile() {
        DocumentModel map = parser.processFile(validAsciidocFile);
        assertNotNull(map);
        assertEquals("draft", map.getStatus());
        assertEquals("post", map.getType());
        assertThat(map.getBody())
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>");
    }

    @Test
    void parseInvalidAsciiDocFile() {
        DocumentModel map = parser.processFile(invalidAsciiDocFile);
        assertNull(map);
    }

    @Test
    void parseValidAsciiDocFileWithoutHeader() {
        DocumentModel map = parser.processFile(validAsciiDocFileWithoutHeader);
        assertNotNull(map);
        assertEquals("Hello: AsciiDoc!", map.get("title"));
        assertEquals("published", map.getStatus());
        assertEquals("page", map.getType());
        assertThat(map.getBody())
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>");
    }

    @Test
    void parseInvalidAsciiDocFileWithoutHeader() {
        DocumentModel map = parser.processFile(invalidAsciiDocFileWithoutHeader);
        assertNull(map);
    }

    @Test
    void parseValidAsciiDocFileWithExampleHeaderInContent() {
        DocumentModel map = parser.processFile(validAsciiDocFileWithHeaderInContent);
        assertNotNull(map);
        assertEquals("published", map.getStatus());
        assertEquals("page", map.getType());
        assertThat(map.getBody())
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
    void parseValidAsciiDocFileWithoutJBakeMetaDataUsingDefaultTypeAndStatus() {
        config.setDefaultStatus("published");
        config.setDefaultType("page");
        Parser parser = new Parser(config);
        DocumentModel map = parser.processFile(validAsciiDocFileWithoutJBakeMetaData);
        assertNotNull(map);
        assertEquals("published", map.getStatus());
        assertEquals("page", map.getType());
        assertThat(map.getBody())
            .contains("<p>JBake now supports AsciiDoc documents without JBake meta data.</p>");
    }

}
