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

    lateinit var tempDir: Path

    beforeTest {
        tempDir = Files.createTempDirectory("jbake-test")
    }

    afterTest {
        tempDir.toFile().deleteRecursively()
    }

    "shouldThrowExceptionIfSourceDirDoesNotExist" {
        val nonExistentFile = File(tempDir.toFile(), "nofolder")
        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns nonExistentFile

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Source dir must exist: " + nonExistentFile.absolutePath
    }

    "shouldThrowExceptionIfSourceDirIsNotReadable" {
        val nonReadableFile = mockk<File>()
        every { nonReadableFile.exists() } returns true
        every { nonReadableFile.isDirectory() } returns true
        every { nonReadableFile.canRead() } returns false
        every { nonReadableFile.absolutePath } returns "/tmp/nonreadable"

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns nonReadableFile

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Source dir is not readable: " + nonReadableFile.absolutePath
    }

    "shouldThrowExceptionIfTemplateDirDoesNotExist" {
        val templateDirName = "template/custom"
        val expectedDir = File(tempDir.toFile(), templateDirName)
        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir.toFile()
        every { configuration.templateDir } returns expectedDir

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Required dir cannot be found! Expected to find [template.folder] at: " + expectedDir.absolutePath
    }

    "shouldThrowExceptionIfContentDirDoesNotExist" {
        val contentDirName = "content"
        val templateDirName = "template"
        val templateDir = newDir(tempDir.toFile(), templateDirName)
        val contentDir = File(tempDir.toFile(), contentDirName)

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir.toFile()
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldBe "Error: Required dir cannot be found! Expected to find [content.folder] at: " + contentDir.absolutePath
    }

    "shouldCreateDestinationDirIfNotExists" {
        val contentDirName = "content"
        val templateDirName = "template"
        val destinationDirName = "output"

        val templateDir = newDir(tempDir.toFile(), templateDirName)
        val contentDir = newDir(tempDir.toFile(), contentDirName)
        val destinationDir = File(tempDir.toFile(), destinationDirName)

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir.toFile()
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir
        every { configuration.destinationDir } returns destinationDir
        every { configuration.assetDir } returns destinationDir

        val inspector = JBakeConfigurationInspector(configuration)

        inspector.inspect()

        destinationDir.shouldExist()
    }

    "shouldThrowExceptionIfDestinationDirNotWritable" {
        val contentDirName = "content"
        val templateDirName = "template"

        val templateDir = newDir(tempDir.toFile(), templateDirName)
        val contentDir = newDir(tempDir.toFile(), contentDirName)
        val destinationDir = mockk<File>()
        every { destinationDir.exists() } returns true
        every { destinationDir.canWrite() } returns false
        every { destinationDir.absolutePath } returns "/tmp/notwritable"

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir.toFile()
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir
        every { configuration.destinationDir } returns destinationDir

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeException> { inspector.inspect() }

        e.message shouldContain "Error: Destination dir is not writable:"
    }

    "shouldLogWarningIfAssetDirDoesNotExist" {
        val contentDirName = "content"
        val templateDirName = "template"
        val destinationDirName = "output"
        val assetDirName = "assets"

        val templateDir = newDir(tempDir.toFile(), templateDirName)
        val contentDir = newDir(tempDir.toFile(), contentDirName)
        val destinationDir = newDir(tempDir.toFile(), destinationDirName)
        val assetDir = File(tempDir.toFile(), assetDirName)

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir.toFile()
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir
        every { configuration.destinationDir } returns destinationDir
        every { configuration.assetDir } returns assetDir

        val inspector = JBakeConfigurationInspector(configuration)

        // Should not throw exception, just log warning
        inspector.inspect()

        // Test passes if no exception is thrown
        assetDir.exists() shouldBe false
    }
})
