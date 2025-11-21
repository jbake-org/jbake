package org.jbake.launcher

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import org.apache.commons.configuration2.ex.ConfigurationException
import org.assertj.core.api.Assertions
import org.itsallcode.junit.sysextensions.AssertExit
import org.itsallcode.junit.sysextensions.ExitGuard
import org.jbake.TestUtils
import org.jbake.app.JBakeException
import org.jbake.app.LoggingTest
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.junit.Assert
import org.junit.function.ThrowingRunnable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Path

@ExtendWith(ExitGuard::class)
internal class MainTest : LoggingTest() {
    private val standardOut: PrintStream? = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()
    private var main: Main? = null

    @Mock
    private val mockBaker: Baker? = null

    @Mock
    private val mockJetty: JettyServer? = null

    @Mock
    private val mockWatcher: BakeWatcher? = null

    @Mock
    private val configUtil: ConfigUtil? = null

    @Mock
    private val factory: JBakeConfigurationFactory? = null

    private var workingdir: String? = null

    @BeforeEach
    fun setUp() {
        this.main = Main(mockBaker!!, mockJetty!!, mockWatcher!!)
        workingdir = System.getProperty("user.dir")
        factory!!.configUtil = configUtil!!
        main!!.jBakeConfigurationFactory = factory
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
        main!!.run(args)

        Mockito.verify<JettyServer?>(mockJetty).run(expectedOutput.getPath(), configuration)
    }

    @Test
    fun launchBakeWithCustomPropertiesEncoding(@TempDir source: Path) {
        val currentWorkingdir = newFolder(source, "jbake")
        mockDefaultJbakeConfiguration(currentWorkingdir)

        val args = arrayOf("-b", "--prop-encoding", "latin1")
        main!!.run(args)

        Mockito.verify<JBakeConfigurationFactory?>(factory).setEncoding("latin1")
    }

    @Test
    fun launchBakeWithDefaultUtf8PropertiesEncoding(@TempDir source: Path) {
        val currentWorkingdir = newFolder(source, "jbake")
        mockDefaultJbakeConfiguration(currentWorkingdir)

        val args = arrayOf("-b")
        main!!.run(args)

        Mockito.verify<JBakeConfigurationFactory?>(factory).setEncoding("utf-8")
    }

    @Test
    fun launchBakeAndJetty(@TempDir source: Path) {
        val sourceFolder = newFolder(source, "src/jbake")
        val expectedOutput = newFolder(sourceFolder.toPath(), "output")
        val configuration = mockJettyConfiguration(sourceFolder, expectedOutput)

        val args = arrayOf("-b", "-s")
        main!!.run(args)

        Mockito.verify<JettyServer?>(mockJetty).run(expectedOutput.getPath(), configuration)
    }

    @Test
    @Throws(ConfigurationException::class)
    fun launchBakeAndJettyWithCustomDirForJetty(@TempDir source: Path) {
        val sourceFolder = newFolder(source, "src/jbake")
        val expectedRunPath = "src" + File.separator + "jbake" + File.separator + "output"
        val output = newFolder(source, expectedRunPath)
        val configuration = mockJettyConfiguration(sourceFolder, output)

        val args = arrayOf("-b", "-s", "src/jbake")
        main!!.run(args)

        Mockito.verify<JettyServer?>(mockJetty).run(expectedRunPath, configuration)
    }

    @Test
    fun launchJettyWithCustomServerSourceDir(@TempDir output: Path) {
        val build = newFolder(output, "build/jbake")
        val configuration = mockJettyConfiguration(build, build)

        val args = arrayOf<String>(build.getPath(), "-s")
        main!!.run(args)

        Mockito.verify<JettyServer?>(mockJetty).run(build.getPath(), configuration)
    }


    // ATTENTION
    // There ist no extra argument for -s option. you can call jbake -s /customsource or jbake /customsource -s
    @Test
    fun launchJettyWithCustomDestinationDir(@TempDir source: Path) {
        val src = newFolder(source, "src/jbake")
        val configuration = mockJettyConfiguration(src, src)

        val args = arrayOf("-s", src.getPath())
        main!!.run(args)

        Mockito.verify<JettyServer?>(mockJetty).run(src.getPath(), configuration)
    }

    @Test
    fun launchJettyWithCustomSrcAndDestDir(@TempDir source: Path, @TempDir output: Path) {
        val src = newFolder(source, "src/jbake")
        val exampleOutput = output.resolve("build/jbake").toFile()
        val configuration = mockJettyConfiguration(src, exampleOutput)

        val args = arrayOf<String>(src.getPath(), exampleOutput.getPath(), "-s")
        main!!.run(args)

        Mockito.verify<JettyServer?>(mockJetty).run(exampleOutput.getPath(), configuration)
    }

    @Test
    fun launchJettyWithCustomDestViaConfig(@TempDir output: Path) {
        val args = arrayOf("-s")
        val exampleOutput = output.resolve("build/jbake").toFile()
        val configuration = stubConfig()
        configuration.destinationFolder = exampleOutput

        main!!.run(stubOptions(args), configuration)

        Mockito.verify<JettyServer?>(mockJetty).run(exampleOutput.getPath(), configuration)
    }

