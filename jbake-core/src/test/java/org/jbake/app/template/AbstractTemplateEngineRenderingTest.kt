package org.jbake.app.template

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.jbake.app.ContentStoreIntegrationTest
import org.jbake.app.Crawler
import org.jbake.app.Parser
import org.jbake.app.Renderer
import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.DocumentTypes.addListener
import org.jbake.model.DocumentTypes.documentTypes
import org.jbake.model.DocumentTypes.resetDocumentTypes
import org.jbake.template.ModelExtractors
import org.jbake.template.ModelExtractorsDocumentTypeListener
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * @author jdlee
 */
abstract class AbstractTemplateEngineRenderingTest(
    protected val templateDir: String? = null,
    protected val templateExtension: String? = null,
) : ContentStoreIntegrationTest() {
    protected val outputStrings: MutableMap<String, MutableList<String>> = HashMap()

    protected lateinit var destinationFolder: File
    protected lateinit var templateFolder: File
    protected lateinit var renderer: Renderer
    protected lateinit var currentLocale: Locale
    private lateinit var parser: Parser

    @Before
    fun setup() {
        currentLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)

        val listener = ModelExtractorsDocumentTypeListener()
        addListener(listener)

        templateFolder = File(sourceFolder, templateDir)
        if (!templateFolder.exists()) throw Exception("Cannot find template folder!")

        destinationFolder = folder.root
        config.destinationFolder = (destinationFolder)
        config.templateFolder = (templateFolder)


        for (docType in documentTypes) {
            val templateFile: File? = config.getTemplateFileByDocType(docType)

            if (templateFile != null) {
                val fileName = templateFile.getName()
                val fileBaseName = fileName.take(fileName.lastIndexOf("."))
                config.setTemplateFileNameForDocType(
                    docType,
                    "$fileBaseName.$templateExtension"
                )
            }
        }

        config.setTemplateFileNameForDocType("paper", "paper.$templateExtension")
        addDocumentType("paper")
        db.updateSchema()

        assertEquals(".html", config.outputExtension)

        val crawler = Crawler(db, config)
        crawler.crawlContentDirectory()
        parser = Parser(config)
        renderer = Renderer(db, config)

        setupExpectedOutputStrings()
    }

    private fun setupExpectedOutputStrings() {
        outputStrings["post"] = mutableListOf(
            "<h2>Second Post</h2>",
            "<p class=\"post-date\">28",
            "2013</p>",
            "Lorem ipsum dolor sit amet",
            "<h5>Published Posts</h5>",
            "blog/2012/first-post.html"
        )

        outputStrings["page"] = mutableListOf(
            "<h4>About</h4>",
            "All about stuff!",
            "<h5>Published Pages</h5>",
            "/projects.html"
        )

        outputStrings["index"] = mutableListOf(
            "<a href=\"blog/2016/another-post.html\"",
            ">Another Post</a>",
            "<a href=\"blog/2013/second-post.html\"",
            ">Second Post</a>"
        )

        outputStrings["feed"] = mutableListOf(
            "<description>My corner of the Internet</description>",
            "<title>Second Post</title>",
            "<title>First Post</title>"
        )

        outputStrings["archive"] = mutableListOf(
            "<a href=\"blog/2013/second-post.html\"",
            ">Second Post</a>",
            "<a href=\"blog/2012/first-post.html\"",
            ">First Post</a>"
        )

        outputStrings["tags"] = mutableListOf(
            "<a href=\"blog/2013/second-post.html\"",
            ">Second Post</a>",
            "<a href=\"blog/2012/first-post.html\"",
            ">First Post</a>"
        )

        outputStrings["tags-index"] = mutableListOf(
            "<h1>Tags</h1>",
            "<h2><a href=\"../tags/blog.html\">blog</a>",
            "3</h2>"
        )

        outputStrings["sitemap"] = mutableListOf(
            "blog/2013/second-post.html",
            "blog/2012/first-post.html",
            "papers/published-paper.html"
        )
    }

    @After
    fun cleanup() {
        resetDocumentTypes()
        ModelExtractors.instance.reset()
        Locale.setDefault(currentLocale)
    }

    @Test fun renderPost() {
        // setup
        val filename = "second-post.html"

        val sampleFile = sourceFolder!!.resolve("content").resolve("blog").resolve("2013").resolve(filename)
        val content = parser.processFile(sampleFile)
        content!!.uri = filename
        renderer.render(content)
        val outputFile = File(destinationFolder, filename)
        assertTrue(outputFile.exists())

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("post")) {
            assertThat(output).contains(string)
        }
    }

    @Test fun renderPage() {
        // setup
        val filename = "about.html"
        val sampleFile = sourceFolder!!.resolve("content").resolve(filename)

        // When
        val content = parser.processFile(sampleFile)
        content!!.uri = filename
        renderer!!.render(content)
        val outputFile = File(destinationFolder, filename)
        assertTrue(outputFile.exists())

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("page")) {
            assertThat(output).contains(string)
        }
    }

    @Test fun renderIndex() {
        //exec
        renderer.renderIndex("index.html")

        //validate
        val outputFile = File(destinationFolder, "index.html")
        assertTrue(outputFile.exists())

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("index")) {
            assertThat(output).contains(string)
        }
    }

    @Test fun renderFeed() {
        renderer.renderFeed("feed.xml")
        val outputFile = File(destinationFolder, "feed.xml")
        assertTrue(outputFile.exists())

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("feed")) {
            assertThat(output).contains(string)
        }
    }

    @Test fun renderArchive() {
        renderer.renderArchive("archive.html")
        val outputFile = File(destinationFolder, "archive.html")
        assertTrue(outputFile.exists())

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("archive")) {
            assertThat(output).contains(string)
        }
    }

    @Test fun renderTags() {
        renderer.renderTags("tags")

        // Then
        val outputFile = destinationFolder.resolve("tags").resolve("blog.html")
        assertTrue(outputFile.exists())
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("tags")) {
            assertThat(output).contains(string)
        }
    }

    @Test fun renderTagsIndex() {
        config.setRenderTagsIndex(true)

        renderer.renderTags("tags")
        val outputFile = destinationFolder.resolve("tags").resolve("index.html")
        assertTrue(outputFile.exists())
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("tags-index")) {
            assertThat(output).contains(string)
        }
    }

    @Test fun renderSitemap() {
        addDocumentType("paper")
        db.updateSchema()

        renderer.renderSitemap("sitemap.xml")
        val outputFile = File(destinationFolder, "sitemap.xml")
        assertTrue(outputFile.exists())

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("sitemap")) {
            assertThat(output).contains(string)
        }
        assertThat(output).doesNotContain("draft-paper.html")
    }

    protected fun getOutputStrings(type: String): MutableList<String> {
        return outputStrings[type] ?: mutableListOf()
    }

    @Test fun checkDbTemplateModelIsPopulated() {
        config.setPaginateIndex(true)
        config.setPostsPerPage(1)

        outputStrings["dbSpan"] = mutableListOf("<span>3</span>")

        db.deleteAllByDocType("post")

        renderer.renderIndexPaging("index.html")

        val outputFile = File(destinationFolder, "index.html")
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())

        for (string in getOutputStrings("dbSpan")) {
            assertThat(output).contains(string)
        }
    }
}
