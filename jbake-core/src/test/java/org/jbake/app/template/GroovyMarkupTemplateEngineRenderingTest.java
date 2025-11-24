package org.jbake.app.template;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.jbake.app.Crawler;
import org.jbake.app.Parser;
import org.jbake.app.Renderer;
import org.jbake.model.DocumentModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroovyMarkupTemplateEngineRenderingTest extends AbstractTemplateEngineRenderingTest {

    GroovyMarkupTemplateEngineRenderingTest() {
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
    void renderCustomTypePaper() throws Exception {
        // setup


        Crawler crawler = new Crawler(db, config);
        crawler.crawl();
        Parser parser = new Parser(config);
        Renderer renderer = new Renderer(db, config);
        String filename = "published-paper.html";

        File sampleFile = new File(sourceFolder.getPath() + File.separator + "content" + File.separator + "papers" + File.separator + filename);
        DocumentModel content = parser.processFile(sampleFile);
        content.setUri("/" + filename);
        renderer.render(content);
        File outputFile = new File(destinationFolder, filename);
        assertTrue(outputFile.exists());

        // verify
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
        for (String string : getOutputStrings("paper")) {
            assertThat(output).contains(string);
        }

    }


}
