package org.jbake.app.configuration

import ch.qos.logback.classic.Level
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.jbake.TestUtils
import org.jbake.app.JBakeException
import org.jbake.app.LoggingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path

class ConfigUtilTest : LoggingTest() {
    private lateinit var sourceFolder: Path
    private lateinit var util: ConfigUtil

    @BeforeEach
    fun setup(@TempDir folder: Path) {
        this.sourceFolder = folder
        this.util = ConfigUtil()
    }

    @Test
    fun shouldLoadSiteHost() {
        val config = util.loadConfig(TestUtils.testResourcesAsSourceFolder)
        assertThat(config.siteHost).isEqualTo("http://www.jbake.org")
    }

    @Test
    fun shouldLoadADefaultConfiguration() {
        val config = util.loadConfig(TestUtils.testResourcesAsSourceFolder)
        assertDefaultPropertiesPresent(config)
    }

    @Test
    fun shouldLoadACustomConfiguration() {
        val customConfigFile = File(sourceFolder.toFile(), "jbake.properties")

        val writer = BufferedWriter(FileWriter(customConfigFile))
        writer.append("test.property=12345")
        writer.close()

        val configuration = util.loadConfig(sourceFolder.toFile())

        assertThat<Any?>(configuration.get("test.property")).isEqualTo("12345")
        assertDefaultPropertiesPresent(configuration)
    }

    @Test
    fun shouldThrowAnExceptionIfSourcefolderDoesNotExist() {
        val nonExistentSourceFolder = Mockito.mock(fileClz)
        Mockito.`when`(nonExistentSourceFolder.absolutePath).thenReturn("/tmp/nonexistent")
        Mockito.`when`(nonExistentSourceFolder.exists()).thenReturn(false)

        val e = org.junit.jupiter.api.Assertions.assertThrows(
            JBakeException::class.java
        ) { util.loadConfig(nonExistentSourceFolder) }
        assertThat(e.message).isEqualTo("The given source folder '/tmp/nonexistent' does not exist.")
    }

