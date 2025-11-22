package org.jbake.app.template

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions
import org.jbake.app.Crawler
import org.jbake.app.Parser
import org.jbake.app.Renderer
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

class GroovyMarkupTemplateEngineRenderingTest : AbstractTemplateEngineRenderingTest("groovyMarkupTemplates", "tpl") {
    init {
        outputStrings["post"] = mutableListOf<String>(
            "<h2>Second Post</h2>",
            "<p class=\"post-date\">28",
            "2013</p>",
            "Lorem ipsum dolor sit amet",
            "<h5>Published Posts</h5>",
            "blog/2012/first-post.html"
        )
        outputStrings["page"] = mutableListOf<String>(
            "<h4>About</h4>",
            "All about stuff!",
            "<h5>Published Pages</h5>",
            "/projects.html"
        )
        outputStrings["index"] = mutableListOf<String>(
            "<h4><a href=\"blog/2012/first-post.html\">First Post</a></h4>",
            "<h4><a href=\"blog/2013/second-post.html\">Second Post</a></h4>"
        )
        outputStrings["feed"] = mutableListOf<String>(
            "<description>My corner of the Internet</description>",
            "<title>Second Post</title>",
            "<title>First Post</title>"
        )
        outputStrings["archive"] = mutableListOf<String>(
            "<a href=\"blog/2013/second-post.html\">Second Post</a></h4>",
            "<a href=\"blog/2012/first-post.html\">First Post</a></h4>"
        )
        outputStrings["tags"] = mutableListOf<String>(
            "<a href=\"blog/2013/second-post.html\">Second Post</a></h4>",
            "<a href=\"blog/2012/first-post.html\">First Post</a></h4>"
        )
        outputStrings["sitemap"] = mutableListOf<String>(
            "blog/2013/second-post.html",
            "blog/2012/first-post.html",
            "papers/published-fixture.groovyMarkupTemplates.paper.html"
        )
        outputStrings["paper"] = mutableListOf<String>(
            "<h2>Published Paper</h2>",
            "<p class=\"post-date\">24",
            "2014</p>",
            "Lorem ipsum dolor sit amet",
            "<h5>Published Posts</h5>",
            "<li>Published Paper published</li>"
        )
    }

    @Test
    fun renderCustomTypePaper() {
        // setup
        val crawler = Crawler(db, config)
        crawler.crawl()
        val parser = Parser(config)
        val renderer = Renderer(db, config)
        val filename = "published-paper.html"

        val sampleFile = File(sourceFolder!!.path + File.separator + "content" + File.separator + "papers" + File.separator + filename)
        val content = parser.processFile(sampleFile)
        content!!.uri = "/" + filename
        renderer.render(content)
        val outputFile = File(destinationFolder, filename)
        Assert.assertTrue(outputFile.exists())

        // verify
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("paper")) {
            Assertions.assertThat(output).contains(string)
        }
    }
}
