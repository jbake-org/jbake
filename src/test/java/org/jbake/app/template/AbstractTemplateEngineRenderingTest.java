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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.jbake.app.ConfigUtil;
import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.DBUtil;
import org.jbake.app.Parser;
import org.jbake.app.Renderer;
import org.jbake.model.DocumentTypes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author jdlee
 */
public abstract class AbstractTemplateEngineRenderingTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File sourceFolder;
    private File destinationFolder;
    private File templateFolder;
    private CompositeConfiguration config;
    private ContentStore db;

    private final String templateDir;
    private final String templateExtension;
    protected final Map<String, List<String>> outputStrings = new HashMap<String, List<String>>();

    public AbstractTemplateEngineRenderingTest(String templateDir, String templateExtension) {
        this.templateDir = templateDir;
        this.templateExtension = templateExtension;
    }

    @Before
    public void setup() throws Exception, IOException, URISyntaxException {
        URL sourceUrl = this.getClass().getResource("/");

        sourceFolder = new File(sourceUrl.getFile());
        if (!sourceFolder.exists()) {
            throw new Exception("Cannot find sample data structure!");
        }

        destinationFolder = folder.getRoot();

        templateFolder = new File(sourceFolder, templateDir);
        if (!templateFolder.exists()) {
            throw new Exception("Cannot find template folder!");
        }

        config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("template") && key.endsWith(".file")) {
                String old = (String) config.getProperty(key);
                config.setProperty(key, old.substring(0, old.length() - 4) + "." + templateExtension);
            }
        }
        Assert.assertEquals(".html", config.getString(ConfigUtil.Keys.OUTPUT_EXTENSION));
        db = DBUtil.createDataStore("memory", "documents"+System.currentTimeMillis());
    }

    @After
    public void cleanup() throws InterruptedException {
        db.drop();
        db.close();
    }

    @Test
    public void renderPost() throws Exception {
        // setup
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Parser parser = new Parser(config, sourceFolder.getPath());
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        String filename = "second-post.html";

        File sampleFile = new File(sourceFolder.getPath() + File.separator + "content"
                + File.separator + "blog" + File.separator + "2013" + File.separator + filename);
        Map<String, Object> content = parser.processFile(sampleFile);
        content.put("uri", "/" + filename);
        renderer.render(content);
        File outputFile = new File(destinationFolder, filename);
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile);
        for (String string : getOutputStrings("post")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderPage() throws Exception {
        // setup
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Parser parser = new Parser(config, sourceFolder.getPath());
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        String filename = "about.html";

        File sampleFile = new File(sourceFolder.getPath() + File.separator + "content" + File.separator + filename);
        Map<String, Object> content = parser.processFile(sampleFile);
        content.put("uri", "/" + filename);
        renderer.render(content);
        File outputFile = new File(destinationFolder, filename);
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile);
        for (String string : getOutputStrings("page")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderIndex() throws Exception {
        //setup
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        //exec
        renderer.renderIndex("index.html");

        //validate
        File outputFile = new File(destinationFolder, "index.html");
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile);
        for (String string : getOutputStrings("index")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderFeed() throws Exception {
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        renderer.renderFeed("feed.xml");
        File outputFile = new File(destinationFolder, "feed.xml");
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile);
        for (String string : getOutputStrings("feed")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderArchive() throws Exception {
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        renderer.renderArchive("archive.html");
        File outputFile = new File(destinationFolder, "archive.html");
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile);
        for (String string : getOutputStrings("archive")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderTags() throws Exception {
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        renderer.renderTags(crawler.getTags(), "tags");

        // verify
        File outputFile = new File(destinationFolder + File.separator + "tags" + File.separator + "blog.html");
        Assert.assertTrue(outputFile.exists());
        String output = FileUtils.readFileToString(outputFile);
        for (String string : getOutputStrings("tags")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void renderSitemap() throws Exception {
        DocumentTypes.addDocumentType("paper");
        DBUtil.updateSchema(db);

        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        renderer.renderSitemap("sitemap.xml");
        File outputFile = new File(destinationFolder, "sitemap.xml");
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile);
        for (String string : getOutputStrings("sitemap")) {
            assertThat(output).contains(string);
        }
        assertThat(output).doesNotContain("draft-paper.html");
    }

    private List<String> getOutputStrings(String type) {
        return outputStrings.get(type);
        
    }
}
