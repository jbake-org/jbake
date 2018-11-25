package org.jbake.launcher;

import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.FileUtil;
import org.jbake.app.JBakeException;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.jbake.util.ConfigurationPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

import java.io.File;

/**
 * Launcher for JBake.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Main {

    private final String USAGE_PREFIX = "Usage: jbake";
    private final String ALT_USAGE_PREFIX = "   or  jbake";
    private final Baker baker;
    private final JettyServer jettyServer;
    private final BakeWatcher watcher;
    private JBakeConfigurationFactory configurationFactory;
    private static final Logger logger = LoggerFactory.getLogger("jbake");

    /**
     * Default constructor.
     */
    public Main() {
        this(new Baker(), new JettyServer(), new BakeWatcher());
    }

    /**
     * Optional constructor to externalize dependencies.
     *
     * @param baker   A {@link Baker} instance
     * @param jetty   A {@link JettyServer} instance
     * @param watcher A {@link BakeWatcher} instance
     */
    protected Main(Baker baker, JettyServer jetty, BakeWatcher watcher) {
        this.baker = baker;
        this.jettyServer = jetty;
        this.watcher = watcher;
        this.configurationFactory = new JBakeConfigurationFactory();
    }

    /**
     * Runs the app with the given arguments.
     *
     * @param args Application arguments
     */
    public static void main(final String[] args) {
        try {
            new Main().run(args);
        } catch (final JBakeException e) {
            logger.error(e.getMessage());
            logger.trace(e.getMessage(), e);
            if (e.getCause() instanceof MissingParameterException) {
                Main.printUsage();
            }
            System.exit(e.getExit());
        }
    }

    public void run(String[] args) throws JBakeException {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            final JBakeConfiguration config;

            LaunchOptions res = parseArguments(args);
            if (res.isRunServer()) {
                config = getJBakeConfigurationFactory().createJettyJbakeConfiguration(res.getSource(), res.getDestination(), res.isClearCache());
            } else {
                config = getJBakeConfigurationFactory().createDefaultJbakeConfiguration(res.getSource(), res.getDestination(), res.isClearCache());
            }
            run(res, config);
        } catch (final ConfigurationException e) {
            throw new JBakeException(SystemExit.CONFIGURATION_ERROR, "Configuration error: " + e.getMessage(), e);
        } catch (MissingParameterException mex) {
            throw new JBakeException(SystemExit.CONFIGURATION_ERROR, mex.getMessage(), mex);
        } catch (final Throwable e) {
            throw new JBakeException(SystemExit.ERROR, "An unexpected error occurred: " + e.getMessage(), e);
        }
    }

    protected void run(LaunchOptions res, JBakeConfiguration config) {
        System.out.println("JBake " + config.getVersion() + " (" + config.getBuildTimeStamp() + " " + config.getAbbreviatedGitHash()  + "#) [http://jbake.org]");
        System.out.println();

        if (res.isHelpNeeded()) {
            printUsage(res);
            // Help was requested, so we are done here
            return;
        }

        if (res.isListConfig()) {
            ConfigurationPrinter printer = new ConfigurationPrinter(config, System.out);
            printer.print();
            return;
        }

        if (res.isBake()) {
            baker.bake(config);
        }

        if (res.isInit()) {
            initStructure(res.getTemplate(), config);
        }

        if (res.isRunServer()) {
            watcher.start(config);
            // TODO: short term fix until bake, server, init commands no longer share underlying values (such as source/dest)
            if (res.isBake()) {
                // bake and server commands have been run together
                if (res.getDestination() != null) {
                    // use the destination provided via the commandline
                    runServer(res.getDestination(), config);
                } else if (!res.getSource().getPath().equals(".")) {
                    // use the source folder provided via the commandline
                    runServer(res.getSource(), config);
                } else {
                    // use the default DESTINATION_FOLDER value
                    runServer(config.getDestinationFolder(), config);
                }
            } else {
                // use the default destination folder
                runServer(config.getDestinationFolder(), config);
            }
        }
    }

    private LaunchOptions parseArguments(String[] args) {
        return CommandLine.populateCommand(new LaunchOptions(), args);
    }

    private void printUsage(Object options) {
        CommandLine cli = new CommandLine(options);
        cli.setUsageHelpLongOptionsMaxWidth(28);
        cli.usage(System.out);
    }

    public static void printUsage() {
        CommandLine.usage(new LaunchOptions(), System.out);
    }

    private void runServer(File path, JBakeConfiguration configuration) {
        jettyServer.run(path.getPath(), configuration);
    }

    private void initStructure(String type, JBakeConfiguration config) {
        Init init = new Init(config);
        try {
            File templateFolder = FileUtil.getRunningLocation();
            File outputFolder;
            if (config.getSourceFolder() != null) {
                outputFolder = config.getSourceFolder();
            } else {
                outputFolder = new File(".");
            }
            init.run(outputFolder, templateFolder, type);
            System.out.println("Base folder structure successfully created.");
        } catch (final Exception e) {
            final String msg = "Failed to initialise structure: " + e.getMessage();
            throw new JBakeException(SystemExit.INIT_ERROR, msg, e);
        }
    }

    public JBakeConfigurationFactory getJBakeConfigurationFactory() {
        return configurationFactory;
    }

    public void setJBakeConfigurationFactory(JBakeConfigurationFactory factory) {
        configurationFactory = factory;
    }


}
