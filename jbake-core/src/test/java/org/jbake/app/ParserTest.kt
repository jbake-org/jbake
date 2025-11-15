package org.jbake.app

import org.assertj.core.api.Assertions
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.PrintWriter
import java.util.*

class ParserTest {
    @Rule
    var folder: TemporaryFolder = TemporaryFolder()

    private var config: DefaultJBakeConfiguration? = null
    private var parser: Parser? = null
    private var rootPath: File? = null

    private var validHTMLFile: File? = null
    private var invalidHTMLFile: File? = null
    private var validMarkdownFileWithCustomHeader: File? = null
    private var validMarkdownFileWithDefaultStatus: File? = null
    private var validMarkdownFileWithDefaultTypeAndStatus: File? = null
    private var invalidMarkdownFileWithoutDefaultStatus: File? = null
    private var invalidMDFile: File? = null
    private var invalidExtensionFile: File? = null
    private var validHTMLWithJSONFile: File? = null
    private var validAsciiDocWithJSONFile: File? = null
    private var validAsciiDocWithADHeaderJSONFile: File? = null
    private var validaAsciidocWithUnsanitizedHeader: File? = null

    private val validHeader =
        "title=This is a Title = This is a valid Title\nstatus=draft\ntype=post\ndate=2013-09-02\n~~~~~~"
    private val invalidHeader = "title=This is a Title\n~~~~~~"
    private val sampleJsonData =
        "{\"numberValue\": 42, \"stringValue\": \"Answer to live, the universe and everything\", \"nullValue\": null, \"arrayValue\": [1, 2], \"objectValue\": {\"val1\": 1, \"val2\": 2}}"

    private val unsanitizedKeys =
        " title= Title \n status= draft \n   type= post   \ndate=2020-02-30\ncustom=custom without bom's\ntags= jbake, java    , tag with space   \n~~~~~~"

    private var customHeaderSeparator: String? = null


