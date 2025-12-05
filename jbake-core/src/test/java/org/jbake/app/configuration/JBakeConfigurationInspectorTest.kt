package org.jbake.app.configuration

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.jbake.TestUtils.newDir
import org.jbake.app.JBakeExitException
import java.io.File
import java.nio.file.Files

class JBakeConfigurationInspectorTest : StringSpec({

    lateinit var tempDir: File

    beforeTest {
        tempDir = Files.createTempDirectory("jbake-test").toFile()
    }

    afterTest {
        tempDir.deleteRecursively()
    }

    "shouldThrowExceptionIfSourceDirDoesNotExist" {
        val nonExistentFile = File(tempDir, "nofolder")
        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns nonExistentFile

        val e = shouldThrow<JBakeExitException> { JBakeConfigurationInspector(configuration).inspect() }
        e.message shouldBe "Source dir must exist: " + nonExistentFile.absolutePath
    }

    "shouldThrowExceptionIfSourceDirIsNotReadable" {
        val nonReadableFile = mockk<File>()
        every { nonReadableFile.exists() } returns true
        every { nonReadableFile.isDirectory() } returns true
        every { nonReadableFile.canRead() } returns false
        every { nonReadableFile.absolutePath } returns "/tmp/nonreadable"

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns nonReadableFile

        val e = shouldThrow<JBakeExitException> { JBakeConfigurationInspector(configuration).inspect() }
        e.message shouldBe "Source dir is not readable: " + nonReadableFile.absolutePath
    }

    "shouldThrowExceptionIfTemplateDirDoesNotExist" {
        val expectedDir = File(tempDir, "template/custom")
        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir
        every { configuration.templateDir } returns expectedDir

        val inspector = JBakeConfigurationInspector(configuration)

        val e = shouldThrow<JBakeExitException> { inspector.inspect() }

        e.message shouldBe "Required dir cannot be found! Expected to find [template.folder] at: " + expectedDir.absolutePath
    }

    "shouldThrowExceptionIfContentDirDoesNotExist" {
        val contentDir = File(tempDir, "content")

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir
        every { configuration.templateDir } returns newDir(tempDir, "template")
        every { configuration.contentDir } returns contentDir

        val e = shouldThrow<JBakeExitException> { JBakeConfigurationInspector(configuration).inspect() }
        e.message shouldBe "Required dir cannot be found! Expected to find [content.folder] at: " + contentDir.absolutePath
    }

    "shouldCreateDestinationDirIfNotExists" {

        val templateDir = newDir(tempDir, "template")
        val contentDir = newDir(tempDir, "content")
        val destinationDir = File(tempDir, "output")

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir
        every { configuration.destinationDir } returns destinationDir
        every { configuration.assetDir } returns destinationDir

        JBakeConfigurationInspector(configuration).inspect()
        destinationDir.shouldExist()
    }

    "shouldThrowExceptionIfDestinationDirNotWritable" {

        val templateDir = newDir(tempDir, "template")
        val contentDir = newDir(tempDir, "content")
        val destinationDir = mockk<File>()
        every { destinationDir.exists() } returns true
        every { destinationDir.canWrite() } returns false
        every { destinationDir.absolutePath } returns "/tmp/notwritable"

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir
        every { configuration.destinationDir } returns destinationDir

        val e = shouldThrow<JBakeExitException> { JBakeConfigurationInspector(configuration).inspect() }
        e.message shouldContain "Destination dir is not writable:"
    }

    "shouldLogWarningIfAssetDirDoesNotExist" {

        val templateDir = newDir(tempDir, "template")
        val contentDir = newDir(tempDir, "content")
        val destinationDir = newDir(tempDir, "output")
        val assetDir = File(tempDir, "assets")

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.sourceDir } returns tempDir
        every { configuration.templateDir } returns templateDir
        every { configuration.contentDir } returns contentDir
        every { configuration.destinationDir } returns destinationDir
        every { configuration.assetDir } returns assetDir

        // Should not throw exception, just log warning
        JBakeConfigurationInspector(configuration).inspect()

        // Test passes if no exception is thrown
        assetDir.exists() shouldBe false
    }
})
