package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.PropertyList
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.File
import java.io.PrintWriter
import java.util.*

class ParserTest : StringSpec({
    lateinit var tempDir: File

    lateinit var config: DefaultJBakeConfiguration
    lateinit var parser: Parser
    lateinit var rootPath: File

    lateinit var validHTMLFile: File
    lateinit var invalidHTMLFile: File
    lateinit var validMarkdownFileWithCustomHeader: File
    lateinit var validMarkdownFileWithDefaultStatus: File
    lateinit var validMarkdownFileWithDefaultTypeAndStatus: File
    lateinit var invalidMarkdownFileWithoutDefaultStatus: File
    lateinit var invalidMDFile: File
    lateinit var invalidExtensionFile: File
    lateinit var validHTMLWithJSONFile: File
    lateinit var validAsciiDocWithJSONFile: File
    lateinit var validAsciiDocWithADHeaderJSONFile: File
    lateinit var validaAsciidocWithUnsanitizedHeader: File

    val validHeader =
        "title=This is a Title = This is a valid Title\nstatus=draft\ntype=post\ndate=2013-09-02\n~~~~~~"
    val invalidHeader = "title=This is a Title\n~~~~~~"
    val sampleJsonData =
        "{\"numberValue\": 42, \"stringValue\": \"Answer to live, the universe and everything\", \"nullValue\": null, \"arrayValue\": [1, 2], \"objectValue\": {\"val1\": 1, \"val2\": 2}}"

    val unsanitizedKeys =
        " title= Title \n status= draft \n   type= post   \ndate=2020-02-30\ncustom=custom without bom's\ntags= jbake, java    , tag with space   \n~~~~~~"

    var customHeaderSeparator: String? = null


    beforeTest {
        tempDir = java.nio.file.Files.createTempDirectory("jbake-test").toFile()
        rootPath = TestUtils.testResourcesAsSourceDir
        config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
        parser = Parser(config)

        validHTMLFile = tempDir.resolve("valid.html").apply { createNewFile() }
        var out = PrintWriter(validHTMLFile)
        out.println(validHeader)
        out.println("<p>This is a test.</p>")
        out.close()

        invalidHTMLFile = tempDir.resolve("invalid.html").apply { createNewFile() }
        out = PrintWriter(invalidHTMLFile)
        out.println(invalidHeader)
        out.close()

        validMarkdownFileWithCustomHeader = tempDir.resolve("validMdCustomHeader.md").apply { createNewFile() }

        customHeaderSeparator = "---------------------------------------"
        out = PrintWriter(validMarkdownFileWithCustomHeader)
        out.println("title=Custom Header separator")
        out.println("type=post")
        out.println("status=draft")
        out.println(customHeaderSeparator)
        out.println("# Hello Markdown!")
        out.println("")
        out.println("A paragraph")
        out.println("")
        out.println("* And")
        out.println("* A")
        out.println("* List")
        out.close()

        validMarkdownFileWithDefaultStatus = tempDir.resolve("validMdDefaultStatus.md").apply { createNewFile() }

        out = PrintWriter(validMarkdownFileWithDefaultStatus)
        out.println("title=Custom Header separator")
        out.println("type=post")
        out.println(config.headerSeparator)
        out.println("# Hello Markdown!")
        out.println("")
        out.println("A paragraph")
        out.println("")
        out.println("* And")
        out.println("* A")
        out.println("* List")
        out.close()

        validMarkdownFileWithDefaultTypeAndStatus = tempDir.resolve("validMdDefaultTypeAndStatus.md").apply { createNewFile() }

        out = PrintWriter(validMarkdownFileWithDefaultTypeAndStatus)
        out.println("title=Custom Header separator")
        out.println("cached=false")
        out.println(config.headerSeparator)
        out.println("# Hello Markdown!")
        out.println("")
        out.println("A paragraph")
        out.println("")
        out.println("* And")
        out.println("* A")
        out.println("* List")
        out.close()

        invalidMarkdownFileWithoutDefaultStatus = tempDir.resolve("invalidMdWithoutDefaultStatus.md").apply { createNewFile() }

        out = PrintWriter(invalidMarkdownFileWithoutDefaultStatus)
        out.println("title=Custom Header separator")
        out.println("type=page")
        out.println(config.headerSeparator)
        out.println("# Hello Markdown!")
        out.println("")
        out.println("A paragraph")
        out.println("")
        out.println("* And")
        out.println("* A")
        out.println("* List")
        out.close()

        invalidMDFile = tempDir.resolve("invalidMd.md").apply { createNewFile() }

        out = PrintWriter(invalidMDFile)
        out.println(invalidHeader)
        out.println("# Hello Markdown!")
        out.println("")
        out.println("A paragraph")
        out.println("")
        out.println("* And")
        out.println("* A")
        out.println("* List")
        out.close()

        invalidExtensionFile = tempDir.resolve("invalid.invalid").apply { createNewFile() }
        out = PrintWriter(invalidExtensionFile)
        out.println("invalid content")
        out.close()

        validHTMLWithJSONFile = tempDir.resolve("validHTMLWithJSONFile.html").apply { createNewFile() }
        out = PrintWriter(validHTMLWithJSONFile)
        out.println("title=This is a Title = This is a valid Title")
        out.println("status=draft")
        out.println("type=post")
        out.println("date=2013-09-02")
        out.print("jsondata=")
        out.println(sampleJsonData)
        out.println("~~~~~~")
        out.println("Sample Body")
        out.close()

        validAsciiDocWithJSONFile = tempDir.resolve("validAsciiDocWithJSONFile.ad").apply { createNewFile() }
        out = PrintWriter(validAsciiDocWithJSONFile)
        out.println("title=This is a Title = This is a valid Title")
        out.println("status=draft")
        out.println("type=post")
        out.println("date=2013-09-02")
        out.print("jsondata=")
        out.println(sampleJsonData)
        out.println("~~~~~~")
        out.println("= Hello, AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        validAsciiDocWithADHeaderJSONFile = tempDir.resolve("validAsciiDocWithADHeaderJSONFile.ad").apply { createNewFile() }
        out = PrintWriter(validAsciiDocWithADHeaderJSONFile)
        out.println("= Hello: AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("2013-09-02")
        out.println(":jbake-status: published")
        out.println(":jbake-type: page")
        out.print(":jbake-jsondata: ")
        out.println(sampleJsonData)
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()

        validaAsciidocWithUnsanitizedHeader = tempDir.resolve("validAsciidocWithUnsanitizedHeader.adoc").apply { createNewFile() }
        out = PrintWriter(validaAsciidocWithUnsanitizedHeader, "UTF-8")
        // Simulating a \uFEFF Byte order Marker in utf-8
        out.print("\uFEFF")
        out.println(unsanitizedKeys)
        out.println("= Hello: AsciiDoc!")
        out.println("Test User <user@test.org>")
        out.println("2013-09-02")
        out.println(":jbake-status: published")
        out.println(":jbake-type: page")
        out.print(":jbake-jsondata: ")
        out.println(sampleJsonData)
        out.println("")
        out.println("JBake now supports AsciiDoc.")
        out.close()
    }

    "parseValidHTMLFile" {
        val documentModel = parser.processFile(validHTMLFile)!!
        documentModel.shouldNotBeNull()
        documentModel.status shouldBe "draft"
        documentModel.type shouldBe "post"
        documentModel.title shouldBe "This is a Title = This is a valid Title"
        documentModel.date.shouldNotBeNull()
        val cal = Calendar.getInstance()
        cal.setTime(documentModel.date)
        cal.get(Calendar.MONTH) shouldBe 8
        cal.get(Calendar.DAY_OF_MONTH) shouldBe 2
        cal.get(Calendar.YEAR) shouldBe 2013
    }

    "parseInvalidHTMLFile" {
        val documentModel = parser.processFile(invalidHTMLFile)
        documentModel.shouldBeNull()
    }

    "parseInvalidExtension" {
        val documentModel = parser.processFile(invalidExtensionFile)
        documentModel.shouldBeNull()
    }


    "parseMarkdownFileWithCustomHeaderSeparator" {
        config.headerSeparator = customHeaderSeparator

        val documentModel = parser.processFile(validMarkdownFileWithCustomHeader)!!
        documentModel.shouldNotBeNull()
        documentModel.status shouldBe "draft"
        documentModel.type shouldBe "post"
        documentModel.body shouldContain "<p>A paragraph</p>"
    }

    "parseMarkdownFileWithDefaultStatus" {
        config.setDefaultStatus("published")

        val documentModel = parser.processFile(validMarkdownFileWithDefaultStatus)!!
        documentModel.shouldNotBeNull()
        documentModel.status shouldBe "published"
        documentModel.type shouldBe "post"
        documentModel.cached shouldBe true
    }

    "parseMarkdownFileWithDefaultTypeAndStatus" {
        config.setDefaultStatus("published")
        config.setDefaultType("page")

        val documentModel = parser.processFile(validMarkdownFileWithDefaultTypeAndStatus)!!
        documentModel.shouldNotBeNull()
        documentModel.status shouldBe "published"
        documentModel.type shouldBe "page"
    }

    "parseMarkdownFileWithDisabledCache" {
        config.setDefaultStatus("published")
        config.setDefaultType("page")

        val documentModel = parser.processFile(validMarkdownFileWithDefaultTypeAndStatus)!!
        documentModel.cached shouldBe false
    }

    "parseInvalidMarkdownFileWithoutDefaultStatus" {
        config.setDefaultStatus("")
        config.setDefaultType("page")

        val documentModel = parser.processFile(invalidMarkdownFileWithoutDefaultStatus)
        documentModel.shouldBeNull()
    }

    "parseInvalidMarkdownFile" {
        val documentModel = parser.processFile(invalidMDFile)
        documentModel.shouldBeNull()
    }

    "sanitizeKeysAndValues" {
        val map = parser.processFile(validaAsciidocWithUnsanitizedHeader)!!

        map.status shouldBe "draft"
        map.title shouldBe "Title"
        map.type shouldBe "post"
        map["custom"] shouldBe "custom without bom's"
        map.tags shouldBe listOf("jbake", "java", "tag with space")
    }

    "sanitizeTags" {
        config.setProperty(PropertyList.TAG_SANITIZE.key, true)
        val map = parser.processFile(validaAsciidocWithUnsanitizedHeader)!!

        map.tags shouldBe listOf("jbake", "java", "tag-with-space")
    }


    "parseValidHTMLWithJSONFile" {
        val documentModel = parser.processFile(validHTMLWithJSONFile)!!
        ParserTest.assertJSONExtracted(documentModel["jsondata"])
    }

    "parseValidAsciiDocWithJSONFile" {
        val documentModel = parser.processFile(validAsciiDocWithJSONFile)!!
        ParserTest.assertJSONExtracted(documentModel["jsondata"])
    }

    "testValidAsciiDocWithADHeaderJSONFile" {
        val documentModel = parser.processFile(validAsciiDocWithADHeaderJSONFile)!!
        ParserTest.assertJSONExtracted(documentModel["jsondata"])
    }

    afterTest {
        tempDir.deleteRecursively()
    }
}) {
    companion object {
        fun assertJSONExtracted(jsonDataEntry: Any?) {
            jsonDataEntry.shouldNotBeNull()
            jsonDataEntry.shouldBeInstanceOf<JSONObject>()
            val jsonData = jsonDataEntry as JSONObject

            jsonData.containsKey("numberValue") shouldBe true
            jsonData["numberValue"].shouldBeInstanceOf<Number>()
            (jsonData["numberValue"] as Number).toInt() shouldBe 42
            jsonData.containsKey("stringValue") shouldBe true
            jsonData["stringValue"].shouldBeInstanceOf<String>()
            jsonData["stringValue"] shouldBe "Answer to live, the universe and everything"
            jsonData.containsKey("nullValue") shouldBe true
            jsonData["nullValue"] shouldBe null
            jsonData.containsKey("arrayValue") shouldBe true
            jsonData["arrayValue"].shouldBeInstanceOf<JSONArray>()
            val arrayValue = jsonData["arrayValue"] as JSONArray
            arrayValue.contains(1L) shouldBe true
            arrayValue.contains(2L) shouldBe true
            jsonData.containsKey("objectValue") shouldBe true
            jsonData["objectValue"].shouldBeInstanceOf<JSONObject>()
            val objectValue = jsonData["objectValue"] as JSONObject
            objectValue["val1"] shouldBe 1L
            objectValue["val2"] shouldBe 2L
        }
    }
}
