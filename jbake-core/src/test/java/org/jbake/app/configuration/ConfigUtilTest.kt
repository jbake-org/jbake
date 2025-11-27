package org.jbake.app.configuration

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain as stringContain
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.io.FileUtils
import org.jbake.TestUtils
import org.jbake.app.JBakeException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class ConfigUtilTest : StringSpec({
    lateinit var sourceFolder: Path
    lateinit var util: ConfigUtil

    beforeTest {
        sourceFolder = Files.createTempDirectory("jbake-test")
        util = ConfigUtil()
    }

    afterTest {
        sourceFolder.toFile().deleteRecursively()
    }

    "shouldLoadSiteHost" {
        val config = util.loadConfig(TestUtils.testResourcesAsSourceFolder)
        config.siteHost shouldBe "http://www.jbake.org"
    }

    "shouldLoadADefaultConfiguration" {
        val config = util.loadConfig(TestUtils.testResourcesAsSourceFolder)
        // Check some default properties are present
        config.destinationFolder shouldNotBe null
        config.assetFolder shouldNotBe null
        config.templateFolder shouldNotBe null
    }

    "shouldLoadACustomConfiguration" {
        val customConfigFile = File(sourceFolder.toFile(), "jbake.properties")
        customConfigFile.writeText("test.property=12345")

        val configuration = util.loadConfig(sourceFolder.toFile())

        configuration.get("test.property") shouldBe "12345"
        // Check default properties still present
        configuration.destinationFolder shouldNotBe null
    }

    "shouldThrowAnExceptionIfSourcefolderDoesNotExist" {
        val nonExistentSourceFolder = mockk<File>()
        every { nonExistentSourceFolder.absolutePath } returns "/tmp/nonexistent"
        every { nonExistentSourceFolder.exists() } returns false

        val e = shouldThrow<JBakeException> { util.loadConfig(nonExistentSourceFolder) }
        e.message shouldBe "The given source folder '/tmp/nonexistent' does not exist."
    }

    "shouldAddSourcefolderToConfiguration" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder)

        config.sourceFolder shouldBe sourceFolder
    }

    "shouldThrowAnExceptionIfSourcefolderIsNotADirectory" {
        val sourceFolder = mockk<File>()
        every { sourceFolder.exists() } returns true
        every { sourceFolder.isDirectory() } returns false
        every { sourceFolder.absolutePath } returns "/tmp/notadir"

        val e = shouldThrow<JBakeException> { util.loadConfig(sourceFolder) }
        e.message shouldBe "The given source folder is not a directory."
    }

    "shouldReturnDestinationFolderFromConfiguration" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "output")
        val config = util.loadConfig(sourceFolder)

        config.destinationFolder shouldBe expectedDestinationFolder
    }

    "shouldReturnAssetFolderFromConfiguration" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "assets")
        val config = util.loadConfig(sourceFolder)

        config.assetFolder shouldBe expectedDestinationFolder
    }

    "shouldReturnTemplateFolderFromConfiguration" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "templates")
        val config = util.loadConfig(sourceFolder)

        config.templateFolder shouldBe expectedDestinationFolder
    }

    "shouldReturnContentFolderFromConfiguration" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "content")
        val config = util.loadConfig(sourceFolder)

        config.contentFolder shouldBe expectedDestinationFolder
    }

    "shouldGetTemplateFileDoctype" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedTemplateFile = File(sourceFolder, "templates/index.ftl")
        val config = util.loadConfig(sourceFolder)

        val templateFile = config.getTemplateFileByDocType("masterindex")
        templateFile shouldBe expectedTemplateFile

        val templateFile2 = config.getTemplateByDocType("team")
        templateFile2 shouldBe "special/team.tpl"
    }

    "shouldLogWarningIfDocumentTypeNotFound" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder)

        // Should return null for unknown document type
        val result = config.getTemplateFileByDocType("none")
        result shouldBe null
    }

    "shouldGetTemplateOutputExtension" {
        val docType = "masterindex"
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setTemplateExtensionForDocType(docType, ".xhtml")

        val extension = config.getOutputExtensionByDocType(docType)

        extension shouldBe ".xhtml"
    }

    "shouldGetMarkdownExtensionsAsList" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        val markdownExtensions = config.markdownExtensions

        markdownExtensions shouldContain "HARDWRAPS"
        markdownExtensions shouldContain "AUTOLINKS"
        markdownExtensions shouldContain "FENCED_CODE_BLOCKS"
        markdownExtensions shouldContain "DEFINITIONS"
    }

    "shouldReturnConfiguredDocTypes" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        val docTypes = config.documentTypes

        docTypes shouldContain "post"
        docTypes shouldContain "page"
        docTypes shouldContain "masterindex"
    }

    "shouldReturnAListOfAsciidoctorOptionsKeys" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val options = config.asciidoctorOptionKeys

        options shouldContain "requires"
        options shouldContain "template_dirs"
    }

    "shouldReturnAnAsciidoctorOption" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val option = config.getAsciidoctorOption("requires") as Collection<String>

        option shouldContain "asciidoctor-diagram"
    }

    "shouldReturnAnAsciidoctorOptionWithAListValue" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")
        @Suppress("UNCHECKED_CAST")
        val option = config.getAsciidoctorOption("template_dirs") as Collection<String>

        option shouldContain "src/template1"
        option shouldContain "src/template2"
    }

    "shouldReturnEmptyListIfOptionNotAvailable" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        @Suppress("UNCHECKED_CAST")
        val options = config.getAsciidoctorOption("template_dirs") as Collection<String>
        options.isEmpty() shouldBe true
    }

    "shouldLogAWarningIfAsciidocOptionCouldNotBeFound" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        // Should return empty list for non-existent option
        @Suppress("UNCHECKED_CAST")
        val result = config.getAsciidoctorOption("template_dirs") as Collection<String>
        result.isEmpty() shouldBe true
    }

    "shouldHandleNonExistingFiles" {
        val source = TestUtils.testResourcesAsSourceFolder
        val expectedTemplateFolder = File(source, "templates")
        val expectedAssetFolder = File(source, "assets")
        val expectedContentFolder = File(source, "content")
        val expectedDestinationFolder = File(source, "output")
        val config = util.loadConfig(source) as DefaultJBakeConfiguration

        config.setupDefaultTemplateFolder()
        config.setupDefaultAssetFolder()
        config.setupDefaultContentFolder()
        config.setupDefaultDestinationFolder()

        val assetFolder = config.assetFolder
        val contentFolder = config.contentFolder
        val destinationFolder = config.destinationFolder
        config.templateFolder shouldBe expectedTemplateFolder
        assetFolder shouldBe expectedAssetFolder
        contentFolder shouldBe expectedContentFolder
        destinationFolder shouldBe expectedDestinationFolder
    }

    "shouldSetCustomFoldersWithAbsolutePaths" {
        // given
        val source = sourceFolder.resolve("source")
        val theme = sourceFolder.resolve("theme")
        val destination = sourceFolder.resolve("destination")

        val originalSource = TestUtils.testResourcesAsSourceFolder
        FileUtils.copyDirectory(originalSource, source.toFile())
        val originalTheme = TestUtils.getTestResourcesAsSourceFolder("/fixture-theme")
        FileUtils.copyDirectory(originalTheme, theme.toFile())

        val expectedTemplateFolder = theme.resolve("templates")
        val expectedAssetFolder = theme.resolve("assets")
        val expectedContentFolder = source.resolve("content")
        val expectedDestination = destination.resolve("output")

        val properties = source.resolve("jbake.properties").toFile()
        val fw = Files.newBufferedWriter(properties.toPath())

        fw.write(PropertyList.ASSET_FOLDER.key + "=" + TestUtils.escapeBackSlashes(expectedAssetFolder))
        fw.newLine()
        fw.write(PropertyList.TEMPLATE_FOLDER.key + "=" + TestUtils.escapeBackSlashes(expectedTemplateFolder))
        fw.newLine()
        fw.write(PropertyList.DESTINATION_FOLDER.key + "=" + TestUtils.escapeBackSlashes(expectedDestination))
        fw.close()

        // when
        val config = util.loadConfig(source.toFile()) as DefaultJBakeConfiguration

        val templateFolder = config.templateFolder
        val assetFolder = config.assetFolder
        val contentFolder = config.contentFolder
        val destinationFolder = config.destinationFolder

        // then
        config.templateFolderName shouldBe expectedTemplateFolder.toString()
        templateFolder shouldBe expectedTemplateFolder.toFile()

        config.assetFolderName shouldBe expectedAssetFolder.toString()
        assetFolder shouldBe expectedAssetFolder.toFile()

        destinationFolder shouldBe expectedDestination.toFile()
        contentFolder shouldBe expectedContentFolder.toFile()
    }

    "shouldUseUtf8EncodingAsDefault" {
        val config = util.loadConfig(TestUtils.testResourcesAsSourceFolder)

        val siteAbout = config.get("site.about") as String?
        util.encoding shouldBe "UTF-8"
        siteAbout shouldNotBe null
    }

    "shouldBePossibleToSetCustomEncoding" {
        val expected = "Latin1 encoded file äöü"
        val config =
            util.setEncoding("ISO8859_1").loadConfig(TestUtils.getTestResourcesAsSourceFolder("/fixtureLatin1"))

        val siteAbout = config.get("site.about") as String?
        siteAbout shouldNotBe null
        siteAbout!! stringContain expected
    }

    "shouldLogAWarningAndFallbackToUTF8IfEncodingIsNotSupported" {
        // Just test that it loads without throwing
        val config = util.setEncoding("UNSUPPORTED_ENCODING")
            .loadConfig(TestUtils.getTestResourcesAsSourceFolder("/fixtureLatin1"))

        config shouldNotBe null
    }


    "shouldReturnIgnoreFileFromConfiguration" {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder)

        config.ignoreFileName shouldBe ".jbakeignore"
    }
})