    @Before
    @Throws(Exception::class)
    fun createSampleFile() {
        rootPath = TestUtils.getTestResourcesAsSourceFolder()
        config = ConfigUtil().loadConfig(rootPath!!) as DefaultJBakeConfiguration
        parser = Parser(config)

        validHTMLFile = folder.newFile("valid.html")
        var out = PrintWriter(validHTMLFile)
        out.println(validHeader)
        out.println("<p>This is a test.</p>")
        out.close()

        invalidHTMLFile = folder.newFile("invalid.html")
        out = PrintWriter(invalidHTMLFile)
        out.println(invalidHeader)
        out.close()

        validMarkdownFileWithCustomHeader = folder.newFile("validMdCustomHeader.md")

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

        validMarkdownFileWithDefaultStatus = folder.newFile("validMdDefaultStatus.md")

        out = PrintWriter(validMarkdownFileWithDefaultStatus)
        out.println("title=Custom Header separator")
        out.println("type=post")
        out.println(config!!.getHeaderSeparator())
        out.println("# Hello Markdown!")
        out.println("")
        out.println("A paragraph")
        out.println("")
        out.println("* And")
        out.println("* A")
        out.println("* List")
        out.close()

        validMarkdownFileWithDefaultTypeAndStatus = folder.newFile("validMdDefaultTypeAndStatus.md")

        out = PrintWriter(validMarkdownFileWithDefaultTypeAndStatus)
        out.println("title=Custom Header separator")
        out.println("cached=false")
        out.println(config!!.getHeaderSeparator())
        out.println("# Hello Markdown!")
        out.println("")
        out.println("A paragraph")
        out.println("")
        out.println("* And")
        out.println("* A")
        out.println("* List")
        out.close()

        invalidMarkdownFileWithoutDefaultStatus = folder.newFile("invalidMdWithoutDefaultStatus.md")

        out = PrintWriter(invalidMarkdownFileWithoutDefaultStatus)
        out.println("title=Custom Header separator")
        out.println("type=page")
        out.println(config!!.getHeaderSeparator())
        out.println("# Hello Markdown!")
        out.println("")
        out.println("A paragraph")
        out.println("")
        out.println("* And")
        out.println("* A")
        out.println("* List")
        out.close()

        invalidMDFile = folder.newFile("invalidMd.md")

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

        invalidExtensionFile = folder.newFile("invalid.invalid")
        out = PrintWriter(invalidExtensionFile)
        out.println("invalid content")
        out.close()

        validHTMLWithJSONFile = folder.newFile("validHTMLWithJSONFile.html")
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

        validAsciiDocWithJSONFile = folder.newFile("validAsciiDocWithJSONFile.ad")
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

        validAsciiDocWithADHeaderJSONFile = folder.newFile("validAsciiDocWithADHeaderJSONFile.ad")
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

        validaAsciidocWithUnsanitizedHeader = folder.newFile("validAsciidocWithUnsanitizedHeader.adoc")
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

    @Test
    fun parseValidHTMLFile() {
        val documentModel = parser!!.processFile(validHTMLFile!!)
        Assert.assertNotNull(documentModel)
        Assert.assertEquals("draft", documentModel!!.status)
        Assert.assertEquals("post", documentModel.type)
        Assert.assertEquals("This is a Title = This is a valid Title", documentModel.title)
        Assert.assertNotNull(documentModel.date)
        val cal = Calendar.getInstance()
        cal.setTime(documentModel.date)
        Assert.assertEquals(8, cal.get(Calendar.MONTH).toLong())
        Assert.assertEquals(2, cal.get(Calendar.DAY_OF_MONTH).toLong())
        Assert.assertEquals(2013, cal.get(Calendar.YEAR).toLong())
    }

    @Test
    fun parseInvalidHTMLFile() {
        val documentModel = parser!!.processFile(invalidHTMLFile!!)
        Assert.assertNull(documentModel)
    }

    @Test
    fun parseInvalidExtension() {
        val documentModel = parser!!.processFile(invalidExtensionFile!!)
        Assert.assertNull(documentModel)
    }


    @Test
    fun parseMarkdownFileWithCustomHeaderSeparator() {
        config!!.setHeaderSeparator(customHeaderSeparator)

        val documentModel = parser!!.processFile(validMarkdownFileWithCustomHeader!!)
        Assert.assertNotNull(documentModel)
        Assert.assertEquals("draft", documentModel!!.status)
        Assert.assertEquals("post", documentModel.type)
        Assertions.assertThat(documentModel.body)
            .contains("<p>A paragraph</p>")
    }

    @Test
    fun parseMarkdownFileWithDefaultStatus() {
        config!!.setDefaultStatus("published")

        val documentModel = parser!!.processFile(validMarkdownFileWithDefaultStatus!!)
        Assert.assertNotNull(documentModel)
        Assert.assertEquals("published", documentModel!!.status)
        Assert.assertEquals("post", documentModel.type)
        Assert.assertEquals(true, documentModel.cached)
    }

    @Test
    fun parseMarkdownFileWithDefaultTypeAndStatus() {
        config!!.setDefaultStatus("published")
        config!!.setDefaultType("page")

        val documentModel = parser!!.processFile(validMarkdownFileWithDefaultTypeAndStatus!!)
        Assert.assertNotNull(documentModel)
        Assert.assertEquals("published", documentModel!!.status)
        Assert.assertEquals("page", documentModel.type)
    }

    @Test
    fun parseMarkdownFileWithDisabledCache() {
        config!!.setDefaultStatus("published")
        config!!.setDefaultType("page")

        val documentModel = parser!!.processFile(validMarkdownFileWithDefaultTypeAndStatus!!)
        Assert.assertEquals(false, documentModel!!.cached)
    }

    @Test
    fun parseInvalidMarkdownFileWithoutDefaultStatus() {
        config!!.setDefaultStatus("")
        config!!.setDefaultType("page")

        val documentModel = parser!!.processFile(invalidMarkdownFileWithoutDefaultStatus!!)
        Assert.assertNull(documentModel)
    }

    @Test
    fun parseInvalidMarkdownFile() {
        val documentModel = parser!!.processFile(invalidMDFile!!)
        Assert.assertNull(documentModel)
    }

    @Test
    fun sanitizeKeysAndValues() {
        val map = parser!!.processFile(validaAsciidocWithUnsanitizedHeader!!)

        Assertions.assertThat(map!!.status).isEqualTo("draft")
        Assertions.assertThat(map.title).isEqualTo("Title")
        Assertions.assertThat(map.type).isEqualTo("post")
        Assertions.assertThat<Any?>(map.get("custom")).isEqualTo("custom without bom's")
        Assertions.assertThat<String?>(map.tags)
            .isEqualTo(mutableListOf<String?>("jbake", "java", "tag with space").toTypedArray())
    }

    @Test
    fun sanitizeTags() {
        config!!.setProperty(TAG_SANITIZE.key, true)
        val map = parser!!.processFile(validaAsciidocWithUnsanitizedHeader!!)

        Assertions.assertThat<String?>(map!!.tags)
            .isEqualTo(mutableListOf<String?>("jbake", "java", "tag-with-space").toTypedArray())
    }


    @Test
    fun parseValidHTMLWithJSONFile() {
        val documentModel = parser!!.processFile(validHTMLWithJSONFile!!)
        assertJSONExtracted(documentModel!!.get("jsondata"))
    }

    @Test
    fun parseValidAsciiDocWithJSONFile() {
        val documentModel = parser!!.processFile(validAsciiDocWithJSONFile!!)
        assertJSONExtracted(documentModel!!.get("jsondata"))
    }

    @Test
    fun testValidAsciiDocWithADHeaderJSONFile() {
        val documentModel = parser!!.processFile(validAsciiDocWithADHeaderJSONFile!!)
        assertJSONExtracted(documentModel!!.get("jsondata"))
    }

    private fun assertJSONExtracted(jsonDataEntry: Any?) {
        Assertions.assertThat<Any?>(jsonDataEntry).isInstanceOf(JSONObject::class.java)
        val jsonData = jsonDataEntry as JSONObject
        Assertions.assertThat(jsonData.containsKey("numberValue")).isTrue()
        Assertions.assertThat<Any?>(jsonData.get("numberValue")).isInstanceOf(Number::class.java)
        Assertions.assertThat((jsonData.get("numberValue") as Number).toInt()).isEqualTo(42)
        Assertions.assertThat(jsonData.containsKey("stringValue")).isTrue()
        Assertions.assertThat<Any?>(jsonData.get("stringValue")).isInstanceOf(String::class.java)
        Assertions.assertThat(jsonData.get("stringValue") as String?)
            .isEqualTo("Answer to live, the universe and everything")
        Assertions.assertThat(jsonData.containsKey("nullValue")).isTrue()
        Assertions.assertThat<Any?>(jsonData.get("nullValue")).isNull()
        Assertions.assertThat(jsonData.containsKey("arrayValue")).isTrue()
        Assertions.assertThat<Any?>(jsonData.get("arrayValue")).isInstanceOf(JSONArray::class.java)
        Assertions.assertThat<Any?>(jsonData.get("arrayValue") as JSONArray?).contains(1L, 2L)
        Assertions.assertThat(jsonData.containsKey("objectValue")).isTrue()
        Assertions.assertThat<Any?>(jsonData.get("objectValue")).isInstanceOf(JSONObject::class.java)
        Assertions.assertThat<Any?, Any?>(jsonData.get("objectValue") as JSONObject?)
            .contains(AbstractMap.SimpleEntry<Any?, Any?>("val1", 1L), AbstractMap.SimpleEntry<Any?, Any?>("val2", 2L))
    }
}
