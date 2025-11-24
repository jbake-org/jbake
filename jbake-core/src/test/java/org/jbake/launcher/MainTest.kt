package org.jbake.launcher

import org.apache.commons.configuration2.ex.ConfigurationException
import org.assertj.core.api.Assertions.assertThat
import org.jbake.TestUtils
import org.jbake.app.JBakeException
import org.jbake.app.LoggingTest
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.util.PathConstants.fS
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Path

// Helper functions for Kotlin null-safety with Mockito
@Suppress("UNCHECKED_CAST")
private fun <T> any(type: Class<T>): T {
    ArgumentMatchers.any(type)
    return null as T
}

@Suppress("UNCHECKED_CAST")
private fun anyString(): String {
    ArgumentMatchers.anyString()
    return ""
}


@ExtendWith(MockitoExtension::class)
internal class MainTest : LoggingTest() {
    private val standardOut: PrintStream? = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()
    private lateinit var main: Main

    @Mock private lateinit var mockBaker: Baker
    @Mock private lateinit var mockJetty: JettyServer
    @Mock private lateinit var mockWatcher: BakeWatcher
    @Mock private lateinit var mockConfigUtil: ConfigUtil
    @Mock private lateinit var mockFactory: JBakeConfigurationFactory

    private var workingdir: String? = null

