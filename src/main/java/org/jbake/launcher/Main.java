package org.jbake.launcher;

import java.io.File;
import java.io.StringWriter;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.jbake.app.Oven;

/**
 * Launcher for JBake.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Main {
	public static final String VERSION = "v2.2";

	private final String USAGE_PREFIX = "Usage: jbake";
	
	/**
	 * Runs the app with the given arguments.
	 * 
	 * @param String[] args
	 */
	public static void main(String[] args) {
		Main m = new Main();
		m.run(m.parseArguments(args));
	}
	
	private void run(LaunchOptions options) {
		try {
			Oven oven = new Oven(options.getSource(), options.getDestination());
			oven.setupPaths();
			oven.bake();
		} catch (Exception e) {
//			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private LaunchOptions parseArguments(String[] args) {
		LaunchOptions res = new LaunchOptions();
		CmdLineParser parser = new CmdLineParser(res);

		try {
			parser.parseArgument(args);

			if (res.isHelpNeeded()) {
				printUsage(parser);
			}
			
			if (res.isRunServer()) {
				Oven oven = new Oven();
				CompositeConfiguration config = null;
				try {
					config = oven.loadConfig();
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
				if (res.getSource().getPath().equals(".")) {
					// use the default destination folder
					runServer(config.getString("destination.folder"), config.getString("server.port"));
				} else {
					runServer(res.getSource().getPath(), config.getString("server.port"));
				}
			}
		} catch (CmdLineException e) {
			printUsage(parser);
		}

		return res;
	}

	private void printUsage(CmdLineParser parser) {
		StringWriter sw = new StringWriter();
		sw.append(USAGE_PREFIX);
		parser.printSingleLineUsage(sw, null);
		System.out.println(sw.toString());
		parser.setUsageWidth(100);
		parser.printUsage(System.out);
		System.exit(0);
	}

	private void runServer(String path, String port) {
		Server server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(Integer.parseInt(port));
        server.addConnector(connector);
 
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
 
        resource_handler.setResourceBase(path);
 
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
        server.setHandler(handlers);
 
        System.out.println("Serving out: " + path + " on http://localhost:" + port + "/");
        
        try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.exit(0);
	}
}
