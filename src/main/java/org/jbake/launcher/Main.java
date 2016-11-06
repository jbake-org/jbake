package org.jbake.launcher;

import java.io.File;
import java.io.StringWriter;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.ConfigUtil;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.FileUtil;
import org.jbake.app.JBakeException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Launcher for JBake.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Main {

	private final String USAGE_PREFIX 		= "Usage: jbake";
	private final String ALT_USAGE_PREFIX	= "   or  jbake";

	/**
	 * Runs the app with the given arguments.
	 *
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			new Main().run(args);
		} catch (final JBakeException e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		} catch (final Throwable e) {
			System.err.println("An unexpected error occurred: " + e.getMessage());
			System.exit(2);
		}
	}

	private Baker baker;
	private JettyServer jettyServer;
	private BakeWatcher watcher;

	/**
	 * Default constructor.
	 */
	public Main() {
		this(new Baker(), new JettyServer(), new BakeWatcher());
	}

	/**
	 * Optional constructor to externalize dependencies.
	 *
	 * @param baker
	 * @param jetty
	 * @param watcher
	 */
	protected Main(Baker baker, JettyServer jetty, BakeWatcher watcher) {
		this.baker = baker;
		this.jettyServer = jetty;
		this.watcher = watcher;
	}

	protected void run(String[] args) {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		LaunchOptions res = parseArguments( args );

		final CompositeConfiguration config;
		try {
			config = ConfigUtil.load( res.getSource() );
		} catch( final ConfigurationException e ) {
			throw new JBakeException( "Configuration error: " + e.getMessage(), e );
		}

		run(res, config);
	}

	protected void run(LaunchOptions res, CompositeConfiguration config) {
		System.out.println("JBake " + config.getString(Keys.VERSION) + " (" + config.getString(Keys.BUILD_TIMESTAMP) + ") [http://jbake.org]");
		System.out.println();
		
		if (res.isHelpNeeded()) {
			printUsage(res);
			// Help was requested, so we are done here
			return;
		}

		if (res.isBake()) {
			baker.bake(res, config);
		}

		if (res.isInit()) {
			initStructure(config, res.getTemplate(), res.getSourceValue());
		}
		
		if (res.isRunServer()) {
			watcher.start(res, config);
			if (res.getSource().getPath().equals(".")) {
				// use the default destination folder
				runServer( config.getString( Keys.DESTINATION_FOLDER ), config.getString( Keys.SERVER_PORT ) );
			} else if (res.getDestination() != null) {
				// use the destination provided via the commandline
				runServer( res.getDestination().getPath(), config.getString( Keys.SERVER_PORT ));
			} else {
				runServer(config.getString( Keys.DESTINATION_FOLDER ), config.getString(Keys.SERVER_PORT));
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
		parser.setUsageWidth(100);
		parser.printUsage(System.out);
	}

	private void runServer(String path, String port) {
		jettyServer.run(path, port);
	}

	private void initStructure(CompositeConfiguration config, String type, String source) {
        Init init = new Init(config);
		try {
            File templateFolder = FileUtil.getRunningLocation();
            File outputFolder;
            if(source != null){
                outputFolder = new File(source);
            } else{
                outputFolder = new File(".");
            }
            init.run(outputFolder, templateFolder, type);
			System.out.println("Base folder structure successfully created.");
		} catch (final Exception e) {
			final String msg = "Failed to initialise structure: " + e.getMessage();
			throw new JBakeException(msg, e);
		}
	}

}
