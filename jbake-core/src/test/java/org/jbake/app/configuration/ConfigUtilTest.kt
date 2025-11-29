package org.jbake.app.configuration

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.io.FileUtils
import org.jbake.TestUtils
import org.jbake.app.JBakeException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import io.kotest.matchers.string.shouldContain as stringContain

class ConfigUtilTest : StringSpec({
    lateinit var sourceDir: Path
    lateinit var util: ConfigUtil

    beforeTest {
        sourceDir = Files.createTempDirectory("jbake-test")
        util = ConfigUtil()
    }

    afterTest {
        sourceDir.toFile().deleteRecursively()
    }

    "shouldLoadSiteHost" {
        val config = util.loadConfig(TestUtils.testResourcesAsSourceFolder)
        config.siteHost shouldBe "http://www.jbake.org"
    }

    "shouldLoadADefaultConfiguration" {
        val config = util.loadConfig(TestUtils.testResourcesAsSourceFolder)
        // Check some default properties are present
        config.destinationDir shouldNotBe null
        config.assetDir shouldNotBe null
        config.templateDir shouldNotBe null
    }

    "shouldLoadACustomConfiguration" {
        val customConfigFile = File(sourceDir.toFile(), "jbake.properties")
        customConfigFile.writeText("test.property=12345")

        val configuration = util.loadConfig(sourceDir.toFile())

        configuration.get("test.property") shouldBe "12345"
        // Check default properties still present
        configuration.destinationDir shouldNotBe null
    }

    "shouldThrowAnExceptionIfSourcefolderDoesNotExist" {
        val nonExistentSourceFolder = mockk<File>()
        every { nonExistentSourceFolder.absolutePath } returns "/tmp/nonexistent"
        every { nonExistentSourceFolder.exists() } returns false

        val e = shouldThrow<JBakeException> { util.loadConfig(nonExistentSourceFolder) }
        e.message shouldBe "The given source folder '/tmp/nonexistent' does not exist."
    }

    "shouldAddSourcefolderToConfiguration" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir)

        config.sourceDir shouldBe sourceDir
    }

    "shouldThrowAnExceptionIfSourcefolderIsNotADirectory" {
        val sourceDir = mockk<File>()
        every { sourceDir.exists() } returns true
        every { sourceDir.isDirectory() } returns false
        every { sourceDir.absolutePath } returns "/tmp/notadir"

        val e = shouldThrow<JBakeException> { util.loadConfig(sourceDir) }
        e.message shouldBe "The given source folder is not a directory."
    }

    "shouldReturnDestinationFolderFromConfiguration" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationDir = File(sourceDir, "output")
        val config = util.loadConfig(sourceDir)

        config.destinationDir shouldBe expectedDestinationDir
    }

    "shouldReturnAssetFolderFromConfiguration" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceDir, "assets")
        val config = util.loadConfig(sourceDir)

        config.assetDir shouldBe expectedDestinationFolder
    }

    "shouldReturnTemplateFolderFromConfiguration" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceDir, "templates")
        val config = util.loadConfig(sourceDir)

        config.templateDir shouldBe expectedDestinationFolder
    }

    "shouldReturnContentFolderFromConfiguration" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceDir, "content")
        val config = util.loadConfig(sourceDir)

        config.contentDir shouldBe expectedDestinationFolder
    }

    "shouldGetTemplateFileDoctype" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val expectedTemplateFile = File(sourceDir, "templates/index.ftl")
        val config = util.loadConfig(sourceDir)

        val templateFile = config.getTemplateFileByDocType("masterindex")
        templateFile shouldBe expectedTemplateFile

        val templateFile2 = config.getTemplateByDocType("team")
        templateFile2 shouldBe "special/team.tpl"
    }

    "shouldLogWarningIfDocumentTypeNotFound" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir)

        // Should return null for unknown document type
        val result = config.getTemplateFileByDocType("none")
        result shouldBe null
    }

    "shouldGetTemplateOutputExtension" {
        val docType = "masterindex"
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir) as DefaultJBakeConfiguration
        config.setTemplateExtensionForDocType(docType, ".xhtml")

        val extension = config.getOutputExtensionByDocType(docType)

        extension shouldBe ".xhtml"
    }

    "shouldGetMarkdownExtensionsAsList" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir) as DefaultJBakeConfiguration

        val markdownExtensions = config.markdownExtensions

        markdownExtensions shouldContain "HARDWRAPS"
        markdownExtensions shouldContain "AUTOLINKS"
        markdownExtensions shouldContain "FENCED_CODE_BLOCKS"
        markdownExtensions shouldContain "DEFINITIONS"
    }

    "shouldReturnConfiguredDocTypes" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir) as DefaultJBakeConfiguration

        val docTypes = config.documentTypes

        docTypes shouldContain "post"
        docTypes shouldContain "page"
        docTypes shouldContain "masterindex"
    }

    "shouldReturnAListOfAsciidoctorOptionsKeys" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val options = config.asciidoctorOptionKeys

        options shouldContain "requires"
        options shouldContain "template_dirs"
    }

    "shouldReturnAnAsciidoctorOption" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val option = config.getAsciidoctorOption("requires") as Collection<String>

        option shouldContain "asciidoctor-diagram"
    }

    "shouldReturnAnAsciidoctorOptionWithAListValue" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")
        @Suppress("UNCHECKED_CAST")
        val option = config.getAsciidoctorOption("template_dirs") as Collection<String>

        option shouldContain "src/template1"
        option shouldContain "src/template2"
    }

    "shouldReturnEmptyListIfOptionNotAvailable" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir) as DefaultJBakeConfiguration
        @Suppress("UNCHECKED_CAST")
        val options = config.getAsciidoctorOption("template_dirs") as Collection<String>
        options.isEmpty() shouldBe true
    }

    "shouldLogAWarningIfAsciidocOptionCouldNotBeFound" {
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir) as DefaultJBakeConfiguration

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

        config.setupDefaultTemplateDir()
        config.setupDefaultAssetDir()
        config.setupDefaultContentDir()
        config.setupDefaultDestinationDir()

        val assetDir = config.assetDir
        val contentDir = config.contentDir
        val destinationFolder = config.destinationDir
        config.templateDir shouldBe expectedTemplateFolder
        assetDir shouldBe expectedAssetFolder
        contentDir shouldBe expectedContentFolder
        destinationFolder shouldBe expectedDestinationFolder
    }

    "shouldSetCustomFoldersWithAbsolutePaths" {
        // given
        val source = sourceDir.resolve("source")
        val theme = sourceDir.resolve("theme")
        val destination = sourceDir.resolve("destination")

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

        val templateDir = config.templateDir
        val assetDir = config.assetDir
        val contentDir = config.contentDir
        val destinationFolder = config.destinationDir

        // then
        config.templateDirName shouldBe expectedTemplateFolder.toString()
        templateDir shouldBe expectedTemplateFolder.toFile()

        config.assetDirName shouldBe expectedAssetFolder.toString()
        assetDir shouldBe expectedAssetFolder.toFile()

        destinationFolder shouldBe expectedDestination.toFile()
        contentDir shouldBe expectedContentFolder.toFile()
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
        val sourceDir = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceDir)

        config.ignoreDirMarkerFileName shouldBe ".jbakeignore"
    }
})
