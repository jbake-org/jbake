package org.jbake.launcher;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.itsallcode.junit.sysextensions.ExitGuard;
import org.jbake.TestUtils;
import org.jbake.app.LoggingTest;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.jbake.exception.JBakeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.itsallcode.junit.sysextensions.AssertExit.assertExitWithStatus;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ExitGuard.class)
class MainTest extends LoggingTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private Main main;
    @Mock
    private Baker mockBaker;
    @Mock
    private JettyServer mockJetty;
    @Mock
    private BakeWatcher mockWatcher;
    @Mock
    private ConfigUtil configUtil;
    @Mock
    private JBakeConfigurationFactory factory;

    private String workingdir;

    @BeforeEach
    void setUp() {
        this.main = new Main(mockBaker, mockJetty, mockWatcher);
        workingdir = System.getProperty("user.dir");
        factory.setConfigUtil(configUtil);
        main.setJBakeConfigurationFactory(factory);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", workingdir);
        System.setOut(standardOut);
    }

    @Test
    void launchJetty(@TempDir Path source) throws Exception {
        File currentWorkingdir = newFolder(source, "src/jbake");
        File expectedOutput = new File(currentWorkingdir, "output");
        JBakeConfiguration configuration = mockJettyConfiguration(currentWorkingdir, expectedOutput);

        String[] args = {"-s"};
        main.run(args);

        verify(mockJetty).run(expectedOutput.getPath(), configuration);
    }

    @Test
    public void launchBakeWithCustomPropertiesEncoding(@TempDir Path source) throws Exception {
        File currentWorkingdir = newFolder(source, "jbake");
        mockDefaultJbakeConfiguration(currentWorkingdir);

        String[] args = {"-b", "--prop-encoding", "latin1"};
        main.run(args);

        verify(factory).setEncoding("latin1");
    }

    @Test
    public void launchBakeWithDefaultUtf8PropertiesEncoding(@TempDir Path source) throws Exception {
        File currentWorkingdir = newFolder(source, "jbake");
        mockDefaultJbakeConfiguration(currentWorkingdir);

        String[] args = {"-b"};
        main.run(args);

        verify(factory).setEncoding("utf-8");
    }

    @Test
    void launchBakeAndJetty(@TempDir Path source) throws Exception {
        File sourceFolder = newFolder(source, "src/jbake");
        File expectedOutput = newFolder(sourceFolder.toPath(), "output");
        JBakeConfiguration configuration = mockJettyConfiguration(sourceFolder, expectedOutput);

        String[] args = {"-b", "-s"};
        main.run(args);

        verify(mockJetty).run(expectedOutput.getPath(), configuration);
    }

    @Test
    void launchBakeAndJettyWithCustomDirForJetty(@TempDir Path source) throws ConfigurationException {
        File sourceFolder = newFolder(source, "src/jbake");
        String expectedRunPath = "src" + File.separator + "jbake" + File.separator + "output";
        File output = newFolder(source, expectedRunPath);
        JBakeConfiguration configuration = mockJettyConfiguration(sourceFolder, output);

        String[] args = {"-b", "-s", "src/jbake"};
        main.run(args);

        verify(mockJetty).run(expectedRunPath, configuration);
    }

    @Test
    void launchJettyWithCustomServerSourceDir(@TempDir Path output) throws Exception {
        File build = newFolder(output, "build/jbake");
        JBakeConfiguration configuration = mockJettyConfiguration(build, build);

        String[] args = {build.getPath(), "-s"};
        main.run(args);

        verify(mockJetty).run(build.getPath(), configuration);
    }


    // ATTENTION
    // There ist no extra argument for -s option. you can call jbake -s /customsource or jbake /customsource -s
    @Test
    void launchJettyWithCustomDestinationDir(@TempDir Path source) throws Exception {
        File src = newFolder(source, "src/jbake");
        JBakeConfiguration configuration = mockJettyConfiguration(src, src);

        String[] args = {"-s", src.getPath()};
        main.run(args);

        verify(mockJetty).run(src.getPath(), configuration);
    }

    @Test
    void launchJettyWithCustomSrcAndDestDir(@TempDir Path source, @TempDir Path output) throws Exception {
        File src = newFolder(source, "src/jbake");
        File exampleOutput = output.resolve("build/jbake").toFile();
        JBakeConfiguration configuration = mockJettyConfiguration(src, exampleOutput);

        String[] args = {src.getPath(), exampleOutput.getPath(), "-s"};
        main.run(args);

        verify(mockJetty).run(exampleOutput.getPath(), configuration);
    }

    @Test
    void launchJettyWithCustomDestViaConfig(@TempDir Path output) throws Exception {
        String[] args = {"-s"};
        final File exampleOutput = output.resolve("build/jbake").toFile();
        DefaultJBakeConfiguration configuration = stubConfig();
        configuration.setDestinationFolder(exampleOutput);

        main.run(stubOptions(args), configuration);

        verify(mockJetty).run(exampleOutput.getPath(), configuration);
    }

    @Test
    void launchJettyWithCmdlineOverridingProperties(@TempDir Path source, @TempDir Path output, @TempDir Path target) throws Exception {
        final File src = newFolder(source, "src/jbake");
        final File expectedOutput = newFolder(output, "build/jbake");
        final File configTarget = newFolder(target, "target/jbake");

        String[] args = {"-s", src.getPath(), expectedOutput.getPath()};
        DefaultJBakeConfiguration configuration = stubConfig();
        configuration.setDestinationFolder(configTarget);
        main.run(stubOptions(args), configuration);

        verify(mockJetty).run(expectedOutput.getPath(), configuration);
    }

    @Test
    void shouldTellUserThatTemplateOptionRequiresInitOption() {

        String[] args = {"-t", "groovy-mte"};

        assertExitWithStatus(SystemExit.CONFIGURATION_ERROR.getStatus(), ()->Main.main(args));

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());

        LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        assertThat(loggingEvent.getMessage()).isEqualTo("Error: Missing required argument(s): --init");
    }

    @Test
    void shouldThrowJBakeExceptionWithSystemExitCodeOnUnexpectedError(@TempDir Path source) throws ConfigurationException {

        Main other = spy(main);
        File currentWorkingdir = newFolder(source, "jbake");
        mockDefaultJbakeConfiguration(currentWorkingdir);

        doThrow(new RuntimeException("something went wrong")).when(other).run(any(LaunchOptions.class), any());

        JBakeException e = assertThrows(JBakeException.class, () -> other.run(new String[]{""}));

        assertThat(e.getMessage()).isEqualTo("An unexpected error occurred: something went wrong");
        assertThat(e.getExit()).isEqualTo(SystemExit.ERROR.getStatus());
    }

    @Test
    void shouldThrowAJBakeExceptionWithConfigurationErrorIfLoadThrowsAnCompositeException() {
        when(factory.setEncoding(any())).thenReturn(factory);
        doThrow(new JBakeException(SystemExit.CONFIGURATION_ERROR, "something went wrong")).when(factory).createDefaultJbakeConfiguration(any(File.class), any(File.class), any(File.class), anyBoolean());
        JBakeException e = assertThrows(JBakeException.class, () -> main.run(new String[]{"-b"}));
        assertThat(e.getExit()).isEqualTo(SystemExit.CONFIGURATION_ERROR.getStatus());
    }

    @Test
    void shouldListCurrentSettings(@TempDir Path source) throws ConfigurationException {
        File src = newFolder(source, "src/jbake");
        mockDefaultJbakeConfiguration(src);

        String[] args = {"-ls"};
        main.run(args);

        assertThat(outputStreamCaptor.toString()).contains("DEFAULT - Settings");
    }

    private LaunchOptions stubOptions(String[] args) {
        return CommandLine.populateCommand(new LaunchOptions(), args);
    }

    private DefaultJBakeConfiguration stubConfig() throws ConfigurationException {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration configuration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourceFolder);
        configuration.setServerPort(8820);
        return configuration;
    }

    private void mockDefaultJbakeConfiguration(File sourceFolder) throws ConfigurationException {
        DefaultJBakeConfiguration configuration = new JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, null, null, false);
        System.setProperty("user.dir", sourceFolder.getPath());
        when(factory.setEncoding(any())).thenReturn(factory);
        when(factory.createDefaultJbakeConfiguration(any(File.class), any(File.class), any(File.class), anyBoolean())).thenReturn(configuration);
    }

    private JBakeConfiguration mockJettyConfiguration(File sourceFolder, File destinationFolder) throws ConfigurationException {
        DefaultJBakeConfiguration configuration = new JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, destinationFolder, null, false);
        System.setProperty("user.dir", sourceFolder.getPath());
        when(factory.createJettyJbakeConfiguration(any(File.class), any(File.class),  any(File.class), anyBoolean())).thenReturn(configuration);
        when(factory.setEncoding(any())).thenReturn(factory);
        return configuration;
    }

    private File newFolder(Path path, String name) {
        File sourceFolder = path.resolve(name).toFile();
        sourceFolder.mkdirs();
        return sourceFolder;
    }
}
