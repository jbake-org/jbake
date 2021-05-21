package org.jbake.launcher;

import org.jbake.app.configuration.ConfigUtil;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(
        description = "JBake is a Java based, open source, static site/blog generator for developers & designers",
        name = "jbake",
        usageHelpAutoWidth = true
)
public class LaunchOptions {
    @Parameters(index = "0", description = "source folder of site content (with templates and assets), if not supplied will default to current directory", arity = "0..1")
    private String source;

    @Parameters(index = "1", description = "destination folder for output, if not supplied will default to a folder called \"output\" in the current directory", arity = "0..1")
    private String destination;

    @Option(names = {"-b", "--bake"}, description = "performs a bake")
    private boolean bake;

    @ArgGroup(exclusive = false, heading = "%n%nJBake initialization%n%n")
    private InitOptions initGroup;

    static class InitOptions {

        @Option(names = {"-i", "--init"}, paramLabel = "<target>", description = "initialises required folder structure with default templates (defaults to current directory if <source> is not supplied)", required = true)
        private boolean init;

        @Option(names = {"-t", "--template"}, defaultValue = "freemarker", fallbackValue = "freemarker", description = "use specified template engine for default templates (uses Freemarker if <template> is not supplied) ", arity = "0..1")
        private String template;
    }

    @Option(names = {"-s", "--server"}, description = "runs HTTP server to serve out baked site, if no <value> is supplied will default to a folder called \"output\" in the current directory")
    private boolean runServer;

    @Option(names = {"-c", "--config"}, description = "use specified file for configuration (defaults to " + ConfigUtil.CONFIG_FILE +" in the source folder if not supplied)")
    private String config;

    @Option(names = {"-h", "--help"}, description = "prints this message", usageHelp = true)
    private boolean helpRequested;

    @Option(names = {"--reset"}, description = "clears the local cache, enforcing rendering from scratch")
    private boolean clearCache;

    @Option(names = {"-ls", "--list-settings"}, description = "list configuration settings")
    private boolean listConfig;

    public String getTemplate() {
        return initGroup.template;
    }

    public File getSource() {
        if (source != null) {
            return new File(source);
        } else {
            return new File(System.getProperty("user.dir"));
        }
    }

    public String getSourceValue() {
        return source;
    }

    public File getDestination() {
        if (destination != null) {
            return new File(destination);
        } else {
            return new File(getSource(), "output");
        }
    }

    public String getDestinationValue() {
        return destination;
    }

    public File getConfig() {
        if (config != null) {
            return new File(config);
        } else {
            return new File(getSource(), "jbake.properties");
        }
    }

    public String getConfigValue() {
        return config;
    }

    public boolean isHelpNeeded() {
        return helpRequested || !(isListConfig() || isBake() || isRunServer() || isInit() || source != null || destination != null);
    }

    public boolean isRunServer() {
        return runServer;
    }

    public boolean isInit() {
        return (initGroup !=null && initGroup.init);
    }

    public boolean isClearCache() {
        return clearCache;
    }

    public boolean isBake() {
        return bake || (source != null && destination != null);
    }

    public boolean isListConfig() {
        return listConfig;
    }
}
