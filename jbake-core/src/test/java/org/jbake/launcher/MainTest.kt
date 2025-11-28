package org.jbake.launcher

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.util.PathUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files

class MainTest : StringSpec({
    lateinit var folder: File
    lateinit var standardOut: PrintStream
    lateinit var outputStreamCaptor: ByteArrayOutputStream
    lateinit var main: Main
    lateinit var mockBaker: Baker
    lateinit var mockJetty: JettyServer
    lateinit var mockWatcher: BakeWatcher
    lateinit var mockConfigUtil: ConfigUtil
    lateinit var mockFactory: JBakeConfigurationFactory
    lateinit var originalWorkDir: String

    beforeTest {
        folder = Files.createTempDirectory("jbake-test").toFile()
        standardOut = System.out
        outputStreamCaptor = ByteArrayOutputStream()
        originalWorkDir = PathUtils.SYSPROP_USER_DIR

        mockBaker = mockk(relaxed = true)
        mockJetty = mockk(relaxed = true)
        mockWatcher = mockk(relaxed = true)
        mockConfigUtil = mockk(relaxed = true)
        mockFactory = mockk(relaxed = true)

        main = Main(mockBaker, mockJetty, mockWatcher)
        mockFactory.configUtil = mockConfigUtil
        main.jBakeConfigurationFactory = mockFactory
        System.setOut(PrintStream(outputStreamCaptor))
    }

    afterTest {
        System.setProperty("user.dir", originalWorkDir)
        System.setOut(standardOut)
        folder.deleteRecursively()
    }

    "launchJetty" {
        val currentWorkingdir = File(folder, "src/jbake").apply { mkdirs() }
        val expectedOutputDir = File(currentWorkingdir, "output")
        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)

        every { mockFactory.createJettyJbakeConfiguration(any<File>(), any(), any(), any()) } returns configuration
        every { mockFactory.setEncoding(any()) } returns mockFactory
        System.setProperty("user.dir", currentWorkingdir.path)

        // Ensure the configuration returns the expected destination folder when server runs.
        every { configuration.destinationFolder } returns expectedOutputDir

        val args = arrayOf("-s")
        main.run(args)

        verify { mockJetty.run(expectedOutputDir.path, configuration) }
    }

    "launchBakeWithCustomPropertiesEncoding" {
        val currentWorkingdir = File(folder, "jbake").apply { mkdirs() }
        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)

        every { mockFactory.setEncoding(any<String>()) } returns mockFactory
        every { mockFactory.createDefaultJbakeConfiguration(any<File>(), any<File>(), any<File>(), any<Boolean>()) } returns configuration
        System.setProperty("user.dir", currentWorkingdir.path)

        val args = arrayOf("-b", "--prop-encoding", "latin1")
        main.run(args)

        verify { mockFactory.setEncoding("latin1") }
    }

    "launchBakeWithDefaultUtf8PropertiesEncoding" {
        val currentWorkingdir = File(folder, "jbake").apply { mkdirs() }
        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)

        every { mockFactory.setEncoding(any<String>()) } returns mockFactory
        every { mockFactory.createDefaultJbakeConfiguration(any<File>(), any<File>(), any<File>(), any<Boolean>()) } returns configuration
        System.setProperty("user.dir", currentWorkingdir.path)

        val args = arrayOf("-b")
        main.run(args)

        verify { mockFactory.setEncoding("utf-8") }
    }

    "launchBakeAndJetty" {
        val currentWorkingdir = File(folder, "src/jbake").apply { mkdirs() }
        val expectedOutput = File(currentWorkingdir, "output")
        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)

        every { mockFactory.createJettyJbakeConfiguration(any<File>(), any<File>(), any<File>(), any()) } returns configuration
        every { mockFactory.setEncoding(any()) } returns mockFactory
        System.setProperty("user.dir", currentWorkingdir.path)

        val args = arrayOf("-b", "-s")
        main.run(args)

        verify { mockJetty.run(expectedOutput.path, configuration) }
    }

    "shouldListCurrentSettings" {
        val src = File(folder, "src/jbake").apply { mkdirs() }
        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)

        every { mockFactory.setEncoding(any()) } returns mockFactory
        every { mockFactory.createDefaultJbakeConfiguration(any<File>(), any<File>(), any<File>(), any()) } returns configuration
        System.setProperty("user.dir", src.path)

        // Ensure ConfigurationPrinter has at least one Property to print the group header
        every { configuration.jbakeProperties } returns mutableListOf(org.jbake.app.configuration.Property("site.host", "Site host"))
        every { configuration.get("site.host") } returns "http://www.jbake.org"

        val args = arrayOf("-ls")
        main.run(args)

        outputStreamCaptor.toString() shouldContain "DEFAULT - Settings" // Why??
    }
})
