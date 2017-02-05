package org.jbake.launcher;

import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class MainTest {

    private Main main;

    @Mock private Baker mockBaker;
    @Mock private JettyServer mockJetty;
    @Mock private BakeWatcher mockWatcher;
    @Mock private ConfigUtil mockConfigUtil;

    @Rule
    public TemporaryFolder root = new TemporaryFolder();

    @Before
    public void setup() {
        this.main = new Main(mockBaker, mockJetty, mockWatcher);
        main.setConfigUtil(mockConfigUtil);
    }

    @Test
    public void launchJetty() throws ConfigurationException {
        File sourceFolder = mockValidSourceFolder();
        File expectedSourcePath = new File(sourceFolder,"output");

        String[] args = {"-s"};
        main.run(args);

        verify(mockJetty).run(expectedSourcePath.getPath(),"8820");
    }
    
    @Test
    public void launchBakeAndJetty() throws ConfigurationException {
        File sourceFolder = mockValidSourceFolder();
        File expectedSourceFolder = new File(sourceFolder, "output");

        String[] args = {"-b", "-s"};
        main.run(args);

        verify(mockJetty).run(expectedSourceFolder.getPath(),"8820");
    }
    
    @Test
    public void launchBakeAndJettyWithCustomDirForJetty() throws Exception {
        File path = mockValidSourceFolder();
        String[] args = {"-b", "-s", path.getPath()};
        main.run(args);

        verify(mockJetty).run(path.getPath(),"8820");
    }

    @Test
    public void launchJettyWithCustomSourceDir() throws ConfigurationException {
        File sourceFolder = mockValidSourceFolder();

        String[] args = {sourceFolder.getPath(), "-s"};
        main.run(args);

        verify(mockJetty).run(sourceFolder.getPath(),"8820");
    }

    // Documentation states these two commands will define the custom output, but the LaunchOptions file isn't setup for that.
    // I have written this test to define the existing functionality of the code and not that defined in docs.
    @Test
    public void launchJettyWithCustomDestinationDir() throws Exception {
        mockValidSourceFolder();

        File exampleOutput = root.newFolder("build", "jbake");
        String[] args = {"-s", exampleOutput.getPath()};
        main.run(args);

        verify(mockJetty).run(exampleOutput.getPath(),"8820");
    }

    @Test
    public void launchJettyWithCustomSrcAndDestDir() throws Exception {
        File sourceFolder = mockValidSourceFolder();

        final File exampleOutput = root.newFolder("build","jbake");

        String[] args = {sourceFolder.getPath(), exampleOutput.getPath(), "-s"};
        main.run(args);

        verify(mockJetty).run(exampleOutput.getPath(),"8820");
    }

    @Test
    public void launchJettyWithCustomDestViaConfig() throws Exception {
        String[] args = {"-s"};
        final File exampleOutput = root.newFolder("build","jbake");
        DefaultJBakeConfiguration configuration = stubConfig();
        configuration.setDestinationFolder(exampleOutput);

        main.run(stubOptions(args), configuration);

        verify(mockJetty).run(exampleOutput.getPath(),"8820");
    }

    @Test
    public void launchJettyWithCmdlineOverridingProperties() throws Exception {
        File sourceFolder = mockValidSourceFolder();
        final File expectedOutput = root.newFolder("build","jbake");
        final File configTarget = root.newFolder("target","jbake");

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


    private File mockValidSourceFolder() throws ConfigurationException {
        File sourceFolder = new File(this.getClass().getResource("/fixture").getFile());
        JBakeConfiguration configuration = new ConfigUtil().loadConfig( sourceFolder );

        when(mockConfigUtil.loadConfig(any(File.class))).thenReturn( configuration );

        return sourceFolder;
    }
}
