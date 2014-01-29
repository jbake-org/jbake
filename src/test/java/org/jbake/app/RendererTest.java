package org.jbake.app;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.apache.commons.configuration.CompositeConfiguration;
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
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RendererTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File sourceFolder;
    private File destinationFolder;
    private File templateFolder;
    private CompositeConfiguration config;
    private ODatabaseDocumentTx db;

    @Before
    public void setup() throws Exception, IOException, URISyntaxException {
        URL sourceUrl = this.getClass().getResource("/");

        sourceFolder = new File(sourceUrl.getFile());
        if (!sourceFolder.exists()) {
            throw new Exception("Cannot find sample data structure!");
        }

        destinationFolder = folder.getRoot();

        templateFolder = new File(sourceFolder, "templates");
        if (!templateFolder.exists()) {
            throw new Exception("Cannot find template folder!");
        }

        config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
        Assert.assertEquals(".html", config.getString("output.extension"));
        db = DBUtil.createDB("memory", "documents"+System.currentTimeMillis());
    }

    @After
    public void cleanup() throws InterruptedException {
        db.drop();
        db.close();
    }

    @Test
    public void render() throws Exception {
        Parser parser = new Parser(config, sourceFolder.getPath());
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);

        File sampleFile = new File(sourceFolder.getPath() + File.separator + "content" + File.separator + "blog" + File.separator + "2013" + File.separator + "second-post.html");
        Map<String, Object> content = parser.processFile(sampleFile);
        content.put("uri", "/second-post.html");
        renderer.render(content);
        File outputFile = new File(destinationFolder, "second-post.html");
        Assert.assertTrue(outputFile.exists());
        Scanner scanner = new Scanner(outputFile);
        boolean foundTitle = false;
        boolean foundDate = false;
        boolean foundBody = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("<h2>Second Post</h2>")) {
                foundTitle = true;
            }
            if (line.trim().startsWith("<p class=\"post-date\">28") && line.endsWith("2013</p>")) {
                foundDate = true;
            }
            if (line.contains("Lorem ipsum dolor sit amet")) {
                foundBody = true;
            }
            if (foundTitle && foundDate && foundBody) {
                break;
            }
        }

        Assert.assertTrue(foundTitle);
        Assert.assertTrue(foundDate);
        Assert.assertTrue(foundBody);
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
        Scanner scanner = new Scanner(outputFile);

        boolean foundFirstTitle = false;
        boolean foundSecondTitle = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("<h4><a href=\"/blog/2012/first-post.html\">First Post</a></h4>")) {
                foundFirstTitle = true;
            }
            if (line.contains("<h4><a href=\"/blog/2013/second-post.html\">Second Post</a></h4>")) {
                foundSecondTitle = true;
            }
            if (foundFirstTitle && foundSecondTitle) {
                break;
            }
        }

        Assert.assertTrue(foundFirstTitle);
        Assert.assertTrue(foundSecondTitle);
    }

    @Test
    public void renderFeed() throws Exception {
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        renderer.renderFeed("feed.xml");
        File outputFile = new File(destinationFolder, "feed.xml");
        Assert.assertTrue(outputFile.exists());
        Scanner scanner = new Scanner(outputFile);

        boolean foundDescription = false;
        boolean foundFirstTitle = false;
        boolean foundSecondTitle = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("<description>My corner of the Internet</description>")) {
                foundDescription = true;
            }
            if (line.contains("<title>Second Post</title>")) {
                foundFirstTitle = true;
            }
            if (line.contains("<title>First Post</title>")) {
                foundSecondTitle = true;
            }
            if (foundDescription && foundFirstTitle && foundSecondTitle) {
                break;
            }
        }

        Assert.assertTrue(foundDescription);
        Assert.assertTrue(foundFirstTitle);
        Assert.assertTrue(foundSecondTitle);
    }

    @Test
    public void renderArchive() throws Exception {
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        renderer.renderArchive("archive.html");
        File outputFile = new File(destinationFolder, "archive.html");
        Assert.assertTrue(outputFile.exists());
        Scanner scanner = new Scanner(outputFile);

        boolean foundFirstPost = false;
        boolean foundSecondPost = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("<a href=\"/blog/2013/second-post.html\">Second Post</a></h4>")) {
                foundFirstPost = true;
            }
            if (line.contains("<a href=\"/blog/2012/first-post.html\">First Post</a></h4>")) {
                foundSecondPost = true;
            }
            if (foundFirstPost && foundSecondPost) {
                break;
            }
        }

        Assert.assertTrue(foundFirstPost);
        Assert.assertTrue(foundSecondPost);
    }

    @Test
    public void renderTags() throws Exception {
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        renderer.renderTags(crawler.getTags(), "tags");
        File outputFile = new File(destinationFolder + File.separator + "tags" + File.separator + "blog.html");
        Assert.assertTrue(outputFile.exists());
        Scanner scanner = new Scanner(outputFile);

        boolean foundFirstPost = false;
        boolean foundSecondPost = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("<a href=\"/blog/2013/second-post.html\">Second Post</a></h4>")) {
                foundFirstPost = true;
            }
            if (line.contains("<a href=\"/blog/2012/first-post.html\">First Post</a></h4>")) {
                foundSecondPost = true;
            }
            if (foundFirstPost && foundSecondPost) {
                break;
            }
        }

        Assert.assertTrue(foundFirstPost);
        Assert.assertTrue(foundSecondPost);
    }
}
