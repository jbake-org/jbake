package org.jbake.app.configuration

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.jbake.TestUtils.newDir
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
        every { configuration.sourceDir } returns nonExistentFile

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
        every { configuration.sourceDir } returns nonReadableFile

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Source folder is not readable: " + nonReadableFile.absolutePath
    }

    "shouldThrowExceptionIfTemplateFolderDoesNotExist" {
        val templateDirName = "template/custom"
        val expectedDir = File(folder.toFile(), templateDirName)
        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns folder.toFile()
        every { configuration.templateDir } returns expectedDir

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Required folder cannot be found! Expected to find [template.folder] at: " + expectedDir.absolutePath
    }

    "shouldThrowExceptionIfContentFolderDoesNotExist" {
        val contentDirName = "content"
        val templateDirName = "template"
        val templateDir = newDir(folder.toFile(), templateDirName)
        val contentDir = File(folder.toFile(), contentDirName)

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns folder.toFile()
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Required folder cannot be found! Expected to find [content.folder] at: " + contentDir.absolutePath
    }

    "shouldCreateDestinationFolderIfNotExists" {
        val contentDirName = "content"
        val templateDirName = "template"
        val destinationFolderName = "output"

        val templateDir = newDir(folder.toFile(), templateDirName)
        val contentDir = newDir(folder.toFile(), contentDirName)
        val destinationFolder = File(folder.toFile(), destinationFolderName)

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns folder.toFile()
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir
        every { configuration.destinationDir } returns destinationFolder
        every { configuration.assetDir } returns destinationFolder

        val inspector = JBakeConfigurationInspector(configuration)

        inspector.inspect()

        destinationFolder.shouldExist()
    }

    "shouldThrowExceptionIfDestinationFolderNotWritable" {
        val contentDirName = "content"
        val templateDirName = "template"

        val templateDir = newDir(folder.toFile(), templateDirName)
        val contentDir = newDir(folder.toFile(), contentDirName)
        val destinationFolder = mockk<File>()
        every { destinationFolder.exists() } returns true
        every { destinationFolder.canWrite() } returns false
        every { destinationFolder.absolutePath } returns "/tmp/notwritable"

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns folder.toFile()
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir
        every { configuration.destinationDir } returns destinationFolder

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldContain "Error: Destination folder is not writable:"
    }

    "shouldLogWarningIfAssetFolderDoesNotExist" {
        val contentDirName = "content"
        val templateDirName = "template"
        val destinationFolderName = "output"
        val assetDirName = "assets"

        val templateDir = newDir(folder.toFile(), templateDirName)
        val contentDir = newDir(folder.toFile(), contentDirName)
        val destinationFolder = newDir(folder.toFile(), destinationFolderName)
        val assetDir = File(folder.toFile(), assetDirName)

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns folder.toFile()
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir
        every { configuration.destinationDir } returns destinationFolder
        every { configuration.assetDir } returns assetDir

        val inspector = JBakeConfigurationInspector(configuration)

        // Should not throw exception, just log warning
        inspector.inspect()

        // Test passes if no exception is thrown
        assetDir.exists() shouldBe false
    }
})
