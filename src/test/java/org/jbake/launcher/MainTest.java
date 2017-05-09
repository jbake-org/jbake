package org.jbake.launcher;

import org.apache.commons.configuration.CompositeConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Iterator;
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
        String[] args = {"-b", "-s", "src/jbake"};
        main.run(args);

        verify(mockJetty).run("src/jbake","8820");
    }

    @Test
    public void launchJettyWithCustomSourceDir() {
        String[] args = {"src/jbake", "-s"};
        main.run(args);

        verify(mockJetty).run("src/jbake","8820");
    }
    
    @Test
    public void launchJettyWithCustomServerSourceDir() {
        String[] args = {"-s", "build/jbake"};
        main.run(args);

        verify(mockJetty).run("build/jbake","8820");
    }

    // Documentation states these two commands will define the custom output, but the LaunchOptions file isn't setup for that.
    // I have written this test to define the existing functionality of the code and not that defined in docs.
    @Test
    public void launchJettyWithCustomDestinationDir() {
        String[] args = {"-s", "build/jbake"};
        main.run(args);

        verify(mockJetty).run("build/jbake","8820");
    }

    @Test
    public void launchJettyWithCustomSrcAndDestDir() {
        String[] args = {"jbake", "build/jbake", "-s"};
        main.run(args);

        verify(mockJetty).run("build/jbake","8820");
    }

    @Test
    public void launchJettyWithCustomDestViaConfig() throws CmdLineException {
        String[] args = {"-s"};
        Map properties = new HashMap(){{
            put("destination.folder", "build/jbake");
        }};
        main.run(stubOptions(args), stubConfig(properties));

        verify(mockJetty).run("build/jbake","8820");
    }

    @Test
    public void launchJettyWithCmdlineOverridingProperties() throws CmdLineException {
        String[] args = {"src/jbake", "build/jbake", "-s"};
        Map<String,String> properties = new HashMap<String,String>(){{
           put("destination.folder", "target/jbake");
        }};
        main.run(stubOptions(args), stubConfig(properties));

        verify(mockJetty).run("build/jbake","8820");
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
