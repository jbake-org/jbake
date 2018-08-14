package org.jbake.launcher;

import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.FileUtil;
import org.jbake.app.JBakeException;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.StringWriter;

/**
 * Launcher for JBake.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Main {

    private final String USAGE_PREFIX = "Usage: jbake";
    private final String ALT_USAGE_PREFIX = "   or  jbake";
    private Baker baker;
    private JettyServer jettyServer;
    private BakeWatcher watcher;
    private JBakeConfigurationFactory configurationFactory;
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
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final Throwable e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    protected void run(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LaunchOptions res = parseArguments(args);

        final JBakeConfiguration config;
        try {
            if (res.isRunServer()) {
                config = getJBakeConfigurationFactory().createJettyJbakeConfiguration(res.getSource(), res.getDestination(), res.isClearCache());
            } else {
                config = getJBakeConfigurationFactory().createDefaultJbakeConfiguration(res.getSource(), res.getDestination(), res.isClearCache());
            }
        } catch (final ConfigurationException e) {
            throw new JBakeException("Configuration error: " + e.getMessage(), e);
        }
        run(res, config);
    }

    protected void run(LaunchOptions res, JBakeConfiguration config) {
        System.out.println("JBake " + config.getVersion() + " (" + config.getBuildTimeStamp() + ") [http://jbake.org]");
        System.out.println();

        if (res.isHelpNeeded()) {
            printUsage(res);
            // Help was requested, so we are done here
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
                    runServer(res.getDestination(), config.getServerPort());
                } else if (!res.getSource().getPath().equals(".")) {
                    // use the source folder provided via the commandline
                    runServer(res.getSource(), config.getServerPort());
                } else {
                    // use the default DESTINATION_FOLDER value
                    runServer(config.getDestinationFolder(), config.getServerPort());
                }
            } else {
                // use the default destination folder
                runServer(config.getDestinationFolder(), config.getServerPort());
            }
        }

    }

    private LaunchOptions parseArguments(String[] args) {
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);

        try {
            parser.parseArgument(args);
        } catch (final CmdLineException e) {
            printUsage(res);
            throw new JBakeException("Invalid commandline arguments: " + e.getMessage(), e);
        }

        return res;
    }

    private void printUsage(Object options) {
        CmdLineParser parser = new CmdLineParser(options);
        StringWriter sw = new StringWriter();
        sw.append(USAGE_PREFIX + "\n");
        sw.append(ALT_USAGE_PREFIX + " <source> <destination>\n");
        sw.append(ALT_USAGE_PREFIX + " [OPTION]... [<value>...]\n\n");
        sw.append("Options:");
        System.out.println(sw.toString());
        parser.getProperties().withUsageWidth(100);
        parser.printUsage(System.out);
    }

    private void runServer(File path, int port) {
        jettyServer.run(path.getPath(), String.valueOf(port));
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
            throw new JBakeException(msg, e);
        }
    }

    public JBakeConfigurationFactory getJBakeConfigurationFactory() {
        return configurationFactory;
    }

    public void setJBakeConfigurationFactory(JBakeConfigurationFactory factory) {
        configurationFactory = factory;
    }


}
