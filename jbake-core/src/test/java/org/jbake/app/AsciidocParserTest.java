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
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AsciidocParserTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private DefaultJBakeConfiguration config;
    private Parser parser;
    private File rootPath;

    private File validAsciiDocFile;

    private String validHeader = "title=This is a Title = This is a valid Title\nstatus=draft\ntype=post\ndate=2013-09-02\n~~~~~~";

    @Before
    public void createSampleFile() throws Exception {
        rootPath = TestUtils.getTestResourcesAsSourceFolder();
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(rootPath);
        parser = new Parser(config);

        validAsciiDocFile = folder.newFile("asciidoc-with-source.ad");
        PrintWriter out = new PrintWriter(validAsciiDocFile);
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

    }


    @Test
    public void parseAsciidocFileWithPrettifyAttribute() {

        config.setProperty(JBakeProperty.ASCIIDOCTOR_ATTRIBUTES,"source-highlighter=prettify");
        Map<String, Object> map = parser.processFile(validAsciiDocFile);
        Assert.assertNotNull(map);
        Assert.assertEquals("draft", map.get("status"));
        Assert.assertEquals("post", map.get("type"));
        assertThat(map.get("body").toString())
                .contains("class=\"paragraph\"")
                .contains("<p>JBake now supports AsciiDoc.</p>")
                .contains("class=\"prettyprint highlight\"");

        System.out.println(map.get("body").toString());
    }

    @Test
    public void parseAsciidocFileWithCustomAttribute() {

        config.setProperty(JBakeProperty.ASCIIDOCTOR_ATTRIBUTES,"source-highlighter=prettify,testattribute=I Love Jbake");
        Map<String, Object> map = parser.processFile(validAsciiDocFile);
        Assert.assertNotNull(map);
        Assert.assertEquals("draft", map.get("status"));
        Assert.assertEquals("post", map.get("type"));
        assertThat(map.get("body").toString())
                .contains("I Love Jbake")
                .contains("class=\"prettyprint highlight\"");

        System.out.println(map.get("body").toString());
    }


}