    @Test
    fun launchJettyWithCmdlineOverridingProperties(
        @TempDir source: Path,
        @TempDir output: Path,
        @TempDir target: Path
    ) {
        val src = newFolder(source, "src/jbake")
        val expectedOutput = newFolder(output, "build/jbake")
        val configTarget = newFolder(target, "target/jbake")

        val args = arrayOf("-s", src.getPath(), expectedOutput.getPath())
        val configuration = stubConfig()
        configuration.destinationFolder = configTarget
        main!!.run(stubOptions(args), configuration)

        Mockito.verify<JettyServer?>(mockJetty).run(expectedOutput.getPath(), configuration)
    }

    @Test
    fun shouldTellUserThatTemplateOptionRequiresInitOption() {
        val args = arrayOf("-t", "groovy-mte")

        AssertExit.assertExitWithStatus(SystemExit.CONFIGURATION_ERROR.status, Runnable { Main.main(args) })

        Mockito.verify<Appender<ILoggingEvent?>?>(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent!!.capture())

        val loggingEvent = captorLoggingEvent!!.getValue()
        Assertions.assertThat(loggingEvent.getMessage()).isEqualTo("Error: Missing required argument(s): --init")
    }

    @Test
    @Throws(ConfigurationException::class)
    fun shouldThrowJBakeExceptionWithSystemExitCodeOnUnexpectedError(@TempDir source: Path) {
        val other = Mockito.spy<Main>(main)
        val currentWorkingdir = newFolder(source, "jbake")
        mockDefaultJbakeConfiguration(currentWorkingdir)

        Mockito.doThrow(RuntimeException("something went wrong")).`when`<Main?>(other).run(
            ArgumentMatchers.any<LaunchOptions?>(LaunchOptions::class.java),
            ArgumentMatchers.any<JBakeConfiguration>()
        )

        val e = Assert.assertThrows<JBakeException>(
            JBakeException::class.java,
            ThrowingRunnable { other.run(arrayOf("")) })

        Assertions.assertThat(e.message).isEqualTo("An unexpected error occurred: something went wrong")
        Assertions.assertThat(e.getExit()).isEqualTo(SystemExit.ERROR.status)
    }

    @Test
    fun shouldThrowAJBakeExceptionWithConfigurationErrorIfLoadThrowsAnCompositeException() {
        Mockito.`when`<JBakeConfigurationFactory>(factory!!.setEncoding(ArgumentMatchers.any<String>()))
            .thenReturn(factory)
        Mockito.doThrow(JBakeException(SystemExit.CONFIGURATION_ERROR, "something went wrong"))
            .`when`<JBakeConfigurationFactory?>(factory).createDefaultJbakeConfiguration(
                ArgumentMatchers.any(File::class.java),
                ArgumentMatchers.any(File::class.java),
                ArgumentMatchers.any(File::class.java),
                ArgumentMatchers.anyBoolean()
            )
        val e = Assert.assertThrows<JBakeException>(
            JBakeException::class.java,
            ThrowingRunnable { main!!.run(arrayOf("-b")) })
        Assertions.assertThat(e.getExit()).isEqualTo(SystemExit.CONFIGURATION_ERROR.status)
    }

    @Test
    @Throws(ConfigurationException::class)
    fun shouldListCurrentSettings(@TempDir source: Path) {
        val src = newFolder(source, "src/jbake")
        mockDefaultJbakeConfiguration(src)

        val args = arrayOf("-ls")
        main!!.run(args)

        Assertions.assertThat(outputStreamCaptor.toString()).contains("DEFAULT - Settings")
    }

    private fun stubOptions(args: Array<String>): LaunchOptions {
        return CommandLine.populateCommand<LaunchOptions>(LaunchOptions(), *args)
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
        System.setProperty("user.dir", sourceFolder.getPath())
        Mockito.`when`<JBakeConfigurationFactory>(factory!!.setEncoding(ArgumentMatchers.any<String>()))
            .thenReturn(factory)
        Mockito.`when`<DefaultJBakeConfiguration>(
            factory.createDefaultJbakeConfiguration(
                ArgumentMatchers.any(File::class.java),
                ArgumentMatchers.any(File::class.java),
                ArgumentMatchers.any(File::class.java),
                ArgumentMatchers.anyBoolean()
            )
        ).thenReturn(configuration)
    }

    @Throws(ConfigurationException::class)
    private fun mockJettyConfiguration(sourceFolder: File, destinationFolder: File): JBakeConfiguration {
        val configuration =
            JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, destinationFolder, null, false)
        System.setProperty("user.dir", sourceFolder.path)

        Mockito.`when`<DefaultJBakeConfiguration>(
            factory!!.createJettyJbakeConfiguration(
                ArgumentMatchers.any(File::class.java),
                ArgumentMatchers.any(File::class.java),
                ArgumentMatchers.any(File::class.java),
                ArgumentMatchers.anyBoolean()
            )
        ).thenReturn(configuration)

        Mockito.`when`<JBakeConfigurationFactory>(factory.setEncoding(ArgumentMatchers.any()))
            .thenReturn(factory)
        return configuration
    }

    private fun newFolder(path: Path, name: String): File {
        val sourceFolder = path.resolve(name).toFile()
        sourceFolder.mkdirs()
        return sourceFolder
    }
}