    @Test
    fun shouldAddSourcefolderToConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder)

        assertThat(config.sourceFolder).isEqualTo(sourceFolder)
    }

    @Test
    fun shouldThrowAnExceptionIfSourcefolderIsNotADirectory() {
        val sourceFolder = Mockito.mock(fileClz)
        Mockito.`when`(sourceFolder.exists()).thenReturn(true)
        Mockito.`when`(sourceFolder.isDirectory()).thenReturn(false)

        val e = org.junit.jupiter.api.Assertions.assertThrows(
            JBakeException::class.java
        ) { util.loadConfig(sourceFolder) }
        assertThat(e.message).isEqualTo("The given source folder is not a directory.")
    }

    @Test
    fun shouldReturnDestinationFolderFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "output")
        val config = util.loadConfig(sourceFolder)

        assertThat(config.destinationFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldReturnAssetFolderFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "assets")
        val config = util.loadConfig(sourceFolder)

        assertThat(config.assetFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldReturnTemplateFolderFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "templates")
        val config = util.loadConfig(sourceFolder)

        assertThat(config.templateFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldReturnContentFolderFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "content")
        val config = util.loadConfig(sourceFolder)

        assertThat(config.contentFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldGetTemplateFileDoctype() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedTemplateFile = File(sourceFolder, "templates/index.ftl")
        val config = util.loadConfig(sourceFolder)

        val templateFile = config.getTemplateFileByDocType("masterindex")
        assertThat(templateFile).isEqualTo(expectedTemplateFile)

        val templateFile2 = config.getTemplateByDocType("team")
        assertThat(templateFile2).isEqualTo("special/team.tpl")
    }

    @Test
    fun shouldLogWarningIfDocumentTypeNotFound() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder)

        config.getTemplateFileByDocType("none")

        Mockito.verify(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent.capture())

        val loggingEvent = captorLoggingEvent.getValue()

        assertThat(loggingEvent.message)
            .isEqualTo("Cannot find configuration key '{}' for document type '{}'")
    }

    @Test
    fun shouldGetTemplateOutputExtension() {
        val docType = "masterindex"
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setTemplateExtensionForDocType(docType, ".xhtml")

        val extension = config.getOutputExtensionByDocType(docType)

        assertThat(extension).isEqualTo(".xhtml")
    }

    @Test
    fun shouldGetMarkdownExtensionsAsList() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        val markdownExtensions = config.markdownExtensions

        assertThat(markdownExtensions)
            .containsExactly("HARDWRAPS", "AUTOLINKS", "FENCED_CODE_BLOCKS", "DEFINITIONS")
    }

    @Test
    fun shouldReturnConfiguredDocTypes() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        val docTypes = config.documentTypes

        assertThat(docTypes).containsExactly(
            "allcontent",
            "team",
            "masterindex",
            "feed",
            "error404",
            "archive",
            "tag",
            "tagsindex",
            "sitemap",
            "post",
            "page"
        )
    }

    @Test
    fun shouldReturnAListOfAsciidoctorOptionsKeys() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val options = config.asciidoctorOptionKeys

        assertThat(options).contains("requires", "template_dirs")
    }

    @Test
    fun shouldReturnAnAsciidoctorOption() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val option = config.getAsciidoctorOption("requires") as Collection<String>

        assertThat(option).contains("asciidoctor-diagram")
    }

    @Test
    fun shouldReturnAnAsciidoctorOptionWithAListValue() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val option = config.getAsciidoctorOption("template_dirs") as Collection<String>

        assertThat(option).contains("src/template1", "src/template2")
    }

    @Test
    fun shouldReturnEmptyListIfOptionNotAvailable() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        val options = config.getAsciidoctorOption("template_dirs") as Collection<String>

        assertThat(options).isEmpty()
    }

    @Test
    fun shouldLogAWarningIfAsciidocOptionCouldNotBeFound() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        config.getAsciidoctorOption("template_dirs")

        Mockito.verify(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent.capture())

        val loggingEvent = captorLoggingEvent.getValue()

        assertThat(loggingEvent.message).isEqualTo("Cannot find asciidoctor option '{}.{}'")
    }

    @Test
    fun shouldHandleNonExistingFiles() {
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
        assertThat(config.templateFolder).isEqualTo(expectedTemplateFolder)
        assertThat(assetFolder).isEqualTo(expectedAssetFolder)
        assertThat(contentFolder).isEqualTo(expectedContentFolder)
        assertThat(destinationFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldSetCustomFoldersWithAbsolutePaths() {
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

        fw.write(PropertyList.ASSET_FOLDER.key + "=" + TestUtils.getOsPath(expectedAssetFolder))
        fw.newLine()
        fw.write(PropertyList.TEMPLATE_FOLDER.key + "=" + TestUtils.getOsPath(expectedTemplateFolder))
        fw.newLine()
        fw.write(PropertyList.DESTINATION_FOLDER.key + "=" + TestUtils.getOsPath(expectedDestination))
        fw.close()

        // when
        val config = util.loadConfig(source.toFile()) as DefaultJBakeConfiguration

        val templateFolder = config.templateFolder
        val assetFolder = config.assetFolder
        val contentFolder = config.contentFolder
        val destinationFolder = config.destinationFolder

        // then
        assertThat(config.templateFolderName).isEqualTo(expectedTemplateFolder.toString())
        assertThat(templateFolder).isEqualTo(expectedTemplateFolder.toFile())

        assertThat(config.assetFolderName).isEqualTo(expectedAssetFolder.toString())
        assertThat(assetFolder).isEqualTo(expectedAssetFolder.toFile())

        assertThat(destinationFolder).isEqualTo(expectedDestination.toFile())
        assertThat(contentFolder).isEqualTo(expectedContentFolder.toFile())
    }

    @Test
    fun shouldUseUtf8EncodingAsDefault() {
        val unicodeString = "中文属性使用默认Properties编码"
        val config = util.loadConfig(TestUtils.testResourcesAsSourceFolder)

        val siteAbout = config.get("site.about") as String?
        assertThat(util.encoding).isEqualTo("UTF-8")
        assertThat(siteAbout).inUnicode().startsWith(unicodeString)
    }

    @Test
    fun shouldBePossibleToSetCustomEncoding() {
        val expected = "Latin1 encoded file äöü"
        val config =
            util.setEncoding("ISO8859_1").loadConfig(TestUtils.getTestResourcesAsSourceFolder("/fixtureLatin1"))

        val siteAbout = config.get("site.about") as String?
        assertThat(siteAbout).contains(expected)
    }

    @Test
    fun shouldLogAWarningAndFallbackToUTF8IfEncodingIsNotSupported() {
        util.setEncoding("UNSUPPORTED_ENCODING")
            .loadConfig(TestUtils.getTestResourcesAsSourceFolder("/fixtureLatin1"))
        Mockito.verify(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent.capture())

        val loggingEvent = captorLoggingEvent.getValue()

        assertThat(loggingEvent.level).isEqualTo(Level.WARN)
        assertThat(loggingEvent.getFormattedMessage())
            .isEqualTo("Unsupported encoding 'UNSUPPORTED_ENCODING'. Using default encoding 'UTF-8'")
    }


    @Test
    fun shouldReturnIgnoreFileFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util.loadConfig(sourceFolder)

        assertThat(config.ignoreFileName).isEqualTo(".jbakeignore")
    }

    @Throws(IllegalAccessException::class)
    private fun assertDefaultPropertiesPresent(config: JBakeConfiguration) {
        for (field in JBakeConfiguration::class.java.getFields()) {
            if (field.isAccessible) {
                val key = field.get("") as String
                println("Key: $key")
                assertThat<Any?>(config.get(key)).isNotNull()
            }
        }
    }
}
