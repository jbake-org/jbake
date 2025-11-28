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
import java.io.File
import java.io.PrintWriter

class AsciidocParserTest : StringSpec({

    lateinit var folder: File

    lateinit var config: DefaultJBakeConfiguration
    lateinit var parser: Parser
    lateinit var rootPath: File

    lateinit var asciidocWithSource: File
    lateinit var validAsciidocFile: File
    lateinit var invalidAsciiDocFile: File
    lateinit var validAsciiDocFileWithoutHeader: File
    lateinit var invalidAsciiDocFileWithoutHeader: File
    lateinit var validAsciiDocFileWithHeaderInContent: File
    lateinit var validAsciiDocFileWithoutJBakeMetaData: File

    val validHeader =
        "title=This is a Title = This is a valid Title\nstatus=draft\ntype=post\ndate=2013-09-02\n~~~~~~"
    val invalidHeader = "title=This is a Title\n~~~~~~"

    beforeTest {
        folder = java.nio.file.Files.createTempDirectory("jbake-test").toFile()
        rootPath = TestUtils.testResourcesAsSourceFolder
        config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
        parser = Parser(config)

        asciidocWithSource = File(folder, "asciidoc-with-source.ad").apply { createNewFile() }
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

        validAsciidocFile = File(folder, "valid.ad").apply { createNewFile() }
        out = PrintWriter(validAsciidocFile)
        out.println(validHeader)
        out.println("= Hello, AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        invalidAsciiDocFile = File(folder, "invalid.ad").apply { createNewFile() }
        out = PrintWriter(invalidAsciiDocFile)
        out.println(invalidHeader)
        out.println("= Hello, AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        validAsciiDocFileWithoutHeader = File(folder, "validwoheader.ad").apply { createNewFile() }
        out = PrintWriter(validAsciiDocFileWithoutHeader)
        out.println("= Hello: AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("2013-09-02")
        out.println(":jbake-status: published")
        out.println(":jbake-type: page")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        invalidAsciiDocFileWithoutHeader = File(folder, "invalidwoheader.ad").apply { createNewFile() }
        out = PrintWriter(invalidAsciiDocFileWithoutHeader)
        out.println("= Hello, AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("2013-09-02")
        out.println(":jbake-status: published")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        validAsciiDocFileWithHeaderInContent = File(folder, "validheaderincontent.ad").apply { createNewFile() }
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

        validAsciiDocFileWithoutJBakeMetaData = File(folder, "validwojbakemetadata.ad").apply { createNewFile() }
        out = PrintWriter(validAsciiDocFileWithoutJBakeMetaData)
        out.println("= Hello: AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("2013-09-02")
        out.println("")
        out.println("JBake now supports AsciiDoc documents without JBake meta data.")
        out.close()
    }


    "parseAsciidocFileWithPrettifyAttribute" {
        config.setProperty(ASCIIDOCTOR_ATTRIBUTES.key, "source-highlighter=prettify")
        val map = parser.processFile(asciidocWithSource)
        map.shouldNotBeNull()
        map.status shouldBe "draft"
        map.type shouldBe "post"
        map.body shouldContain "class=\"paragraph\""
        map.body shouldContain "<p>JBake now supports AsciiDoc.</p>"
        map.body shouldContain "class=\"prettyprint highlight\""
        map.body shouldNotContain "I Love Jbake"
        println(map.body)
    }

    "parseAsciidocFileWithCustomAttribute" {
        config.setProperty(ASCIIDOCTOR_ATTRIBUTES.key, "source-highlighter=prettify,testattribute=I Love Jbake")
        val map = parser.processFile(asciidocWithSource)
        map.shouldNotBeNull()
        map.status shouldBe "draft"
        map.type shouldBe "post"
        map.body shouldContain "I Love Jbake"
        map.body shouldContain "class=\"prettyprint highlight\""
        println(map.body)
    }

    "parseValidAsciiDocFile" {
        val map = parser.processFile(validAsciidocFile)
        map.shouldNotBeNull()
        map.status shouldBe "draft"
        map.type shouldBe "post"
        map.body shouldContain "class=\"paragraph\""
        map.body shouldContain "<p>JBake now supports AsciiDoc.</p>"
    }

    "parseInvalidAsciiDocFile" {
        val map = parser.processFile(invalidAsciiDocFile)
        map.shouldBeNull()
    }

    "parseValidAsciiDocFileWithoutHeader" {
        val map = parser.processFile(validAsciiDocFileWithoutHeader)
        map.shouldNotBeNull()
        map["title"] shouldBe "Hello: AsciiDoc!"
        map.status shouldBe "published"
        map.type shouldBe "page"
        map.body shouldContain "class=\"paragraph\""
        map.body shouldContain "<p>JBake now supports AsciiDoc.</p>"
    }

    "parseInvalidAsciiDocFileWithoutHeader" {
        val map = parser.processFile(invalidAsciiDocFileWithoutHeader)
        map.shouldBeNull()
    }

    "parseValidAsciiDocFileWithExampleHeaderInContent" {
        val map = parser.processFile(validAsciiDocFileWithHeaderInContent)
        map.shouldNotBeNull()
        map.status shouldBe "published"
        map.type shouldBe "page"
        map.body shouldContain "class=\"paragraph\""
        map.body shouldContain "<p>JBake now supports AsciiDoc.</p>"
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
        map.shouldNotBeNull()
        map.status shouldBe "published"
        map.type shouldBe "page"
        map.body shouldContain "<p>JBake now supports AsciiDoc documents without JBake meta data.</p>"
    }


    afterTest {
        folder.deleteRecursively()
    }
})
