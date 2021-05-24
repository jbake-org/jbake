package org.jbake.launcher;

import org.jbake.app.configuration.ConfigUtil;
import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LaunchOptionsTest {

    @Test
    public void showHelp() {
        String[] args = {"-h"};
        LaunchOptions res = parseArgs(args);
        assertThat(res.isHelpNeeded()).isTrue();
    }

    @Test
    public void runServer() {
        String[] args = {"-s"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isRunServer()).isTrue();
    }

    @Test
    public void runServerWithFolder() {
        String[] args = {"-s", "/tmp"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isRunServer()).isTrue();
        assertThat(res.getSource()).isEqualTo(new File("/tmp"));
    }

    @Test
    public void init() {
        String[] args = {"-i"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isInit()).isTrue();
        assertThat(res.getTemplate()).isEqualTo("freemarker");
    }

    @Test
    public void initWithTemplate() {
        String[] args = {"-i", "-t", "foo"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isInit()).isTrue();
        assertThat(res.getTemplate()).isEqualTo("foo");
    }

    @Test
    public void initWithSourceDirectory() {
        String[] args = {"-i", "/tmp"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isInit()).isTrue();
        assertThat(res.getSourceValue()).isEqualTo("/tmp");
    }

    @Test
    public void initWithTemplateAndSourceDirectory() {
        String[] args = {"-i", "-t", "foo", "/tmp"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isInit()).isTrue();
        assertThat(res.getTemplate()).isEqualTo("foo");
        assertThat(res.getSourceValue()).isEqualTo("/tmp");
    }

    @Test
    public void shouldThrowAnExceptionCallingTemplateWithoutInitOption() {
        String[] args = {"-t", "groovy-mte"};

        assertThatExceptionOfType(MissingParameterException.class).isThrownBy(()-> {
            LaunchOptions res = parseArgs(args);
        }).withMessage("Error: Missing required argument(s): --init");
    }

    @Test
    public void bake() {
        String[] args = {"-b"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isBake()).isTrue();
    }

    @Test
    public void listConfig() throws Exception {
        String[] args = {"-ls"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isListConfig()).isTrue();
    }

    @Test
    public void listConfigLongOption() throws Exception {
        String[] args = {"--list-settings"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isListConfig()).isTrue();
    }

    @Test
    public void customPropertiesEncoding() throws Exception {
        String[] args = {"--prop-encoding", "utf-16"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.getPropertiesEncoding()).isEqualTo("utf-16");
    }

    @Test
    public void defaultEncodingIsUtf8() throws Exception {
        String[] args = {};
        LaunchOptions res = parseArgs(args);

        assertThat(res.getPropertiesEncoding()).isEqualTo("utf-8");
    }

    @Test
    public void bakeNoArgs() {
        String[] args = {};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isHelpNeeded()).isTrue();
        assertThat(res.isRunServer()).isFalse();
        assertThat(res.isInit()).isFalse();
        assertThat(res.isBake()).isFalse();
        assertThat(res.getSource().getPath()).isEqualTo(System.getProperty("user.dir"));
        assertThat(res.getDestination().getPath()).isEqualTo(System.getProperty("user.dir") + File.separator + "output");
        assertThat(res.getConfig().getPath()).isEqualTo(System.getProperty("user.dir") + File.separator + ConfigUtil.CONFIG_FILE);
    }

    @Test
    public void bakeWithArgs() {
        String[] args = {"/tmp/source", "/tmp/destination"};
        LaunchOptions res = parseArgs(args);

        assertThat(res.isHelpNeeded()).isFalse();
        assertThat(res.isRunServer()).isFalse();
        assertThat(res.isInit()).isFalse();
        assertThat(res.isBake()).isTrue();
        assertThat(res.getSource()).isEqualTo(new File("/tmp/source"));
        assertThat(res.getDestination()).isEqualTo(new File("/tmp/destination"));
    }

    @Test
    public void configArg() {
        String[] args = {"-c", "foo"};
        LaunchOptions res = parseArgs(args);
        assertThat(res.getConfig().getAbsoluteFile().toString()).isEqualTo(System.getProperty("user.dir")+ File.separator + "foo");

    }

    private LaunchOptions parseArgs(String[] args) {
        return CommandLine.populateCommand(new LaunchOptions(), args);
    }
}
