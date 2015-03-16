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
package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.jbake.app.ConfigUtil.Keys;
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
public class FreemarkerRendererTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File sourceFolder;
    private File destinationFolder;
    private File templateFolder;
    private CompositeConfiguration config;
    private ContentStore db;

    @Before
    public void setup() throws Exception, IOException, URISyntaxException {
        URL sourceUrl = this.getClass().getResource("/");

        sourceFolder = new File(sourceUrl.getFile());
        if (!sourceFolder.exists()) {
            throw new Exception("Cannot find sample data structure!");
        }

        destinationFolder = folder.getRoot();

        templateFolder = new File(sourceFolder, "freemarkerTemplates");
        if (!templateFolder.exists()) {
            throw new Exception("Cannot find template folder!");
        }

        config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("template") && key.endsWith(".file")) {
                String old = (String)config.getProperty(key);
                config.setProperty(key, old.substring(0, old.length()-4)+".ftl");
            }
        }
        config.setProperty(Keys.PAGINATE_INDEX, true);
        config.setProperty(Keys.POSTS_PER_PAGE, 1);
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

        File sampleFile = new File(sourceFolder.getPath() + File.separator + "content" + File.separator + "blog" + File.separator + "2013" + File.separator + filename);
        Map<String, Object> content = parser.processFile(sampleFile);
        content.put("uri", "/" + filename);
        renderer.render(content);
        File outputFile = new File(destinationFolder, filename);
        Assert.assertTrue(outputFile.exists());
        
        // verify
        String output = FileUtils.readFileToString(outputFile);
        assertThat(output) 
        	.contains("<h1>Second Post</h1>")
        	.contains("<p><em>28 February 2013</em></p>")
        	.contains("Lorem ipsum dolor sit amet");
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
        assertThat(output) 
	        .contains("<title>About</title>")
	    	.contains("All about stuff!");
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
        assertThat(output) 
        	.contains("<a href=\"blog/2012/first-post.html\">")
        	.contains("<a href=\"blog/2013/second-post.html\">");
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
        assertThat(output) 
        	.contains("<description>My corner of the Internet</description>")
        	.contains("<title>Second Post</title>")
        	.contains("<title>First Post</title>");
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
        assertThat(output) 
        	.contains("<a href=\"blog/2013/second-post.html\">")
        	.contains("<a href=\"blog/2012/first-post.html\">");
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
        assertThat(output) 
        	.contains("<a href=\"blog/2013/second-post.html\">")
        	.contains("<a href=\"blog/2012/first-post.html\">");
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
        assertThat(output) 
        	.contains("blog/2013/second-post.html")
        	.contains("blog/2012/first-post.html")
        	.contains("papers/published-paper.html")
        	.doesNotContain("draft-paper.html");
    }
    
}
