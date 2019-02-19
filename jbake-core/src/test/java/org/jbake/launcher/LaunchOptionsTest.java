package org.jbake.launcher;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;

import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LaunchOptionsTest {

    @Spy
    Main main;

    @Test
    public void showHelp() throws Exception {
        String[] args = {"-h"};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isHelpNeeded()).isTrue();
    }

    @Test
    public void runServer() throws Exception {
        String[] args = {"-s"};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isRunServer()).isTrue();
    }

    @Test
    public void runServerWithFolder() throws Exception {
        String path = "/tmp";
        String[] args = {"-s", path};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isRunServer()).isTrue();
        assertThat(res.getSource()).isEqualTo(new File(path));

        // ensures path supplied is actually used when running server
        JBakeConfiguration config = new JBakeConfigurationFactory().createJettyJbakeConfiguration(res.getSource(), res.getDestination(), res.isClearCache());
        // to stop server actually running when method is called
        doNothing().when(main).runServer(any(File.class), any(Integer.class));
        main.run(res, config);
        // verify method was called with correct path
        verify(main).runServer(new File(path), config.getServerPort());
    }

    @Test
    public void init() throws Exception {
        String[] args = {"-i"};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isInit()).isTrue();
        assertThat(res.getTemplate()).isEqualTo("freemarker");
    }

    @Test
    public void initWithTemplate() throws Exception {
        String[] args = {"-i", "-t", "foo"};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isInit()).isTrue();
        assertThat(res.getTemplate()).isEqualTo("foo");
    }

    @Test
    public void initWithSourceDirectory() throws Exception {
        String[] args = {"-i", "/tmp"};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isInit()).isTrue();
        assertThat(res.getSourceValue()).isEqualTo("/tmp");
    }

    @Test
    public void initWithTemplateAndSourceDirectory() throws Exception {
        String[] args = {"-i", "-t", "foo", "/tmp"};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isInit()).isTrue();
        assertThat(res.getTemplate()).isEqualTo("foo");
        assertThat(res.getSourceValue()).isEqualTo("/tmp");
    }

    @Test
    public void bake() throws Exception {
        String[] args = {"-b"};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isBake()).isTrue();
    }

    @Test
    public void bakeNoArgs() throws Exception {
        String[] args = {};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isHelpNeeded()).isTrue();
        assertThat(res.isRunServer()).isFalse();
        assertThat(res.isInit()).isFalse();
        assertThat(res.isBake()).isFalse();
        assertThat(res.getSource().getPath()).isEqualTo(System.getProperty("user.dir"));
        assertThat(res.getDestination().getPath()).isEqualTo(System.getProperty("user.dir") + File.separator + "output");
    }

    @Test
    public void bakeWithArgs() throws Exception {
        String[] args = {"/tmp/source", "/tmp/destination"};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        assertThat(res.isHelpNeeded()).isFalse();
        assertThat(res.isRunServer()).isFalse();
        assertThat(res.isInit()).isFalse();
        assertThat(res.isBake()).isTrue();
        assertThat(res.getSource()).isEqualTo(new File("/tmp/source"));
        assertThat(res.getDestination()).isEqualTo(new File("/tmp/destination"));
    }
}
