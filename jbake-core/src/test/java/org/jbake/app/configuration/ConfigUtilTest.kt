package org.jbake.app.configuration

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions
import org.jbake.TestUtils
import org.jbake.app.JBakeException
import org.jbake.app.LoggingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
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
        val config = util!!.loadConfig(TestUtils.testResourcesAsSourceFolder)
        assertThat(config.siteHost).isEqualTo("http://www.jbake.org")
    }

    @Test
    fun shouldLoadADefaultConfiguration() {
        val config = util!!.loadConfig(TestUtils.testResourcesAsSourceFolder)
        assertDefaultPropertiesPresent(config)
    }

    @Test
    fun shouldLoadACustomConfiguration() {
        val customConfigFile = File(sourceFolder!!.toFile(), "jbake.properties")

        val writer = BufferedWriter(FileWriter(customConfigFile))
        writer.append("test.property=12345")
        writer.close()

        val configuration = util!!.loadConfig(sourceFolder!!.toFile())

        Assertions.assertThat<Any?>(configuration.get("test.property")).isEqualTo("12345")
        assertDefaultPropertiesPresent(configuration)
    }

    @Test
    fun shouldThrowAnExceptionIfSourcefolderDoesNotExist() {
        val nonExistentSourceFolder = Mockito.mock<File>(File::class.java)
        Mockito.`when`<String>(nonExistentSourceFolder.getAbsolutePath()).thenReturn("/tmp/nonexistent")
        Mockito.`when`<Boolean?>(nonExistentSourceFolder.exists()).thenReturn(false)

        val e = org.junit.jupiter.api.Assertions.assertThrows<JBakeException>(
            JBakeException::class.java,
            Executable { util!!.loadConfig(nonExistentSourceFolder) })
        Assertions.assertThat(e.message).isEqualTo("The given source folder '/tmp/nonexistent' does not exist.")
    }

    @Test
    fun shouldAddSourcefolderToConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder)

        assertThat(config.sourceFolder).isEqualTo(sourceFolder)
    }

    @Test
    fun shouldThrowAnExceptionIfSourcefolderIsNotADirectory() {
        val sourceFolder = Mockito.mock<File>(File::class.java)
        Mockito.`when`<Boolean?>(sourceFolder.exists()).thenReturn(true)
        Mockito.`when`<Boolean?>(sourceFolder.isDirectory()).thenReturn(false)

        val e = org.junit.jupiter.api.Assertions.assertThrows<JBakeException>(
            JBakeException::class.java,
            Executable { util!!.loadConfig(sourceFolder) })
        Assertions.assertThat(e.message).isEqualTo("The given source folder is not a directory.")
    }

    @Test
    fun shouldReturnDestinationFolderFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "output")
        val config = util!!.loadConfig(sourceFolder)

        assertThat(config.destinationFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldReturnAssetFolderFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "assets")
        val config = util!!.loadConfig(sourceFolder)

        assertThat(config.assetFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldReturnTemplateFolderFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "templates")
        val config = util!!.loadConfig(sourceFolder)

        assertThat(config.templateFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldReturnContentFolderFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedDestinationFolder = File(sourceFolder, "content")
        val config = util!!.loadConfig(sourceFolder)

        assertThat(config.contentFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldGetTemplateFileDoctype() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val expectedTemplateFile = File(sourceFolder, "templates/index.ftl")
        val config = util!!.loadConfig(sourceFolder)

        val templateFile = config.getTemplateFileByDocType("masterindex")
        Assertions.assertThat(templateFile).isEqualTo(expectedTemplateFile)

        val templateFile2 = config.getTemplateByDocType("team")
        Assertions.assertThat(templateFile2).isEqualTo("special/team.tpl")
    }

    @Test
    fun shouldLogWarningIfDocumentTypeNotFound() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder)

        config.getTemplateFileByDocType("none")

        Mockito.verify<Appender<ILoggingEvent?>?>(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent.capture())

        val loggingEvent = captorLoggingEvent.getValue()

        Assertions.assertThat(loggingEvent.getMessage())
            .isEqualTo("Cannot find configuration key '{}' for document type '{}'")
    }

    @Test
    fun shouldGetTemplateOutputExtension() {
        val docType = "masterindex"
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setTemplateExtensionForDocType(docType, ".xhtml")

        val extension = config.getOutputExtensionByDocType(docType)

        Assertions.assertThat(extension).isEqualTo(".xhtml")
    }

    @Test
    fun shouldGetMarkdownExtensionsAsList() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        val markdownExtensions = config.getMarkdownExtensions()

        Assertions.assertThat<String>(markdownExtensions)
            .containsExactly("HARDWRAPS", "AUTOLINKS", "FENCED_CODE_BLOCKS", "DEFINITIONS")
    }

    @Test
    fun shouldReturnConfiguredDocTypes() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        val docTypes = config.documentTypes

        Assertions.assertThat<String>(docTypes).containsExactly(
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
        val config = util!!.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val options = config.getAsciidoctorOptionKeys()

        Assertions.assertThat<String>(options).contains("requires", "template_dirs")
    }

    @Test
    fun shouldReturnAnAsciidoctorOption() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val option = config.getAsciidoctorOption("requires")

        Assertions.assertThat<String>(option).contains("asciidoctor-diagram")
    }

    @Test
    fun shouldReturnAnAsciidoctorOptionWithAListValue() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder) as DefaultJBakeConfiguration
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram")
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2")

        val option = config.getAsciidoctorOption("template_dirs")

        Assertions.assertThat<String>(option).contains("src/template1", "src/template2")
    }

    @Test
    fun shouldReturnEmptyListIfOptionNotAvailable() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        val options = config.getAsciidoctorOption("template_dirs")

        Assertions.assertThat<String>(options).isEmpty()
    }

    @Test
    fun shouldLogAWarningIfAsciidocOptionCouldNotBeFound() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder) as DefaultJBakeConfiguration

        config.getAsciidoctorOption("template_dirs")

        Mockito.verify<Appender<ILoggingEvent?>?>(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent.capture())

        val loggingEvent = captorLoggingEvent.getValue()

        Assertions.assertThat(loggingEvent.getMessage()).isEqualTo("Cannot find asciidoctor option '{}.{}'")
    }

    @Test
    fun shouldHandleNonExistingFiles() {
        val source = TestUtils.testResourcesAsSourceFolder
        val expectedTemplateFolder = File(source, "templates")
        val expectedAssetFolder = File(source, "assets")
        val expectedContentFolder = File(source, "content")
        val expectedDestinationFolder = File(source, "output")
        val config = util!!.loadConfig(source) as DefaultJBakeConfiguration

        config.setTemplateFolder(null)
        config.setAssetFolder(null)
        config.setContentFolder(null)
        config.destinationFolder = (null)

        val templateFolder = config.getTemplateFolder()
        val assetFolder = config.getAssetFolder()
        val contentFolder = config.getContentFolder()
        val destinationFolder = config.getDestinationFolder()

        Assertions.assertThat(templateFolder).isEqualTo(expectedTemplateFolder)
        Assertions.assertThat(assetFolder).isEqualTo(expectedAssetFolder)
        Assertions.assertThat(contentFolder).isEqualTo(expectedContentFolder)
        Assertions.assertThat(destinationFolder).isEqualTo(expectedDestinationFolder)
    }

    @Test
    fun shouldSetCustomFoldersWithAbsolutePaths() {
        // given
        val source = sourceFolder!!.resolve("source")
        val theme = sourceFolder!!.resolve("theme")
        val destination = sourceFolder!!.resolve("destination")

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
        val config = util!!.loadConfig(source.toFile()) as DefaultJBakeConfiguration

        val templateFolder = config.getTemplateFolder()
        val assetFolder = config.getAssetFolder()
        val contentFolder = config.getContentFolder()
        val destinationFolder = config.getDestinationFolder()

        // then
        Assertions.assertThat(config.getTemplateFolderName()).isEqualTo(expectedTemplateFolder.toString())
        Assertions.assertThat(templateFolder).isEqualTo(expectedTemplateFolder.toFile())

        Assertions.assertThat(config.getAssetFolderName()).isEqualTo(expectedAssetFolder.toString())
        Assertions.assertThat(assetFolder).isEqualTo(expectedAssetFolder.toFile())

        Assertions.assertThat(destinationFolder).isEqualTo(expectedDestination.toFile())
        Assertions.assertThat(contentFolder).isEqualTo(expectedContentFolder.toFile())
    }

    @Test
    fun shouldUseUtf8EncodingAsDefault() {
        val unicodeString = "中文属性使用默认Properties编码"
        val config = util!!.loadConfig(TestUtils.testResourcesAsSourceFolder)

        val siteAbout = config.get("site.about") as String?
        Assertions.assertThat(util!!.encoding).isEqualTo("UTF-8")
        Assertions.assertThat(siteAbout).inUnicode().startsWith(unicodeString)
    }

    @Test
    fun shouldBePossibleToSetCustomEncoding() {
        val expected = "Latin1 encoded file äöü"
        val config =
            util!!.setEncoding("ISO8859_1").loadConfig(TestUtils.getTestResourcesAsSourceFolder("/fixtureLatin1"))

        val siteAbout = config.get("site.about") as String?
        Assertions.assertThat(siteAbout).contains(expected)
    }

    @Test
    fun shouldLogAWarningAndFallbackToUTF8IfEncodingIsNotSupported() {
        val config = util!!.setEncoding("UNSUPPORTED_ENCODING")
            .loadConfig(TestUtils.getTestResourcesAsSourceFolder("/fixtureLatin1"))
        Mockito.verify<Appender<ILoggingEvent?>?>(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent.capture())

        val loggingEvent = captorLoggingEvent.getValue()

        Assertions.assertThat<Level?>(loggingEvent.getLevel()).isEqualTo(Level.WARN)
        Assertions.assertThat(loggingEvent.getFormattedMessage())
            .isEqualTo("Unsupported encoding 'UNSUPPORTED_ENCODING'. Using default encoding 'UTF-8'")
    }


    @Test
    fun shouldReturnIgnoreFileFromConfiguration() {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val config = util!!.loadConfig(sourceFolder)

        assertThat(config.ignoreFileName).isEqualTo(".jbakeignore")
    }

    @Throws(IllegalAccessException::class)
    private fun assertDefaultPropertiesPresent(config: JBakeConfiguration) {
        for (field in JBakeConfiguration::class.java.getFields()) {
            if (field.isAccessible()) {
                val key = field.get("") as String?
                println("Key: " + key)
                Assertions.assertThat<Any?>(config.get(key)).isNotNull()
            }
        }
    }
}
