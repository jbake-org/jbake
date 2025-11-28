package org.jbake.app.configuration

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.jbake.TestUtils.newFolder
import org.jbake.app.JBakeException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class JBakeConfigurationInspectorTest : StringSpec({

    lateinit var folder: Path

    beforeTest {
        folder = Files.createTempDirectory("jbake-test")
    }

    afterTest {
        folder.toFile().deleteRecursively()
    }

    "shouldThrowExceptionIfSourceFolderDoesNotExist" {
        val nonExistentFile = File(folder.toFile(), "nofolder")
        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceFolder } returns nonExistentFile

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Source folder must exist: " + nonExistentFile.absolutePath
    }

    "shouldThrowExceptionIfSourceFolderIsNotReadable" {
        val nonReadableFile = mockk<File>()
        every { nonReadableFile.exists() } returns true
        every { nonReadableFile.isDirectory() } returns true
        every { nonReadableFile.canRead() } returns false
        every { nonReadableFile.absolutePath } returns "/tmp/nonreadable"

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceFolder } returns nonReadableFile

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Source folder is not readable: " + nonReadableFile.absolutePath
    }

    "shouldThrowExceptionIfTemplateFolderDoesNotExist" {
        val templateFolderName = "template/custom"
        val expectedFolder = File(folder.toFile(), templateFolderName)
        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceFolder } returns folder.toFile()
        every { configuration.templateFolder } returns expectedFolder

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Required folder cannot be found! Expected to find [template.folder] at: " + expectedFolder.absolutePath
    }

    "shouldThrowExceptionIfContentFolderDoesNotExist" {
        val contentFolderName = "content"
        val templateFolderName = "template"
        val templateFolder = newFolder(folder.toFile(), templateFolderName)
        val contentFolder = File(folder.toFile(), contentFolderName)

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceFolder } returns folder.toFile()
        every { configuration.templateFolder } returns templateFolder
        every { configuration.contentFolder } returns contentFolder

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Required folder cannot be found! Expected to find [content.folder] at: " + contentFolder.absolutePath
    }

    "shouldCreateDestinationFolderIfNotExists" {
        val contentFolderName = "content"
        val templateFolderName = "template"
        val destinationFolderName = "output"

        val templateFolder = newFolder(folder.toFile(), templateFolderName)
        val contentFolder = newFolder(folder.toFile(), contentFolderName)
        val destinationFolder = File(folder.toFile(), destinationFolderName)

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceFolder } returns folder.toFile()
        every { configuration.templateFolder } returns templateFolder
        every { configuration.contentFolder } returns contentFolder
        every { configuration.destinationFolder } returns destinationFolder
        every { configuration.assetFolder } returns destinationFolder

        val inspector = JBakeConfigurationInspector(configuration)

        inspector.inspect()

        destinationFolder.shouldExist()
    }

    "shouldThrowExceptionIfDestinationFolderNotWritable" {
        val contentFolderName = "content"
        val templateFolderName = "template"

        val templateFolder = newFolder(folder.toFile(), templateFolderName)
        val contentFolder = newFolder(folder.toFile(), contentFolderName)
        val destinationFolder = mockk<File>()
        every { destinationFolder.exists() } returns true
        every { destinationFolder.canWrite() } returns false
        every { destinationFolder.absolutePath } returns "/tmp/notwritable"

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceFolder } returns folder.toFile()
        every { configuration.templateFolder } returns templateFolder
        every { configuration.contentFolder } returns contentFolder
        every { configuration.destinationFolder } returns destinationFolder

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldContain "Error: Destination folder is not writable:"
    }

    "shouldLogWarningIfAssetFolderDoesNotExist" {
        val contentFolderName = "content"
        val templateFolderName = "template"
        val destinationFolderName = "output"
        val assetFolderName = "assets"

        val templateFolder = newFolder(folder.toFile(), templateFolderName)
        val contentFolder = newFolder(folder.toFile(), contentFolderName)
        val destinationFolder = newFolder(folder.toFile(), destinationFolderName)
        val assetFolder = File(folder.toFile(), assetFolderName)

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceFolder } returns folder.toFile()
        every { configuration.templateFolder } returns templateFolder
        every { configuration.contentFolder } returns contentFolder
        every { configuration.destinationFolder } returns destinationFolder
        every { configuration.assetFolder } returns assetFolder

        val inspector = JBakeConfigurationInspector(configuration)

        // Should not throw exception, just log warning
        inspector.inspect()

        // Test passes if no exception is thrown
        assetFolder.exists() shouldBe false
    }
})
