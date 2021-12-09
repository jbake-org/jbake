/*
 * The MIT License
 *
 * Copyright 2015 jdlee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jbake.app.template;

import org.apache.commons.io.FileUtils;
import org.jbake.app.ContentStoreIntegrationTest;
import org.jbake.app.Crawler;
import org.jbake.app.Parser;
import org.jbake.app.Renderer;
import org.jbake.engine.ModelExtractors;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.template.model.ModelExtractorsDocumentTypeListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jdlee
 */
public abstract class AbstractTemplateEngineRenderingTest extends ContentStoreIntegrationTest {

    protected final String templateDir;
    protected final String templateExtension;
    protected final Map<String, List<String>> outputStrings = new HashMap<>();

    protected File destinationFolder;
    protected File templateFolder;
    protected Renderer renderer;
    protected Locale currentLocale;
    private Parser parser;

    public AbstractTemplateEngineRenderingTest(String templateDir, String templateExtension) {
        this.templateDir = templateDir;
        this.templateExtension = templateExtension;
    }

    @Before
    public void setup() throws Exception {
        currentLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);

        ModelExtractorsDocumentTypeListener listener = new ModelExtractorsDocumentTypeListener();
        DocumentTypes.addListener(listener);

        templateFolder = new File(sourceFolder, templateDir);
        if (!templateFolder.exists()) {
            throw new Exception("Cannot find template folder!");
        }

        destinationFolder = folder.getRoot();
        config.setDestinationFolder(destinationFolder);
        config.setTemplateFolder(templateFolder);


        for (String docType : DocumentTypes.getDocumentTypes()) {
            File templateFile = config.getTemplateFileByDocType(docType);

            if (templateFile != null) {
                String fileName = templateFile.getName();
                String fileBaseName = fileName.substring(0, fileName.lastIndexOf("."));
                config.setTemplateFileNameForDocType(docType, fileBaseName + "." + templateExtension);
            }
        }

        config.setTemplateFileNameForDocType("paper", "paper." + templateExtension);
        DocumentTypes.addDocumentType("paper");
        db.updateSchema();

        Assert.assertEquals(".html", config.getOutputExtension());

        Crawler crawler = new Crawler(db, config);
        crawler.crawl();
        parser = new Parser(config);
        renderer = new Renderer(db, config);

        setupExpectedOutputStrings();
    }

    private void setupExpectedOutputStrings() {

        outputStrings.put("post", Arrays.asList("<h2>Second Post</h2>",
                "<p class=\"post-date\">28",
                "2013</p>",
                "Lorem ipsum dolor sit amet",
                "<h5>Published Posts</h5>",
                "blog/2012/first-post.html"));

        outputStrings.put("page", Arrays.asList("<h4>About</h4>",
                "All about stuff!",
                "<h5>Published Pages</h5>",
                "/projects.html"));

        outputStrings.put("index", Arrays.asList("<a href=\"blog/2016/another-post.html\"",
                ">Another Post</a>",
                "<a href=\"blog/2013/second-post.html\"",
                ">Second Post</a>"));

        outputStrings.put("feed", Arrays.asList("<description>My corner of the Internet</description>",
                "<title>Second Post</title>",
                "<title>First Post</title>"));

        outputStrings.put("archive", Arrays.asList("<a href=\"blog/2013/second-post.html\"",
                ">Second Post</a>",
                "<a href=\"blog/2012/first-post.html\"",
                ">First Post</a>"));

        outputStrings.put("tags", Arrays.asList("<a href=\"blog/2013/second-post.html\"",
                ">Second Post</a>",
                "<a href=\"blog/2012/first-post.html\"",
                ">First Post</a>"));

        outputStrings.put("tags-index", Arrays.asList("<h1>Tags</h1>",
                "<h2><a href=\"../tags/blog.html\">blog</a>",
                "3</h2>"));

        outputStrings.put("sitemap", Arrays.asList("blog/2013/second-post.html",
                "blog/2012/first-post.html",
                "papers/published-paper.html"));

    }

    @After
    public void cleanup() {
        DocumentTypes.resetDocumentTypes();
        ModelExtractors.getInstance().reset();
        Locale.setDefault(currentLocale);
    }

    @Test
    public void renderPost() throws Exception {
        // setup
        String filename = "second-post.html";

        File sampleFile = new File(sourceFolder.getPath() + File.separator + "content"
                + File.separator + "blog" + File.separator + "2013" + File.separator + filename);
        DocumentModel content = parser.processFile(sampleFile);
        content.setUri("/" + filename);
        renderer.render(content);
        File outputFile = new File(destinationFolder, filename);
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("post")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderPage() throws Exception {
        // setup
        String filename = "about.html";

        File sampleFile = new File(sourceFolder.getPath() + File.separator + "content" + File.separator + filename);
        DocumentModel content = parser.processFile(sampleFile);
        content.setUri("/" + filename);
        renderer.render(content);
        File outputFile = new File(destinationFolder, filename);
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("page")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderIndex() throws Exception {
        //exec
        renderer.renderIndex("index.html");

        //validate
        File outputFile = new File(destinationFolder, "index.html");
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("index")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderFeed() throws Exception {
        renderer.renderFeed("feed.xml");
        File outputFile = new File(destinationFolder, "feed.xml");
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("feed")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderArchive() throws Exception {
        renderer.renderArchive("archive.html");
        File outputFile = new File(destinationFolder, "archive.html");
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("archive")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderTags() throws Exception {
        renderer.renderTags("tags");

        // verify
        File outputFile = new File(destinationFolder + File.separator + "tags" + File.separator + "blog.html");
        Assert.assertTrue(outputFile.exists());
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("tags")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderTagsIndex() throws Exception {
        config.setRenderTagsIndex(true);

        renderer.renderTags("tags");
        File outputFile = new File(destinationFolder + File.separator + "tags" + File.separator + "index.html");
        Assert.assertTrue(outputFile.exists());
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("tags-index")) {
            assertThat(output).contains(string);
        }

    }

    @Test
    public void renderSitemap() throws Exception {
        DocumentTypes.addDocumentType("paper");
        db.updateSchema();

        renderer.renderSitemap("sitemap.xml");
        File outputFile = new File(destinationFolder, "sitemap.xml");
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("sitemap")) {
            assertThat(output).contains(string);
        }
        assertThat(output).doesNotContain("draft-paper.html");
    }

    protected List<String> getOutputStrings(String type) {
        return outputStrings.get(type);

    }

    @Test
    public void checkDbTemplateModelIsPopulated() throws Exception {

        config.setPaginateIndex(true);
        config.setPostsPerPage(1);

        outputStrings.put("dbSpan", Arrays.asList("<span>3</span>"));

        db.deleteAllByDocType("post");

        renderer.renderIndexPaging("index.html");

        File outputFile = new File(destinationFolder, "index.html");
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());

        for (String string : getOutputStrings("dbSpan")) {
            assertThat(output).contains(string);
        }

    }
}
