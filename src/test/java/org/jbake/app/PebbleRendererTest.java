package org.jbake.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FileUtils;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.model.DocumentTypes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class PebbleRendererTest {

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

        templateFolder = new File(sourceFolder, "pebbleTemplates");
        if (!templateFolder.exists()) {
            throw new Exception("Cannot find template folder!");
        }

        config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("template") && key.endsWith(".file")) {
                String old = (String)config.getProperty(key);
                config.setProperty(key, old.substring(0, old.length()-4)+".pebble");
            }
        }
        Assert.assertEquals(".html", config.getString(Keys.OUTPUT_EXTENSION));
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
        	.contains("<p class=\"post-date\">28")
        	.contains("2013</p>")
        	.contains("Lorem ipsum dolor sit amet")
        	.contains("<h5>Published Posts</h5>")
        	.contains("blog/2012/first-post.html");
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
	        .contains("<h1>About</h1>")
	    	.contains("All about stuff!")
	    	.contains("<h5>Published Pages</h5>")
	    	.contains("/projects.html");
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
        	.contains("<a href=\"blog/2012/first-post.html\"><h1>First Post</h1></a>")
                .contains("<a href=\"blog/2013/second-post.html\"><h1>Second Post</h1></a>");
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
        	.contains("<a href=\"blog/2013/second-post.html\">Second Post</a></li>")
        	.contains("<a href=\"blog/2012/first-post.html\">First Post</a></li>");
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
        	.contains("<a href=\"blog/2013/second-post.html\">Second Post</a></li>")
        	.contains("<a href=\"blog/2012/first-post.html\">First Post</a></li>");
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
