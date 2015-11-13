package org.jbake.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class GroovyRendererTest {

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

        templateFolder = new File(sourceFolder, "groovyTemplates");
        if (!templateFolder.exists()) {
            throw new Exception("Cannot find template folder!");
        }

        config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("template") && key.endsWith(".file")) {
                String old = (String)config.getProperty(key);
                config.setProperty(key, old.substring(0, old.length()-4)+".gsp");
            }
        }
        Assert.assertEquals(".html", config.getString(Keys.OUTPUT_EXTENSION));
        db = DBUtil.createDB("memory", "documents"+System.currentTimeMillis());
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
        	.contains("<h2>Second Post</h2>")
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
	        .contains("<h4>About</h4>")
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
            .contains("<h4><a href=\"blog/2012/first-post.html\">First Post</a></h4>")
            .contains("<p>28/02/2013 -")
            .contains("<h4><a href=\"blog/2013/second-post.html\">Second Post</a></h4>")
            .contains("<p>27/02/2012 -")
            .contains("Lorem ipsum dolor sit amet")
            .contains("Aliquam erat volutpat.");
    }

    @Test 
    public void renderIndexWithExcerpt() throws Exception {
        //setup
        config.setProperty(Keys.INDEX_SUMMERY, true);
        config.setProperty(ContentTag.summaryLength.key(), 60);
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
            .contains("<h4><a href=\"blog/2012/first-post.html\">First Post</a></h4>")
            .contains("<p>28/02/2013 -")
            .contains("<h4><a href=\"blog/2013/second-post.html\">Second Post</a></h4>")
            .contains("<p>27/02/2012 -")
            .contains("Lorem ipsum dolor sit amet")
            .contains("ultricies a hendrerit quam iaculis")
            .doesNotContain("Duis tempor elit sit amet")
            .doesNotContain("Aliquam erat volutpat.");
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
            .contains("<title>First Post</title>")
            .contains("Lorem ipsum dolor sit amet")
            .contains("Aliquam erat volutpat.");
    }

    @Test
    public void renderFeedWithExcerpt() throws Exception {
        config.setProperty(Keys.FEED_SUMMERY, true);
        config.setProperty(ContentTag.summaryLength.key(), 60);
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
        .contains("<title>First Post</title>")
        .contains("Lorem ipsum dolor sit amet")
        .contains("ultricies a hendrerit quam iaculis")
        .doesNotContain("Duis tempor elit sit amet")
        .doesNotContain("Aliquam erat volutpat.");
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
        	.contains("<a href=\"blog/2013/second-post.html\">Second Post</a></h4>")
        	.contains("<a href=\"blog/2012/first-post.html\">First Post</a></h4>");
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
        	.contains("<a href=\"blog/2013/second-post.html\">Second Post</a></h4>")
        	.contains("<a href=\"blog/2012/first-post.html\">First Post</a></h4>");
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
