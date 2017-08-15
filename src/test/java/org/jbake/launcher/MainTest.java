package org.jbake.launcher;

import org.apache.commons.configuration.CompositeConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.class )
public class MainTest {

    private Main main;

    @Mock private Baker mockBaker;
    @Mock private JettyServer mockJetty;
    @Mock private BakeWatcher mockWatcher;

    @Before
    public void setup() {
        this.main = new Main(mockBaker, mockJetty, mockWatcher);
    }

    @Test
    public void launchJetty() {
        String[] args = {"-s"};
        main.run(args);

        verify(mockJetty).run("output","8820");
    }
    
    @Test
    public void launchBakeAndJetty() {
        String[] args = {"-b", "-s"};
        main.run(args);

        verify(mockJetty).run("output","8820");
    }
    
    @Test
    public void launchBakeAndJettyWithCustomDirForJetty() {
        String path = "src" + File.separator + "jbake";
        String[] args = {"-b", "-s", path};
        main.run(args);

        verify(mockJetty).run(path,"8820");
    }

    @Test
    public void launchJettyWithCustomSourceDir() {
        String path = "src" + File.separator + "jbake";
        String[] args = {path, "-s"};
        main.run(args);

        verify(mockJetty).run(path,"8820");
    }
    
    @Test
    public void launchJettyWithCustomServerSourceDir() {
        String path = "build" + File.separator + "jbake";
        String[] args = {"-s", path};
        main.run(args);

        verify(mockJetty).run(path,"8820");
    }

    // Documentation states these two commands will define the custom output, but the LaunchOptions file isn't setup for that.
    // I have written this test to define the existing functionality of the code and not that defined in docs.
    @Test
    public void launchJettyWithCustomDestinationDir() {
        String path = "build" + File.separator + "jbake";
        String[] args = {"-s", path};
        main.run(args);

        verify(mockJetty).run(path,"8820");
    }

    @Test
    public void launchJettyWithCustomSrcAndDestDir() {
        String path = "build" + File.separator + "jbake";
        String[] args = {"jbake", path, "-s"};
        main.run(args);

        verify(mockJetty).run(path,"8820");
    }

    @Test
    public void launchJettyWithCustomDestViaConfig() throws CmdLineException {
        final String path = "build" + File.separator + "jbake";
        String[] args = {"-s"};
        Map<String, String> properties = new HashMap<String, String>(){{
            put("destination.folder", path);
        }};
        main.run(stubOptions(args), stubConfig(properties));

        verify(mockJetty).run(path,"8820");
    }

    @Test
    public void launchJettyWithCmdlineOverridingProperties() throws CmdLineException {
        String buildPath = "build" + File.separator + "jbake";
        String sourcePath = "src" + File.separator + "jbake";
        final String targetPath = "target" + File.separator + "jbake";

        String[] args = {sourcePath, buildPath, "-s"};
        Map<String,String> properties = new HashMap<String,String>(){{
           put("destination.folder", targetPath);
        }};
        main.run(stubOptions(args), stubConfig(properties));

        verify(mockJetty).run(buildPath,"8820");
    }

    private LaunchOptions stubOptions(String[] args) throws CmdLineException {
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);
        return res;
    }

    private CompositeConfiguration stubConfig(Map<String, String> properties) {
        CompositeConfiguration config = new CompositeConfiguration();
        config.addProperty("server.port", "8820");
        for (Map.Entry<String,String> pair : properties.entrySet()) {
            config.addProperty(pair.getKey(), pair.getValue());
        }
        return config;
    }
}
