package org.jbake.launcher;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.apache.commons.configuration.ConfigurationException;
import org.itsallcode.junit.sysextensions.ExitGuard;
import org.jbake.TestUtils;
import org.jbake.app.LoggingTest;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.itsallcode.junit.sysextensions.AssertExit.assertExitWithStatus;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ExitGuard.class)
public class MainTest extends LoggingTest {

    private Main main;

    @Mock private Baker mockBaker;
    @Mock private JettyServer mockJetty;
    @Mock private BakeWatcher mockWatcher;
    @Mock private ConfigUtil configUtil;
    @Mock private JBakeConfigurationFactory factory;

    private String workingdir;

    @BeforeEach
    public void setUp() {
        this.main = new Main(mockBaker, mockJetty, mockWatcher);
        workingdir = System.getProperty("user.dir");
        factory.setConfigUtil(configUtil);
        main.setJBakeConfigurationFactory(factory);
    }

    @AfterEach
    public void tearDown() {
        System.setProperty("user.dir", workingdir);
    }

    @Test
    public void launchJetty(@TempDir Path source) throws Exception {

        File currentWorkingdir = newFolder(source,"src/jbake");
        File expectedOutput = new File(currentWorkingdir,"output");
        JBakeConfiguration configuration = mockJettyConfiguration(currentWorkingdir,expectedOutput);

        String[] args = {"-s"};
        main.run(args);

        verify(mockJetty).run(expectedOutput.getPath(),configuration);
    }

    @Test
    public void launchBakeAndJetty(@TempDir Path source) throws Exception {
        File sourceFolder = newFolder(source, "src/jbake");
        File expectedOutput = newFolder(sourceFolder.toPath(), "output");
        JBakeConfiguration configuration = mockJettyConfiguration(sourceFolder, expectedOutput);

        String[] args = {"-b", "-s"};
        main.run(args);

        verify(mockJetty).run(expectedOutput.getPath(),configuration);
    }


    @Test
    public void launchBakeAndJettyWithCustomDirForJetty(@TempDir Path source) throws ConfigurationException, IOException {
        File sourceFolder = newFolder(source,"src/jbake");
        String expectedRunPath = "src" + File.separator + "jbake" + File.separator + "output";
        File output = newFolder(source,expectedRunPath);
        JBakeConfiguration configuration = mockJettyConfiguration(sourceFolder,output);

        String[] args = {"-b", "-s", "src/jbake"};
        main.run(args);

        verify(mockJetty).run(expectedRunPath, configuration);
    }

    @Test
    public void launchJettyWithCustomServerSourceDir(@TempDir Path output) throws Exception {
        File build = newFolder(output,"build/jbake");
        JBakeConfiguration configuration = mockJettyConfiguration(build, build);

        String[] args = {build.getPath(), "-s"};
        main.run(args);

        verify(mockJetty).run(build.getPath(), configuration);
    }


    // ATTENTION
    // There ist no extra argument for -s option. you can call jbake -s /customsource or jbake /customsource -s
    @Test
    public void launchJettyWithCustomDestinationDir(@TempDir Path source) throws Exception {
        File src = newFolder(source, "src/jbake");
        JBakeConfiguration configuration = mockJettyConfiguration(src,src);

        String[] args = {"-s", src.getPath()};
        main.run(args);

        verify(mockJetty).run(src.getPath(), configuration);
    }

    @Test
    public void launchJettyWithCustomSrcAndDestDir(@TempDir Path source, @TempDir Path output) throws Exception {
        File src = newFolder(source, "src/jbake");
        File exampleOutput = output.resolve("build/jbake").toFile();
        JBakeConfiguration configuration = mockJettyConfiguration(src,exampleOutput);

        String[] args = {src.getPath(), exampleOutput.getPath(), "-s"};
        main.run(args);

        verify(mockJetty).run(exampleOutput.getPath(), configuration);
    }

    @Test
    public void launchJettyWithCustomDestViaConfig(@TempDir Path output) throws Exception {
        String[] args = {"-s"};
        final File exampleOutput = output.resolve("build/jbake").toFile();
        DefaultJBakeConfiguration configuration = stubConfig();
        configuration.setDestinationFolder(exampleOutput);

        main.run(stubOptions(args), configuration);

        verify(mockJetty).run(exampleOutput.getPath(), configuration);
    }

    @Test
    public void launchJettyWithCmdlineOverridingProperties(@TempDir Path source, @TempDir Path output, @TempDir Path target) throws Exception {
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
    public void shouldTellUserThatTemplateOptionRequiresInitOption() throws Exception {

        String[] args = {"-t", "groovy-mte"};

        assertExitWithStatus(1, ()->Main.main(args));

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());

        LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        assertThat(loggingEvent.getMessage()).isEqualTo("Invalid commandline arguments: option \"-t (--template)\" requires the option(s) [-i]");
    }

    private LaunchOptions stubOptions(String[] args) throws CmdLineException {
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);
        return res;
    }

    private DefaultJBakeConfiguration stubConfig() throws ConfigurationException {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration configuration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourceFolder);
        configuration.setServerPort(8820);
        return configuration;
    }

    private void mockDefaultJbakeConfiguration(File sourceFolder) throws ConfigurationException {
        DefaultJBakeConfiguration configuration = new JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, null, false);
        System.setProperty("user.dir", sourceFolder.getPath());

        when(factory.createJettyJbakeConfiguration(any(File.class), any(File.class), anyBoolean())).thenReturn(configuration);
    }

    private JBakeConfiguration mockJettyConfiguration(File sourceFolder, File destinationFolder) throws ConfigurationException {
        DefaultJBakeConfiguration configuration = new JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, destinationFolder, false);
        System.setProperty("user.dir", sourceFolder.getPath());

        when(factory.createJettyJbakeConfiguration(any(File.class), any(File.class), anyBoolean())).thenReturn(configuration);
        return configuration;
    }

    private File newFolder(Path path, String name) {
        File sourceFolder = path.resolve(name).toFile();
        sourceFolder.mkdirs();
        return sourceFolder;
    }
}
