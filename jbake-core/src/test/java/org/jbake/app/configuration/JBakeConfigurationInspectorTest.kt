package org.jbake.app.configuration

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import org.jbake.TestUtils
import org.jbake.app.JBakeException
import org.jbake.app.LoggingTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito
import java.io.File
import java.nio.file.Path

class JBakeConfigurationInspectorTest : LoggingTest() {
    private var folder: Path? = null

    @BeforeEach
    fun setup(@TempDir folder: Path) {
        this.folder = folder
    }


    @Test
    @Throws(Exception::class)
    fun shouldThrowExceptionIfSourceFolderDoesNotExist() {
        val nonExistentFile = File(folder!!.toFile(), "nofolder")
        val configuration = Mockito.mock<JBakeConfiguration>(JBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.sourceFolder).thenReturn(nonExistentFile)

        val inspector = JBakeConfigurationInspector(configuration)

        val e = Assertions.assertThrows<JBakeException>(JBakeException::class.java, Executable { inspector.inspect() })

        org.assertj.core.api.Assertions.assertThat(e.message)
            .isEqualTo("Error: Source folder must exist: " + nonExistentFile.getAbsolutePath())
    }

    @Test
    @Throws(Exception::class)
    fun shouldThrowExceptionIfSourceFolderIsNotReadable() {
        val nonReadableFile = Mockito.mock<File>(File::class.java)
        Mockito.`when`<Boolean?>(nonReadableFile.exists()).thenReturn(true)
        Mockito.`when`<Boolean?>(nonReadableFile.isDirectory()).thenReturn(true)
        Mockito.`when`<Boolean?>(nonReadableFile.canRead()).thenReturn(false)

        val configuration = Mockito.mock<JBakeConfiguration>(JBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.sourceFolder).thenReturn(nonReadableFile)

        val inspector = JBakeConfigurationInspector(configuration)

        val e = Assertions.assertThrows<JBakeException>(JBakeException::class.java, Executable { inspector.inspect() })

        org.assertj.core.api.Assertions.assertThat(e.message)
            .isEqualTo("Error: Source folder is not readable: " + nonReadableFile.getAbsolutePath())
    }

    @Test
    @Throws(Exception::class)
    fun shouldThrowExceptionIfTemplateFolderDoesNotExist() {
        val templateFolderName = "template/custom"
        val expectedFolder = File(folder!!.toFile(), templateFolderName)
        val configuration = Mockito.mock<JBakeConfiguration>(JBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.sourceFolder).thenReturn(folder!!.toFile())
        Mockito.`when`<Any?>(configuration.templateFolder).thenReturn(expectedFolder)

        val inspector = JBakeConfigurationInspector(configuration)

        val e = Assertions.assertThrows<JBakeException>(JBakeException::class.java, Executable { inspector.inspect() })

        org.assertj.core.api.Assertions.assertThat(e.message)
            .isEqualTo("Error: Required folder cannot be found! Expected to find [template.folder] at: " + expectedFolder.getAbsolutePath())
    }

    @Test
    @Throws(Exception::class)
    fun shouldThrowExceptionIfContentFolderDoesNotExist() {
        val contentFolderName = "content"
        val templateFolderName = "template"
        val templateFolder = TestUtils.newFolder(folder!!.toFile(), templateFolderName)
        val contentFolder = File(folder!!.toFile(), contentFolderName)

        val configuration = Mockito.mock<JBakeConfiguration>(JBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.sourceFolder).thenReturn(folder!!.toFile())
        Mockito.`when`<Any?>(configuration.templateFolder).thenReturn(templateFolder)
        Mockito.`when`<Any?>(configuration.contentFolder).thenReturn(contentFolder)

        val inspector = JBakeConfigurationInspector(configuration)


        val e = Assertions.assertThrows<JBakeException>(JBakeException::class.java, Executable { inspector.inspect() })

        org.assertj.core.api.Assertions.assertThat(e.message)
            .isEqualTo("Error: Required folder cannot be found! Expected to find [content.folder] at: " + contentFolder.getAbsolutePath())
    }

