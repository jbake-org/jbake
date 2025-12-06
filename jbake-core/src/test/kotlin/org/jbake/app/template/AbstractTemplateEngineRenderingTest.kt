package org.jbake.app.template

import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.apache.commons.io.FileUtils
import org.jbake.app.ContentStoreIntegrationTest
import org.jbake.app.Crawler
import org.jbake.parser.Parser
import org.jbake.app.Renderer
import org.jbake.model.DocumentTypeRegistry
import org.jbake.model.DocumentTypeRegistry.addDocumentType
import org.jbake.model.DocumentTypeRegistry.addListener
import org.jbake.model.DocumentTypeRegistry.documentTypes
import org.jbake.model.DocumentTypeRegistry.resetDocumentTypes
import org.jbake.model.ModelExtractorsDocumentTypeListener
import org.jbake.template.ModelExtractorsRegistry
import java.io.File
import java.nio.charset.Charset
import java.util.*

abstract class AbstractTemplateEngineRenderingTest(
    protected val templateDir: String,
    protected val templateExtension: String? = null,
) : ContentStoreIntegrationTest() {

    protected val expectedInOutput: MutableMap<String, MutableList<String>> = HashMap()

    protected lateinit var destinationDir_: File
    protected lateinit var templateDir_: File
    protected lateinit var renderer: Renderer
    protected lateinit var currentLocale: Locale
    private lateinit var parser: Parser

    protected fun setup() {
        currentLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)

        val listener = ModelExtractorsDocumentTypeListener()
        DocumentTypeRegistry.clearListenersForTests()
        addListener(listener)

        templateDir_ = sourceDir.resolve(templateDir)
        if (!templateDir_.exists()) throw Exception("Cannot find template folder!")

        destinationDir_ = tempDir
        config.destinationDir = (destinationDir_)
        config.templateDir = (templateDir_)

        for (docType in documentTypes) {
            val templateFile: File = config.getTemplateFileByDocType(docType)
                ?: continue
            val fileName = templateFile.getName()
            val fileBaseName = fileName.take(fileName.lastIndexOf("."))
            config.setTemplateFileNameForDocType(docType, "$fileBaseName.$templateExtension")
        }

        config.setTemplateFileNameForDocType("paper", "paper.$templateExtension")
        addDocumentType("paper")
        db.updateSchema()

        config.outputExtension shouldBe ".html"

        val crawler = Crawler(db, config)
        crawler.crawlContentDirectory()
        parser = Parser(config)
        renderer = Renderer(db, config)

        setupExpectedOutputStrings()
    }

    private fun setupExpectedOutputStrings() {
        expectedInOutput["post"] = mutableListOf(
            "<h2>Second Post</h2>",
            "<p class=\"post-date\">28",
            "2013</p>",
            "Lorem ipsum dolor sit amet",
            "<h5>Published Posts</h5>",
            "blog/2012/first-post.html"
        )

        expectedInOutput["page"] = mutableListOf(
            "<h4>About</h4>",
            "All about stuff!",
            "<h5>Published Pages</h5>",
            "/projects.html"
        )

        expectedInOutput["index"] = mutableListOf(
            "<a href=\"blog/2016/another-post.html\"",
            ">Another Post</a>",
            "<a href=\"blog/2013/second-post.html\"",
            ">Second Post</a>"
        )

        expectedInOutput["feed"] = mutableListOf(
            "<description>My corner of the Internet</description>",
            "<title>Second Post</title>",
            "<title>First Post</title>"
        )

        expectedInOutput["archive"] = mutableListOf(
            "<a href=\"blog/2013/second-post.html\"",
            ">Second Post</a>",
            "<a href=\"blog/2012/first-post.html\"",
            ">First Post</a>"
        )

        expectedInOutput["tags"] = mutableListOf(
            "<a href=\"blog/2013/second-post.html\"",
            ">Second Post</a>",
            "<a href=\"blog/2012/first-post.html\"",
            ">First Post</a>"
        )

        expectedInOutput["tags-index"] = mutableListOf(
            "<h1>Tags</h1>",
            "<h2><a href=\"../tags/blog.html\">blog</a>",
            "3</h2>"
        )

        expectedInOutput["sitemap"] = mutableListOf(
            "blog/2013/second-post.html",
            "blog/2012/first-post.html",
            "papers/published-paper.html"
        )
    }

    protected fun cleanup() {
        resetDocumentTypes()
        ModelExtractorsRegistry.instance.reset()
        Locale.setDefault(currentLocale)
    }

    protected fun testRenderPost() {
        // setup
        val filename = "second-post.html"

        val sampleFile = sourceDir.resolve("content").resolve("blog").resolve("2013").resolve(filename)
        val content = parser.processFile(sampleFile)
        content!!.uri = filename
        renderer.render(content)
        val outputFile = File(destinationDir_, filename)
        outputFile.shouldExist()

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getExpectedInOutput("post")) {
            output shouldContain string
        }
    }

    protected fun testRenderPage() {
        // setup
        val filename = "about.html"
        val sampleFile = sourceDir.resolve("content").resolve(filename)

        // When
        val content = parser.processFile(sampleFile)
        content!!.uri = filename
        renderer.render(content)
        val outputFile = File(destinationDir_, filename)
        outputFile.shouldExist()

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getExpectedInOutput("page")) {
            output shouldContain string
        }
    }

    protected fun testRenderIndex() {
        //exec
        renderer.renderIndex("index.html")

        //validate
        val outputFile = File(destinationDir_, "index.html")
        outputFile.shouldExist()

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (expectedSnippet in getExpectedInOutput("index")) {
            output shouldContain expectedSnippet
        }
    }

    protected fun testRenderFeed() {
        renderer.renderFeed("feed.xml")
        val outputFile = File(destinationDir_, "feed.xml")
        outputFile.shouldExist()

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getExpectedInOutput("feed")) {
            output shouldContain string
        }
    }

    protected fun testRenderArchive() {
        renderer.renderArchive("archive.html")
        val outputFile = File(destinationDir_, "archive.html")
        outputFile.shouldExist()

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getExpectedInOutput("archive")) {
            output shouldContain string
        }
    }

    protected fun testRenderTags() {
        renderer.renderTags("tags")

        // Then
        val outputFile = destinationDir_.resolve("tags").resolve("blog.html")
        outputFile.shouldExist()
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getExpectedInOutput("tags")) {
            output shouldContain string
        }
    }

    protected fun testRenderTagsIndex() {
        config.setRenderTagsIndex(true)

        renderer.renderTags("tags")
        val outputFile = destinationDir_.resolve("tags").resolve("index.html")
        outputFile.shouldExist()
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getExpectedInOutput("tags-index")) {
            output shouldContain string
        }
    }

    protected fun testRenderSitemap() {
        addDocumentType("paper")
        db.updateSchema()

        renderer.renderSitemap("sitemap.xml")
        val outputFile = File(destinationDir_, "sitemap.xml")
        outputFile.shouldExist()

        // Then
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (snippet in getExpectedInOutput("sitemap")) {
            output shouldContain snippet
        }
        output shouldNotContain "draft-paper.html"
    }

    protected fun getExpectedInOutput(type: String): MutableList<String> {
        return expectedInOutput[type] ?: mutableListOf()
    }

    protected fun testCheckDbTemplateModelIsPopulated() {
        config.setPaginateIndex(true)
        config.setPostsPerPage(1)

        expectedInOutput["dbSpan"] = mutableListOf("<span>3</span>")

        db.deleteAllByDocType("post")

        renderer.renderIndexPaging("index.html")

        val outputFile = File(destinationDir_, "index.html")
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())

        for (string in getExpectedInOutput("dbSpan")) {
            output shouldContain string
        }
    }
}
