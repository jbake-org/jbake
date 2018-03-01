package org.jbake.app.template;

import org.apache.commons.io.FileUtils;
import org.jbake.app.Crawler;
import org.jbake.app.DBUtil;
import org.jbake.app.Parser;
import org.jbake.app.Renderer;
import org.jbake.model.DocumentTypes;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GroovyMarkupTemplateEngineRenderingTest extends AbstractTemplateEngineRenderingTest {

    public GroovyMarkupTemplateEngineRenderingTest() {
        super("groovyMarkupTemplates", "tpl");

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
        outputStrings.put("index", Arrays.asList("<h4><a href=\"blog/2012/first-post.html\">First Post</a></h4>",
                "<h4><a href=\"blog/2013/second-post.html\">Second Post</a></h4>"));
        outputStrings.put("feed", Arrays.asList("<description>My corner of the Internet</description>",
                "<title>Second Post</title>",
                "<title>First Post</title>"));
        outputStrings.put("archive", Arrays.asList("<a href=\"blog/2013/second-post.html\">Second Post</a></h4>",
                "<a href=\"blog/2012/first-post.html\">First Post</a></h4>"));
        outputStrings.put("tags", Arrays.asList("<a href=\"blog/2013/second-post.html\">Second Post</a></h4>",
                "<a href=\"blog/2012/first-post.html\">First Post</a></h4>"));
        outputStrings.put("sitemap", Arrays.asList("blog/2013/second-post.html",
                "blog/2012/first-post.html",
                "papers/published-fixture.groovyMarkupTemplates.paper.html"));
        outputStrings.put("paper", Arrays.asList("<h2>Published Paper</h2>",
                "<p class=\"post-date\">24",
                "2014</p>",
                "Lorem ipsum dolor sit amet",
                "<h5>Published Posts</h5>",
                "<li>Published Paper published</li>"));

    }

    @Test
    public void renderCustomTypePaper() throws Exception {
        // setup
        config.setProperty("template.paper.file", "paper." + templateExtension);
        DocumentTypes.addDocumentType("paper");
        DBUtil.updateSchema(db);

        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + "content"));
        Parser parser = new Parser(config, sourceFolder.getPath());
        Renderer renderer = new Renderer(db, destinationFolder, templateFolder, config);
        String filename = "published-paper.html";

        File sampleFile = new File(sourceFolder.getPath() + File.separator + "content" + File.separator + "papers" + File.separator + filename);
        Map<String, Object> content = parser.processFile(sampleFile);
        content.put(Crawler.Attributes.URI, "/" + filename);
        renderer.render(content);
        File outputFile = new File(destinationFolder, filename);
        Assert.assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("paper")) {
            assertThat(output).contains(string);
        }

    }


}
