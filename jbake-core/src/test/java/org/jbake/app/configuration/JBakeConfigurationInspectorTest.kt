package org.jbake.app.configuration

import org.assertj.core.api.Assertions.assertThat
import org.jbake.TestUtils.newFolder
import org.jbake.app.JBakeException
import org.jbake.app.LoggingTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.*
import java.io.File
import java.nio.file.Path

class JBakeConfigurationInspectorTest : LoggingTest() {
    private lateinit var folder: Path

    @BeforeEach
    fun setup(@TempDir folder: Path) {
        this.folder = folder
    }


    @Test fun shouldThrowExceptionIfSourceFolderDoesNotExist() {
        val nonExistentFile = File(folder.toFile(), "nofolder")
        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.sourceFolder).thenReturn(nonExistentFile)

        val inspector = JBakeConfigurationInspector(configuration)

        val e = assertThrows(JBakeException::class.java) { inspector.inspect() }

        assertThat(e.message)
            .isEqualTo("Error: Source folder must exist: " + nonExistentFile.absolutePath)
    }

    @Test fun shouldThrowExceptionIfSourceFolderIsNotReadable() {
        val nonReadableFile = mock(File::class.java)
        `when`(nonReadableFile.exists()).thenReturn(true)
        `when`(nonReadableFile.isDirectory()).thenReturn(true)
        `when`(nonReadableFile.canRead()).thenReturn(false)

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.sourceFolder).thenReturn(nonReadableFile)

        val inspector = JBakeConfigurationInspector(configuration)

        val e = assertThrows(JBakeException::class.java) { inspector.inspect() }

        assertThat(e.message)
            .isEqualTo("Error: Source folder is not readable: " + nonReadableFile.absolutePath)
    }

    @Test fun shouldThrowExceptionIfTemplateFolderDoesNotExist() {
        val templateFolderName = "template/custom"
        val expectedFolder = File(folder.toFile(), templateFolderName)
        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.sourceFolder).thenReturn(folder.toFile())
        `when`(configuration.templateFolder).thenReturn(expectedFolder)

        val inspector = JBakeConfigurationInspector(configuration)

        val e = assertThrows(JBakeException::class.java) { inspector.inspect() }

        assertThat(e.message)
            .isEqualTo("Error: Required folder cannot be found! Expected to find [template.folder] at: " + expectedFolder.absolutePath)
    }

    @Test fun shouldThrowExceptionIfContentFolderDoesNotExist() {
        val contentFolderName = "content"
        val templateFolderName = "template"
        val templateFolder = newFolder(folder.toFile(), templateFolderName)
        val contentFolder = File(folder.toFile(), contentFolderName)

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.sourceFolder).thenReturn(folder.toFile())
        `when`(configuration.templateFolder).thenReturn(templateFolder)
        `when`(configuration.contentFolder).thenReturn(contentFolder)

        val inspector = JBakeConfigurationInspector(configuration)


        val e = assertThrows(JBakeException::class.java) { inspector.inspect() }

        assertThat(e.message)
            .isEqualTo("Error: Required folder cannot be found! Expected to find [content.folder] at: " + contentFolder.absolutePath)
    }

    @Test fun shouldCreateDestinationFolderIfNotExists() {
        val contentFolderName = "content"
        val templateFolderName = "template"
        val destinationFolderName = "output"

        val templateFolder = newFolder(folder.toFile(), templateFolderName)
        val contentFolder = newFolder(folder.toFile(), contentFolderName)
        val destinationFolder = File(folder.toFile(), destinationFolderName)

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.sourceFolder).thenReturn(folder.toFile())
        `when`(configuration.templateFolder).thenReturn(templateFolder)
        `when`(configuration.contentFolder).thenReturn(contentFolder)
        `when`(configuration.destinationFolder).thenReturn(destinationFolder)
        `when`(configuration.assetFolder).thenReturn(destinationFolder)

        val inspector = JBakeConfigurationInspector(configuration)

        inspector.inspect()

        assertThat(destinationFolder).exists()
    }

    @Test fun shouldThrowExceptionIfDestinationFolderNotWritable() {
        val contentFolderName = "content"
        val templateFolderName = "template"

        val templateFolder = newFolder(folder.toFile(), templateFolderName)
        val contentFolder = newFolder(folder.toFile(), contentFolderName)
        val destinationFolder = mock(File::class.java)
        `when`(destinationFolder.exists()).thenReturn(true)

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.sourceFolder).thenReturn(folder.toFile())
        `when`(configuration.templateFolder).thenReturn(templateFolder)
        `when`(configuration.contentFolder).thenReturn(contentFolder)
        `when`(configuration.destinationFolder).thenReturn(destinationFolder)

        val inspector = JBakeConfigurationInspector(configuration)

        val e = assertThrows(JBakeException::class.java) { inspector.inspect() }

        assertThat(e.message).contains("Error: Destination folder is not writable:")
    }

    @Test fun shouldLogWarningIfAssetFolderDoesNotExist() {
        val contentFolderName = "content"
        val templateFolderName = "template"
        val destinationFolderName = "output"
        val assetFolderName = "assets"

        val templateFolder = newFolder(folder.toFile(), templateFolderName)
        val contentFolder = newFolder(folder.toFile(), contentFolderName)
        val destinationFolder = newFolder(folder.toFile(), destinationFolderName)
        val assetFolder = File(folder.toFile(), assetFolderName)

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.sourceFolder).thenReturn(folder.toFile())
        `when`(configuration.templateFolder).thenReturn(templateFolder)
        `when`(configuration.contentFolder).thenReturn(contentFolder)
        `when`(configuration.destinationFolder).thenReturn(destinationFolder)
        `when`(configuration.assetFolder).thenReturn(assetFolder)


        val inspector = JBakeConfigurationInspector(configuration)

        inspector.inspect()

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture())
        assertThat(captorLoggingEvent.value.message).isEqualTo("No asset folder '{}' was found!")
        assertThat(captorLoggingEvent.value.formattedMessage).isEqualTo("No asset folder '${assetFolder.absolutePath}' was found!")
    }
}
