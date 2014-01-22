package org.jbake.launcher;

import java.io.File;
import java.io.StringWriter;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.ConfigUtil;
import org.jbake.app.FileUtil;
import org.jbake.app.Oven;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Launcher for JBake.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Main {

	private final String USAGE_PREFIX = "Usage: jbake";
	
	/**
	 * Runs the app with the given arguments.
	 * 
	 * @param String[] args
	 */
	public static void main(String[] args) {
		new Main().run(args);
	}
	
	private void bake(LaunchOptions options) {
		try {
			Oven oven = new Oven(options.getSource(), options.getDestination());
			oven.setupPaths();
			oven.bake();
		} catch (Exception e) {
//			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void run(String[] args) {
		LaunchOptions res = parseArguments(args);

		CompositeConfiguration config = null;
		try {
			config = ConfigUtil.load(res.getSource());
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("JBake " + config.getString("version") + " (" + config.getString("build.timestamp") + ") [http://jbake.org]");
		System.out.println();
		boolean didSomething=false;
		
		if (res.isHelpNeeded()) {
			didSomething=true;
			printUsage(res);
		}
		
		if(res.isBake()) {
			didSomething=true;
			bake(res);
		}

		if (res.isRunServer()) {
			didSomething=true;
			if (res.getSource().getPath().equals(".")) {
				// use the default destination folder
				runServer(config.getString("destination.folder"), config.getString("server.port"));
			} else {
				runServer(res.getSource().getPath(), config.getString("server.port"));
			}
		}
		
		if (res.isInit()) {
			didSomething=true;
			initStructure(config);
		}
		
		if(!didSomething) {
			bake(res);
		}
	}
	private LaunchOptions parseArguments(String[] args) {
		LaunchOptions res = new LaunchOptions();
		CmdLineParser parser = new CmdLineParser(res);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			printUsage(res);
		}

		return res;
	}

	private void printUsage(Object options) {
		CmdLineParser parser = new CmdLineParser(options);
		StringWriter sw = new StringWriter();
		sw.append(USAGE_PREFIX);
		parser.printSingleLineUsage(sw, null);
		System.out.println(sw.toString());
		parser.setUsageWidth(100);
		parser.printUsage(System.out);
		System.exit(0);
	}

	private void runServer(String path, String port) {
		JettyServer.run(path, port);
		System.exit(0);
	}
	
	private void initStructure(CompositeConfiguration config) {
		Init init = new Init(config);
		try {
			File codeFolder = FileUtil.getRunningLocation();
			init.run(new File("."), codeFolder);
			System.out.println("Base folder structure successfully created.");
			System.exit(0);
		} catch (Exception e) {
			System.err.println("Failed to initalise structure!");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