    @BeforeEach
    fun setUp() {
        this.main = Main(mockBaker, mockJetty, mockWatcher)
        workingdir = System.getProperty("user.dir")
        mockFactory.configUtil = mockConfigUtil
        main.jBakeConfigurationFactory = mockFactory
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @AfterEach
    fun tearDown() {
        System.setProperty("user.dir", workingdir)
        System.setOut(standardOut)
    }

    @Test
    fun launchJetty(@TempDir source: Path) {
        val currentWorkingdir = newFolder(source, "src/jbake")
        val expectedOutput = File(currentWorkingdir, "output")
        val configuration = mockJettyConfiguration(currentWorkingdir, expectedOutput)

        val args = arrayOf("-s")
        main.run(args)

        verify<JettyServer>(mockJetty).run(expectedOutput.path, configuration)
    }

    @Test
    fun launchBakeWithCustomPropertiesEncoding(@TempDir source: Path) {
        val currentWorkingdir = newFolder(source, "jbake")
        mockDefaultJbakeConfiguration(currentWorkingdir)

        val args = arrayOf("-b", "--prop-encoding", "latin1")
        main.run(args)

        verify<JBakeConfigurationFactory>(mockFactory).setEncoding("latin1")
    }

    @Test
    fun launchBakeWithDefaultUtf8PropertiesEncoding(@TempDir source: Path) {
        val currentWorkingdir = newFolder(source, "jbake")
        mockDefaultJbakeConfiguration(currentWorkingdir)

        val args = arrayOf("-b")
        main.run(args)

        verify<JBakeConfigurationFactory>(mockFactory).setEncoding("utf-8")
    }

    @Test
    fun launchBakeAndJetty(@TempDir source: Path) {
        val sourceFolder = newFolder(source, "src/jbake")
        val expectedOutput = newFolder(sourceFolder.toPath(), "output")
        val configuration = mockJettyConfiguration(sourceFolder, expectedOutput)

        val args = arrayOf("-b", "-s")
        main.run(args)

        verify<JettyServer>(mockJetty).run(expectedOutput.path, configuration)
    }

    @Test
    @Throws(ConfigurationException::class)
    fun launchBakeAndJettyWithCustomDirForJetty(@TempDir source: Path) {
        val sourceFolder = newFolder(source, "src/jbake")
        val expectedRunPath = "src" + fS + "jbake" + fS + "output"
        val output = newFolder(source, expectedRunPath)
        val configuration = mockJettyConfiguration(sourceFolder, output)

        val args = arrayOf("-b", "-s", "src/jbake")
        main.run(args)

        verify<JettyServer>(mockJetty).run(expectedRunPath, configuration)
    }

    @Test
    fun launchJettyWithCustomServerSourceDir(@TempDir output: Path) {
        val build = newFolder(output, "build/jbake")
        val configuration = mockJettyConfiguration(build, build)

        val args = arrayOf<String>(build.path, "-s")
        main.run(args)

        verify<JettyServer>(mockJetty).run(build.path, configuration)
    }


    // ATTENTION
    // There ist no extra argument for -s option. you can call jbake -s /customsource or jbake /customsource -s
    @Test
    fun launchJettyWithCustomDestinationDir(@TempDir source: Path) {
        val src = newFolder(source, "src/jbake")
        val configuration = mockJettyConfiguration(src, src)

        val args = arrayOf("-s", src.path)
        main.run(args)

        verify<JettyServer>(mockJetty).run(src.path, configuration)
    }

    @Test
    fun launchJettyWithCustomSrcAndDestDir(@TempDir source: Path, @TempDir output: Path) {
        val src = newFolder(source, "src/jbake")
        val exampleOutput = output.resolve("build/jbake").toFile()
        val configuration = mockJettyConfiguration(src, exampleOutput)

        val args = arrayOf<String>(src.path, exampleOutput.path, "-s")
        main.run(args)

        verify<JettyServer>(mockJetty).run(exampleOutput.path, configuration)
    }

    @Test
    fun launchJettyWithCustomDestViaConfig(@TempDir output: Path) {
        val args = arrayOf("-s")
        val exampleOutput = output.resolve("build/jbake").toFile()
        val configuration = stubConfig()
        configuration.destinationFolder = exampleOutput

        main.run(stubOptions(args), configuration)

        verify<JettyServer>(mockJetty).run(exampleOutput.path, configuration)
    }

    @Test
    fun launchJettyWithCmdlineOverridingProperties(@TempDir source: Path, @TempDir output: Path, @TempDir target: Path) {
        val src = newFolder(source, "src/jbake")
        val expectedOutput = newFolder(output, "build/jbake")
        val configTarget = newFolder(target, "target/jbake")

        val args = arrayOf("-s", src.path, expectedOutput.path)
        val configuration = stubConfig()
        configuration.destinationFolder = configTarget
        main.run(stubOptions(args), configuration)

        verify<JettyServer>(mockJetty).run(expectedOutput.path, configuration)
    }

    @Test
    fun shouldTellUserThatTemplateOptionRequiresInitOption() {
        val args = arrayOf("-t", "groovy-mte")

        val exception = assertThrows(JBakeException::class.java) { main.run(args) }

        assertThat(exception.getExit()).isEqualTo(SystemExit.CONFIGURATION_ERROR.status)
        verify(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent.capture())

        val loggingEvent = captorLoggingEvent.getValue()
        assertThat(loggingEvent.message).isEqualTo("Error: Missing required argument(s): --init")
    }

    @Test
    @Throws(ConfigurationException::class)
    fun shouldThrowJBakeExceptionWithSystemExitCodeOnUnexpectedError(@TempDir source: Path) {
        val other = Mockito.spy<Main>(main)
        val currentWorkingDir = newFolder(source, "jbake")
        mockDefaultJbakeConfiguration(currentWorkingDir)

        doThrow(RuntimeException("something went wrong"))
            .`when`(other).run(any(LaunchOptions::class.java), any(JBakeConfiguration::class.java))

        val e = assertThrows(JBakeException::class.java) { other.run(arrayOf("")) }

        assertThat(e.message).isEqualTo("An unexpected error occurred: something went wrong")
        assertThat(e.getExit()).isEqualTo(SystemExit.ERROR.status)
    }

    @Test
    fun shouldThrowAJBakeExceptionWithConfigurationErrorIfLoadThrowsAnCompositeException() {
        `when`(mockFactory.setEncoding(anyString())).thenReturn(mockFactory)
        doThrow(JBakeException(SystemExit.CONFIGURATION_ERROR, "something went wrong"))
            .`when`(mockFactory).createDefaultJbakeConfiguration(any(FCJ), any(FCJ), any(FCJ), anyBoolean())
        val e = assertThrows(JBakeException::class.java) { main.run(arrayOf("-b")) }
        assertThat(e.getExit()).isEqualTo(SystemExit.CONFIGURATION_ERROR.status)
    }

    @Test
    @Throws(ConfigurationException::class)
    fun shouldListCurrentSettings(@TempDir source: Path) {
        val src = newFolder(source, "src/jbake")
        mockDefaultJbakeConfiguration(src)

        val args = arrayOf("-ls")
        main.run(args)

        assertThat(outputStreamCaptor.toString()).contains("DEFAULT - Settings")
    }

    private fun stubOptions(args: Array<String>): LaunchOptions {
        return CommandLine.populateCommand(LaunchOptions(), *args)
    }

    @Throws(ConfigurationException::class)
    private fun stubConfig(): DefaultJBakeConfiguration {
        val sourceFolder = TestUtils.testResourcesAsSourceFolder
        val configuration = ConfigUtil().loadConfig(sourceFolder) as DefaultJBakeConfiguration
        configuration.setServerPort(8820)
        return configuration
    }

    @Throws(ConfigurationException::class)
    private fun mockDefaultJbakeConfiguration(sourceFolder: File) {
        val configuration = JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, null, null, false)
        System.setProperty("user.dir", sourceFolder.path)
        `when`(mockFactory.setEncoding(anyString())).thenReturn(mockFactory)
        `when`(
            mockFactory.createDefaultJbakeConfiguration(any(FCJ), any(FCJ), any(FCJ), anyBoolean())
        ).thenReturn(configuration)
    }

    @Throws(ConfigurationException::class)
    private fun mockJettyConfiguration(sourceFolder: File, destinationFolder: File): JBakeConfiguration {
        val configuration =
            JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, destinationFolder, null, false)
        System.setProperty("user.dir", sourceFolder.path)

        `when`(
            mockFactory.createJettyJbakeConfiguration(any(FCJ), any(FCJ), any(FCJ), anyBoolean())
        ).thenReturn(configuration)

        `when`(mockFactory.setEncoding(anyString())).thenReturn(mockFactory)
        return configuration
    }

    private fun newFolder(path: Path, name: String): File {
        val sourceFolder = path.resolve(name).toFile()
        sourceFolder.mkdirs()
        return sourceFolder
    }
}

private val FCJ = File::class.java
