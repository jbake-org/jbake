package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.PropertyList.ASCIIDOCTOR_ATTRIBUTES
import org.jbake.model.DocumentModel
import org.jbake.parser.Parser
import java.io.File

class AsciidocParserTest : StringSpec({

    val validHeader = """
        |title=This is a Title = This is a valid Title
        |status=draft
        |type=post
        |date=2013-09-02
        |~~~~~~
        """.trimMargin()

    val invalidHeader = """
        |title=This is a Title
        |~~~~~~
        """.trimMargin()

    val asciidocWithSourceContent = validHeader + "\n" + """
        |= Hello, AsciiDoc!
        |Test User <user@test.org>
        |
        |JBake now supports AsciiDoc.
        |
        |```
        |#!/bin/bash
        |
        |echo 'hello world!'
        |```
        |
        |{testattribute}
        """.trimMargin()

    val validAsciidocContent = validHeader + "\n" + """
        |= Hello, AsciiDoc!
        |Test User <user@test.org>
        |
        |JBake now supports AsciiDoc.
        """.trimMargin()

    val invalidAsciiDocContent = invalidHeader + "\n" + """
        |= Hello, AsciiDoc!
        |Test User <user@test.org>
        |
        |JBake now supports AsciiDoc.
        """.trimMargin()

    val validAsciiDocWithoutHeaderContent = """
        |= Hello: AsciiDoc!
        |Test User <user@test.org>
        |2013-09-02
        |:jbake-status: published
        |:jbake-type: page
        |
        |JBake now supports AsciiDoc.
        """.trimMargin()

    val invalidAsciiDocWithoutHeaderContent = """
        |= Hello, AsciiDoc!
        |Test User <user@test.org>
        |2013-09-02
        |:jbake-status: published
        |
        |JBake now supports AsciiDoc.
        """.trimMargin()

    val validAsciiDocWithHeaderInContentContent = """
        |= Hello, AsciiDoc!
        |Test User <user@test.org>
        |2013-09-02
        |:jbake-status: published
        |:jbake-type: page
        |
        |JBake now supports AsciiDoc.
        |
        |----
        |title=Example Header
        |date=2013-02-01
        |type=post
        |tags=tag1, tag2
        |status=published
        |~~~~~~
        |----
        """.trimMargin()

    val validAsciiDocWithoutJBakeMetaDataContent = """
        |= Hello: AsciiDoc!
        |Test User <user@test.org>
        |2013-09-02
        |
        |JBake now supports AsciiDoc documents without JBake meta data.
        """.trimMargin()
    
    fun assertBasicAsciiDocContent(body: String) {
        body shouldContain "class=\"paragraph\""
        body shouldContain "<p>JBake now supports AsciiDoc.</p>"
    }

    fun assertMetadata(map: DocumentModel?, status: String, type: String) {
        map.shouldNotBeNull()
        map.status shouldBe status
        map.type shouldBe type
    }

    lateinit var tempDir: File
    lateinit var rootPath: File
    lateinit var config: DefaultJBakeConfiguration
    lateinit var parser: Parser
    
    lateinit var asciidocWithSource: File
    lateinit var validAsciidocFile: File
    lateinit var invalidAsciiDocFile: File
    lateinit var validAsciiDocFileWithoutHeader: File
    lateinit var invalidAsciiDocFileWithoutHeader: File
    lateinit var validAsciiDocFileWithHeaderInContent: File
    lateinit var validAsciiDocFileWithoutJBakeMetaData: File

    fun createTestFile(name: String, content: String) =
        tempDir.resolve(name).apply { createNewFile(); writeText(content) }

    beforeTest {
        tempDir = java.nio.file.Files.createTempDirectory("jbake-test").toFile()
        rootPath = TestUtils.testResourcesAsSourceDir
        config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
        parser = Parser(config)
        
        asciidocWithSource = createTestFile("asciidoc-with-source.ad", asciidocWithSourceContent)
        validAsciidocFile = createTestFile("valid.ad", validAsciidocContent)
        invalidAsciiDocFile = createTestFile("invalid.ad", invalidAsciiDocContent)
        validAsciiDocFileWithoutHeader = createTestFile("validwoheader.ad", validAsciiDocWithoutHeaderContent)
        invalidAsciiDocFileWithoutHeader = createTestFile("invalidwoheader.ad", invalidAsciiDocWithoutHeaderContent)
        validAsciiDocFileWithHeaderInContent = createTestFile("validheaderincontent.ad", validAsciiDocWithHeaderInContentContent)
        validAsciiDocFileWithoutJBakeMetaData = createTestFile("validwojbakemetadata.ad", validAsciiDocWithoutJBakeMetaDataContent)
    }


    "parseAsciidocFileWithPrettifyAttribute" {
        config.setProperty(ASCIIDOCTOR_ATTRIBUTES.key, "source-highlighter=prettify")
        val map = parser.processFile(asciidocWithSource)
        assertMetadata(map, "draft", "post")
        assertBasicAsciiDocContent(map!!.body)
        map.body shouldContain "class=\"prettyprint highlight\""
        map.body shouldNotContain "I Love Jbake"
    }

    "parseAsciidocFileWithCustomAttribute" {
        config.setProperty(ASCIIDOCTOR_ATTRIBUTES.key, "source-highlighter=prettify,testattribute=I Love Jbake")
        val map = parser.processFile(asciidocWithSource)
        assertMetadata(map, "draft", "post")
        map!!.body shouldContain "I Love Jbake"
        map.body shouldContain "class=\"prettyprint highlight\""
    }

    "parseValidAsciiDocFile" {
        val map = parser.processFile(validAsciidocFile)
        assertMetadata(map, "draft", "post")
        assertBasicAsciiDocContent(map!!.body)
    }

    "parseInvalidAsciiDocFile" {
        val map = parser.processFile(invalidAsciiDocFile)
        map.shouldBeNull()
    }

    "parseValidAsciiDocFileWithoutHeader" {
        val map = parser.processFile(validAsciiDocFileWithoutHeader)
        assertMetadata(map, "published", "page")
        map!!["title"] shouldBe "Hello: AsciiDoc!"
        assertBasicAsciiDocContent(map.body)
    }

    "parseInvalidAsciiDocFileWithoutHeader" {
        val map = parser.processFile(invalidAsciiDocFileWithoutHeader)
        map.shouldBeNull()
    }

    "parseValidAsciiDocFileWithExampleHeaderInContent" {
        val map = parser.processFile(validAsciiDocFileWithHeaderInContent)
        assertMetadata(map, "published", "page")
        assertBasicAsciiDocContent(map!!.body)
        map.body shouldContain "class=\"listingblock\""
        map.body shouldContain "class=\"content\""
        map.body shouldContain "<pre>"
        map.body shouldContain "title=Example Header"
        map.body shouldContain "date=2013-02-01"
        map.body shouldContain "tags=tag1, tag2"
    }

    "parseValidAsciiDocFileWithoutJBakeMetaDataUsingDefaultTypeAndStatus" {
        config.setDefaultStatus("published")
        config.setDefaultType("page")
        val parser = Parser(config)
        val map = parser.processFile(validAsciiDocFileWithoutJBakeMetaData)
        assertMetadata(map, "published", "page")
        map!!.body shouldContain "<p>JBake now supports AsciiDoc documents without JBake meta data.</p>"
    }


    afterTest {
        tempDir.deleteRecursively()
    }
})
