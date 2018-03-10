package org.jbake.launcher;

import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class MainTest {

    private Main main;

    @Rule public TemporaryFolder folder = new TemporaryFolder();
    @Mock private Baker mockBaker;
    @Mock private JettyServer mockJetty;
    @Mock private BakeWatcher mockWatcher;
    @Mock private ConfigUtil configUtil;
    @Mock private JBakeConfigurationFactory factory;

    private String workingdir;

    @Before
    public void setUp() {
        this.main = new Main(mockBaker, mockJetty, mockWatcher);
        workingdir = System.getProperty("user.dir");
        factory.setConfigUtil(configUtil);
        main.setJBakeConfigurationFactory(factory);
    }

    @After
    public void tearDown() {
        System.setProperty("user.dir", workingdir);
    }

    @Test
    public void launchJetty() throws Exception {
        File currentWorkingdir = folder.newFolder("src", "jbake");
        File expectedOutput = new File(currentWorkingdir,"output");
        mockJettyConfiguration(currentWorkingdir,expectedOutput);

        String[] args = {"-s"};
        main.run(args);

        verify(mockJetty).run(expectedOutput.getPath(),"8820");
    }
    
    @Test
    public void launchBakeAndJetty() throws Exception {
        File sourceFolder = folder.newFolder("src", "jbake");
        File expectedOutput = new File(sourceFolder, "output");
        mockJettyConfiguration(sourceFolder, expectedOutput);

        String[] args = {"-b", "-s"};
        main.run(args);

        verify(mockJetty).run(expectedOutput.getPath(),"8820");
    }
    
    @Test
    public void launchBakeAndJettyWithCustomDirForJetty() throws ConfigurationException, IOException {
        mockValidSourceFolder("src/jbake", true);
        String expectedRunPath = "src" + File.separator + "jbake" + File.separator + "output";

        String[] args = {"-b", "-s", "src/jbake"};
        main.run(args);

        verify(mockJetty).run(expectedRunPath,"8820");
    }

    @Test
    public void launchJettyWithCustomServerSourceDir() throws Exception {
        File sourceFolder = mockValidSourceFolder("build/jbake", true);

        String[] args = {sourceFolder.getPath(), "-s"};
        main.run(args);

        verify(mockJetty).run(sourceFolder.getPath(),"8820");
    }

    // ATTENTION
    // There ist no extra argument for -s option. you can call jbake -s /customsource or jbake /customsource -s
    @Test
    public void launchJettyWithCustomDestinationDir() throws Exception {
        File sourceFolder = mockValidSourceFolder("src/jbake", true);

        String[] args = {"-s", sourceFolder.getPath()};
        main.run(args);

        verify(mockJetty).run(sourceFolder.getPath(),"8820");
    }

    @Test
    public void launchJettyWithCustomSrcAndDestDir() throws Exception {
        File sourceFolder = mockValidSourceFolder("src/jbake", true);

        final File exampleOutput = folder.newFolder("build","jbake");

        String[] args = {sourceFolder.getPath(), exampleOutput.getPath(), "-s"};
        main.run(args);

        verify(mockJetty).run(exampleOutput.getPath(),"8820");
    }

    @Test
    public void launchJettyWithCustomDestViaConfig() throws Exception {
        String[] args = {"-s"};
        final File exampleOutput = folder.newFolder("build","jbake");
        DefaultJBakeConfiguration configuration = stubConfig();
        configuration.setDestinationFolder(exampleOutput);

        main.run(stubOptions(args), configuration);

        verify(mockJetty).run(exampleOutput.getPath(),"8820");
    }

    @Test
    public void launchJettyWithCmdlineOverridingProperties() throws Exception {
        File sourceFolder = mockValidSourceFolder("src/jbake", true);
        final File expectedOutput = folder.newFolder("build","jbake");
        final File configTarget = folder.newFolder("target","jbake");

        String[] args = {sourceFolder.getPath(), expectedOutput.getPath(), "-s"};
        DefaultJBakeConfiguration configuration = stubConfig();
        configuration.setDestinationFolder(configTarget);
        main.run(stubOptions(args), configuration);

        verify(mockJetty).run(expectedOutput.getPath(),"8820");
    }

    private LaunchOptions stubOptions(String[] args) throws CmdLineException {
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);
        return res;
    }

    private DefaultJBakeConfiguration stubConfig() throws ConfigurationException {
        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        DefaultJBakeConfiguration configuration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig( sourceFolder );
        configuration.setServerPort(8820);
        return configuration;
    }

    private File mockValidSourceFolder(String sourcePath, boolean withJetty) throws IOException, ConfigurationException {
        File mockedSourceFolder = folder.newFolder(sourcePath.split("/"));
        if ( withJetty ) {
            mockJettyConfiguration(mockedSourceFolder, mockedSourceFolder);
        }
        else {
            mockDefaultJbakeConfiguration(mockedSourceFolder);
        }
        return mockedSourceFolder;
    }

    private void mockDefaultJbakeConfiguration(File sourceFolder) throws ConfigurationException {
        DefaultJBakeConfiguration configuration = new JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder,null,false);
        System.setProperty("user.dir", sourceFolder.getPath());

        when(factory.createJettyJbakeConfiguration(any(File.class),any(File.class),anyBoolean())).thenReturn( configuration );
    }

    private void mockJettyConfiguration(File sourceFolder, File destinationFolder) throws ConfigurationException {
        DefaultJBakeConfiguration configuration = new JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder,destinationFolder,false);
        System.setProperty("user.dir", sourceFolder.getPath());

        when(factory.createJettyJbakeConfiguration(any(File.class),any(File.class),anyBoolean())).thenReturn( configuration );
    }
}
