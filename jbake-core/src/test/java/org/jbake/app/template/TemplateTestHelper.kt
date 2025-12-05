package org.jbake.app.template

import io.kotest.matchers.shouldBe
import org.jbake.app.*
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.model.DocumentTypeRegistry
import org.jbake.model.ModelExtractorsDocumentTypeListener
import org.jbake.parser.Parser
import org.jbake.template.ModelExtractors
import java.io.File
import java.util.*

/**
 * Helper class for template engine rendering tests
 * Replaces AbstractTemplateEngineRenderingTest
 */
class TemplateTestHelper(
    private val templateDir: String,
    private val templateExtension: String
) {
    val expectedInOutput: MutableMap<String, MutableList<String>> = HashMap()

    lateinit var destinationDir: File
    lateinit var templateDir_: File
    lateinit var renderer: Renderer
    lateinit var db: ContentStore
    lateinit var config: DefaultJBakeConfiguration
    private lateinit var currentLocale: Locale
    private lateinit var parser: Parser
    private lateinit var sourceDir: File

    fun setupClass() {
        ContentStoreIntegrationTest.setUpClass()
        db = ContentStoreIntegrationTest.db
        config = ContentStoreIntegrationTest.config
        sourceDir = ContentStoreIntegrationTest.sourceDir
    }

    fun setupTest() {
        db.startup()

        currentLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)

        val listener = ModelExtractorsDocumentTypeListener()
        DocumentTypeRegistry.clearListenersForTests()
        DocumentTypeRegistry.addListener(listener)

        templateDir_ = sourceDir.resolve(templateDir)
        if (!templateDir_.exists()) throw Exception("Cannot find template folder!")

        destinationDir = ContentStoreIntegrationTest.tempDir
        config.destinationDir = destinationDir
        config.templateDir = templateDir_

        for (docType in DocumentTypeRegistry.documentTypes) {
            val templateFile: File? = config.getTemplateFileByDocType(docType)

            if (templateFile != null) {
                val fileName = templateFile.name
                val fileBaseName = fileName.take(fileName.lastIndexOf("."))
                config.setTemplateFileNameForDocType(
                    docType,
                    "$fileBaseName.$templateExtension"
                )
            }
        }

        config.setTemplateFileNameForDocType("paper", "paper.$templateExtension")
        DocumentTypeRegistry.addDocumentType("paper")
        db.updateSchema()

        config.outputExtension shouldBe ".html"

        val crawler = Crawler(db, config)
        crawler.crawlContentDirectory()
        parser = Parser(config)
        renderer = Renderer(db, config)

        setupExpectedOutputStrings()
    }

    fun teardownTest() {
        db.drop()
        DocumentTypeRegistry.resetDocumentTypes()
        DocumentTypeRegistry.clearListenersForTests()
        ModelExtractors.instance.reset()
        Locale.setDefault(currentLocale)
    }

    fun teardownClass() {
        ContentStoreIntegrationTest.cleanUpClass()
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

    fun getExpectedInOutput(type: String): MutableList<String> {
        return expectedInOutput[type] ?: mutableListOf()
    }
}
