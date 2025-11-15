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
package org.jbake.app.template

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions
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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * @author jdlee
 */
abstract class AbstractTemplateEngineRenderingTest(
    protected val templateDir: String,
    protected val templateExtension: String?
) : ContentStoreIntegrationTest() {
    protected val outputStrings: MutableMap<String?, MutableList<String?>?> = HashMap<String?, MutableList<String?>?>()

    protected var destinationFolder: File? = null
    protected var templateFolder: File? = null
    protected var renderer: Renderer? = null
    protected var currentLocale: Locale? = null
    private var parser: Parser? = null

    @Before
    @Throws(Exception::class)
    fun setup() {
        currentLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)

        val listener = ModelExtractorsDocumentTypeListener()
        addListener(listener)

        templateFolder = File(ContentStoreIntegrationTest.Companion.sourceFolder, templateDir)
        if (!templateFolder!!.exists()) {
            throw Exception("Cannot find template folder!")
        }

        destinationFolder = ContentStoreIntegrationTest.Companion.folder.getRoot()
        ContentStoreIntegrationTest.Companion.config.setDestinationFolder(destinationFolder)
        ContentStoreIntegrationTest.Companion.config.setTemplateFolder(templateFolder)


        for (docType in documentTypes) {
            val templateFile: File? = ContentStoreIntegrationTest.Companion.config.getTemplateFileByDocType(docType)

            if (templateFile != null) {
                val fileName = templateFile.getName()
                val fileBaseName = fileName.substring(0, fileName.lastIndexOf("."))
                ContentStoreIntegrationTest.Companion.config.setTemplateFileNameForDocType(
                    docType,
                    fileBaseName + "." + templateExtension
                )
            }
        }

        ContentStoreIntegrationTest.Companion.config.setTemplateFileNameForDocType(
            "paper",
            "paper." + templateExtension
        )
        addDocumentType("paper")
        ContentStoreIntegrationTest.Companion.db.updateSchema()

        Assert.assertEquals(".html", ContentStoreIntegrationTest.Companion.config.getOutputExtension())

        val crawler = Crawler(ContentStoreIntegrationTest.Companion.db, ContentStoreIntegrationTest.Companion.config)
        crawler.crawl()
        parser = Parser(ContentStoreIntegrationTest.Companion.config)
        renderer = Renderer(ContentStoreIntegrationTest.Companion.db, ContentStoreIntegrationTest.Companion.config)

        setupExpectedOutputStrings()
    }

    private fun setupExpectedOutputStrings() {
        outputStrings.put(
            "post", mutableListOf<String?>(
                "<h2>Second Post</h2>",
                "<p class=\"post-date\">28",
                "2013</p>",
                "Lorem ipsum dolor sit amet",
                "<h5>Published Posts</h5>",
                "blog/2012/first-post.html"
            )
        )

        outputStrings.put(
            "page", mutableListOf<String?>(
                "<h4>About</h4>",
                "All about stuff!",
                "<h5>Published Pages</h5>",
                "/projects.html"
            )
        )

        outputStrings.put(
            "index", mutableListOf<String?>(
                "<a href=\"blog/2016/another-post.html\"",
                ">Another Post</a>",
                "<a href=\"blog/2013/second-post.html\"",
                ">Second Post</a>"
            )
        )

        outputStrings.put(
            "feed", mutableListOf<String?>(
                "<description>My corner of the Internet</description>",
                "<title>Second Post</title>",
                "<title>First Post</title>"
            )
        )

        outputStrings.put(
            "archive", mutableListOf<String?>(
                "<a href=\"blog/2013/second-post.html\"",
                ">Second Post</a>",
                "<a href=\"blog/2012/first-post.html\"",
                ">First Post</a>"
            )
        )

        outputStrings.put(
            "tags", mutableListOf<String?>(
                "<a href=\"blog/2013/second-post.html\"",
                ">Second Post</a>",
                "<a href=\"blog/2012/first-post.html\"",
                ">First Post</a>"
            )
        )

        outputStrings.put(
            "tags-index", mutableListOf<String?>(
                "<h1>Tags</h1>",
                "<h2><a href=\"../tags/blog.html\">blog</a>",
                "3</h2>"
            )
        )

        outputStrings.put(
            "sitemap", mutableListOf<String?>(
                "blog/2013/second-post.html",
                "blog/2012/first-post.html",
                "papers/published-paper.html"
            )
        )
    }

    @After
    fun cleanup() {
        resetDocumentTypes()
        ModelExtractors.getInstance().reset()
        Locale.setDefault(currentLocale)
    }

    @Test
    @Throws(Exception::class)
    fun renderPost() {
        // setup
        val filename = "second-post.html"

        val sampleFile: File = File(
            (ContentStoreIntegrationTest.Companion.sourceFolder.getPath() + File.separator + "content"
                    + File.separator + "blog" + File.separator + "2013" + File.separator + filename)
        )
        val content = parser!!.processFile(sampleFile)
        content!!.uri = "/" + filename
        renderer!!.render(content)
        val outputFile = File(destinationFolder, filename)
        Assert.assertTrue(outputFile.exists())

        // verify
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("post")!!) {
            Assertions.assertThat(output).contains(string)
        }
    }

    @Test
    @Throws(Exception::class)
    fun renderPage() {
        // setup
        val filename = "about.html"

        val sampleFile: File =
            File(ContentStoreIntegrationTest.Companion.sourceFolder.getPath() + File.separator + "content" + File.separator + filename)
        val content = parser!!.processFile(sampleFile)
        content!!.uri = "/" + filename
        renderer!!.render(content)
        val outputFile = File(destinationFolder, filename)
        Assert.assertTrue(outputFile.exists())

        // verify
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("page")!!) {
            Assertions.assertThat(output).contains(string)
        }
    }

    @Test
    @Throws(Exception::class)
    fun renderIndex() {
        //exec
        renderer!!.renderIndex("index.html")

        //validate
        val outputFile = File(destinationFolder, "index.html")
        Assert.assertTrue(outputFile.exists())

        // verify
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("index")!!) {
            Assertions.assertThat(output).contains(string)
        }
    }

    @Test
    @Throws(Exception::class)
    fun renderFeed() {
        renderer!!.renderFeed("feed.xml")
        val outputFile = File(destinationFolder, "feed.xml")
        Assert.assertTrue(outputFile.exists())

        // verify
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("feed")!!) {
            Assertions.assertThat(output).contains(string)
        }
    }

    @Test
    @Throws(Exception::class)
    fun renderArchive() {
        renderer!!.renderArchive("archive.html")
        val outputFile = File(destinationFolder, "archive.html")
        Assert.assertTrue(outputFile.exists())

        // verify
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("archive")!!) {
            Assertions.assertThat(output).contains(string)
        }
    }

    @Test
    @Throws(Exception::class)
    fun renderTags() {
        renderer!!.renderTags("tags")

        // verify
        val outputFile = File(destinationFolder.toString() + File.separator + "tags" + File.separator + "blog.html")
        Assert.assertTrue(outputFile.exists())
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("tags")!!) {
            Assertions.assertThat(output).contains(string)
        }
    }

    @Test
    @Throws(Exception::class)
    fun renderTagsIndex() {
        ContentStoreIntegrationTest.Companion.config.setRenderTagsIndex(true)

        renderer!!.renderTags("tags")
        val outputFile = File(destinationFolder.toString() + File.separator + "tags" + File.separator + "index.html")
        Assert.assertTrue(outputFile.exists())
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("tags-index")!!) {
            Assertions.assertThat(output).contains(string)
        }
    }

    @Test
    @Throws(Exception::class)
    fun renderSitemap() {
        addDocumentType("paper")
        ContentStoreIntegrationTest.Companion.db.updateSchema()

        renderer!!.renderSitemap("sitemap.xml")
        val outputFile = File(destinationFolder, "sitemap.xml")
        Assert.assertTrue(outputFile.exists())

        // verify
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
        for (string in getOutputStrings("sitemap")!!) {
            Assertions.assertThat(output).contains(string)
        }
        Assertions.assertThat(output).doesNotContain("draft-paper.html")
    }

    protected fun getOutputStrings(type: String?): MutableList<String?>? {
        return outputStrings.get(type)
    }

    @Test
    @Throws(Exception::class)
    fun checkDbTemplateModelIsPopulated() {
        ContentStoreIntegrationTest.Companion.config.setPaginateIndex(true)
        ContentStoreIntegrationTest.Companion.config.setPostsPerPage(1)

        outputStrings.put("dbSpan", mutableListOf<String?>("<span>3</span>"))

        ContentStoreIntegrationTest.Companion.db.deleteAllByDocType("post")

        renderer!!.renderIndexPaging("index.html")

        val outputFile = File(destinationFolder, "index.html")
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())

        for (string in getOutputStrings("dbSpan")!!) {
            Assertions.assertThat(output).contains(string)
        }
    }
}