    @Test
    @Throws(Exception::class)
    fun shouldCreateDestinationFolderIfNotExists() {
        val contentFolderName = "content"
        val templateFolderName = "template"
        val destinationFolderName = "output"

        val templateFolder = TestUtils.newFolder(folder!!.toFile(), templateFolderName)
        val contentFolder = TestUtils.newFolder(folder!!.toFile(), contentFolderName)
        val destinationFolder = File(folder!!.toFile(), destinationFolderName)

        val configuration = Mockito.mock<JBakeConfiguration>(JBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.sourceFolder).thenReturn(folder!!.toFile())
        Mockito.`when`<Any?>(configuration.templateFolder).thenReturn(templateFolder)
        Mockito.`when`<Any?>(configuration.contentFolder).thenReturn(contentFolder)
        Mockito.`when`<Any?>(configuration.destinationFolder).thenReturn(destinationFolder)
        Mockito.`when`<Any?>(configuration.assetFolder).thenReturn(destinationFolder)

        val inspector = JBakeConfigurationInspector(configuration)

        inspector.inspect()

        org.assertj.core.api.Assertions.assertThat(destinationFolder).exists()
    }

    @Test
    @Throws(Exception::class)
    fun shouldThrowExceptionIfDestinationFolderNotWritable() {
        val contentFolderName = "content"
        val templateFolderName = "template"

        val templateFolder = TestUtils.newFolder(folder!!.toFile(), templateFolderName)
        val contentFolder = TestUtils.newFolder(folder!!.toFile(), contentFolderName)
        val destinationFolder = Mockito.mock<File>(File::class.java)
        Mockito.`when`<Boolean?>(destinationFolder.exists()).thenReturn(true)

        val configuration = Mockito.mock<JBakeConfiguration>(JBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.sourceFolder).thenReturn(folder!!.toFile())
        Mockito.`when`<Any?>(configuration.templateFolder).thenReturn(templateFolder)
        Mockito.`when`<Any?>(configuration.contentFolder).thenReturn(contentFolder)
        Mockito.`when`<Any?>(configuration.destinationFolder).thenReturn(destinationFolder)

        val inspector = JBakeConfigurationInspector(configuration)

        val e = Assertions.assertThrows<JBakeException>(JBakeException::class.java, Executable { inspector.inspect() })

        org.assertj.core.api.Assertions.assertThat(e.message).contains("Error: Destination folder is not writable:")
    }

    @Test
    @Throws(Exception::class)
    fun shouldLogWarningIfAssetFolderDoesNotExist() {
        val contentFolderName = "content"
        val templateFolderName = "template"
        val destinationFolderName = "output"
        val assetFolderName = "assets"

        val templateFolder = TestUtils.newFolder(folder!!.toFile(), templateFolderName)
        val contentFolder = TestUtils.newFolder(folder!!.toFile(), contentFolderName)
        val destinationFolder = TestUtils.newFolder(folder!!.toFile(), destinationFolderName)
        val assetFolder = File(folder!!.toFile(), assetFolderName)

        val configuration = Mockito.mock<JBakeConfiguration>(JBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.sourceFolder).thenReturn(folder!!.toFile())
        Mockito.`when`<Any?>(configuration.templateFolder).thenReturn(templateFolder)
        Mockito.`when`<Any?>(configuration.contentFolder).thenReturn(contentFolder)
        Mockito.`when`<Any?>(configuration.destinationFolder).thenReturn(destinationFolder)
        Mockito.`when`<Any?>(configuration.assetFolder).thenReturn(assetFolder)


        val inspector = JBakeConfigurationInspector(configuration)

        inspector.inspect()

        Mockito.verify<Appender<ILoggingEvent?>?>(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent.capture())

        val loggingEvent = captorLoggingEvent.getValue()

        org.assertj.core.api.Assertions.assertThat(loggingEvent.getMessage())
            .isEqualTo("No asset folder '{}' was found!")
        org.assertj.core.api.Assertions.assertThat(loggingEvent.getFormattedMessage())
            .isEqualTo("No asset folder '" + assetFolder.getAbsolutePath() + "' was found!")
    }
}
