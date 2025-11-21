package org.jbake.app

import org.assertj.core.api.Assertions
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.PropertyList
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.PrintWriter

class AsciidocParserTest {
    @Rule @JvmField
    var folder: TemporaryFolder = TemporaryFolder()

    private var config: DefaultJBakeConfiguration? = null
    private var parser: Parser? = null
    private var rootPath: File? = null

    private var asciidocWithSource: File? = null
    private var validAsciidocFile: File? = null
    private var invalidAsciiDocFile: File? = null
    private var validAsciiDocFileWithoutHeader: File? = null
    private var invalidAsciiDocFileWithoutHeader: File? = null
    private var validAsciiDocFileWithHeaderInContent: File? = null
    private var validAsciiDocFileWithoutJBakeMetaData: File? = null

    private val validHeader =
        "title=This is a Title = This is a valid Title\nstatus=draft\ntype=post\ndate=2013-09-02\n~~~~~~"
    private val invalidHeader = "title=This is a Title\n~~~~~~"

    @Before
    fun createSampleFile() {
        rootPath = TestUtils.testResourcesAsSourceFolder
        config = ConfigUtil().loadConfig(rootPath!!) as DefaultJBakeConfiguration
        parser = Parser(config)

        asciidocWithSource = folder.newFile("asciidoc-with-source.ad")
        var out = PrintWriter(asciidocWithSource)
        out.println(validHeader)
        out.println("= Hello, AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.println("")
        out.println("```")
        out.println("#!/bin/bash")
        out.println("")
        out.println("echo 'hello world!'")
        out.println("```")
        out.println("")
        out.println("{testattribute}")

        out.close()

        validAsciidocFile = folder.newFile("valid.ad")
        out = PrintWriter(validAsciidocFile)
        out.println(validHeader)
        out.println("= Hello, AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        invalidAsciiDocFile = folder.newFile("invalid.ad")
        out = PrintWriter(invalidAsciiDocFile)
        out.println(invalidHeader)
        out.println("= Hello, AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        validAsciiDocFileWithoutHeader = folder.newFile("validwoheader.ad")
        out = PrintWriter(validAsciiDocFileWithoutHeader)
        out.println("= Hello: AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("2013-09-02")
        out.println(":jbake-status: published")
        out.println(":jbake-type: page")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        invalidAsciiDocFileWithoutHeader = folder.newFile("invalidwoheader.ad")
        out = PrintWriter(invalidAsciiDocFileWithoutHeader)
        out.println("= Hello, AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("2013-09-02")
        out.println(":jbake-status: published")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        validAsciiDocFileWithHeaderInContent = folder.newFile("validheaderincontent.ad")
        out = PrintWriter(validAsciiDocFileWithHeaderInContent)
        out.println("= Hello, AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("2013-09-02")
        out.println(":jbake-status: published")
        out.println(":jbake-type: page")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.println("")
        out.println("----")
        out.println("title=Example Header")
        out.println("date=2013-02-01")
        out.println("type=post")
        out.println("tags=tag1, tag2")
        out.println("status=published")
        out.println("~~~~~~")
        out.println("----")
        out.close()

        validAsciiDocFileWithoutJBakeMetaData = folder.newFile("validwojbakemetadata.ad")
        out = PrintWriter(validAsciiDocFileWithoutJBakeMetaData)
        out.println("= Hello: AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("2013-09-02")
        out.println("")
        out.println("JBake now supports AsciiDoc documents without JBake meta data.")
        out.close()
    }


    @Test
    fun parseAsciidocFileWithPrettifyAttribute() {
        config!!.setProperty(PropertyList.ASCIIDOCTOR_ATTRIBUTES.key, "source-highlighter=prettify")
        val map = parser!!.processFile(asciidocWithSource!!)
        Assert.assertNotNull(map)
        Assert.assertEquals("draft", map!!.status)
        Assert.assertEquals("post", map.type)
        Assertions.assertThat(map.body)
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>")
            .contains("class=\"prettyprint highlight\"")

        Assertions.assertThat(map.body).doesNotContain("I Love Jbake")
        println(map.body)
    }

    @Test
    fun parseAsciidocFileWithCustomAttribute() {
        config!!.setProperty(ASCIIDOCTOR_ATTRIBUTES.key, "source-highlighter=prettify,testattribute=I Love Jbake")
        val map = parser!!.processFile(asciidocWithSource!!)
        Assert.assertNotNull(map)
        Assert.assertEquals("draft", map!!.status)
        Assert.assertEquals("post", map.type)
        Assertions.assertThat(map.body)
            .contains("I Love Jbake")
            .contains("class=\"prettyprint highlight\"")

        println(map.body)
    }

    @Test
    fun parseValidAsciiDocFile() {
        val map = parser!!.processFile(validAsciidocFile!!)
        Assert.assertNotNull(map)
        Assert.assertEquals("draft", map!!.status)
        Assert.assertEquals("post", map.type)
        Assertions.assertThat(map.body)
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>")
    }

    @Test
    fun parseInvalidAsciiDocFile() {
        val map = parser!!.processFile(invalidAsciiDocFile!!)
        Assert.assertNull(map)
    }

    @Test
    fun parseValidAsciiDocFileWithoutHeader() {
        val map = parser!!.processFile(validAsciiDocFileWithoutHeader!!)
        Assert.assertNotNull(map)
        Assert.assertEquals("Hello: AsciiDoc!", map!!.get("title"))
        Assert.assertEquals("published", map.status)
        Assert.assertEquals("page", map.type)
        Assertions.assertThat(map.body)
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>")
    }

    @Test
    fun parseInvalidAsciiDocFileWithoutHeader() {
        val map = parser!!.processFile(invalidAsciiDocFileWithoutHeader!!)
        Assert.assertNull(map)
    }

    @Test
    fun parseValidAsciiDocFileWithExampleHeaderInContent() {
        val map = parser!!.processFile(validAsciiDocFileWithHeaderInContent!!)
        Assert.assertNotNull(map)
        Assert.assertEquals("published", map!!.status)
        Assert.assertEquals("page", map.type)
        Assertions.assertThat(map.body)
            .contains("class=\"paragraph\"")
            .contains("<p>JBake now supports AsciiDoc.</p>")
            .contains("class=\"listingblock\"")
            .contains("class=\"content\"")
            .contains("<pre>")
            .contains("title=Example Header")
            .contains("date=2013-02-01")
            .contains("tags=tag1, tag2")
    }

    @Test
    fun parseValidAsciiDocFileWithoutJBakeMetaDataUsingDefaultTypeAndStatus() {
        config!!.setDefaultStatus("published")
        config!!.setDefaultType("page")
        val parser = Parser(config)
        val map = parser.processFile(validAsciiDocFileWithoutJBakeMetaData!!)
        Assert.assertNotNull(map)
        Assert.assertEquals("published", map!!.status)
        Assert.assertEquals("page", map.type)
        Assertions.assertThat(map.body)
            .contains("<p>JBake now supports AsciiDoc documents without JBake meta data.</p>")
    }
}
