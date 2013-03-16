package org.jbake.launcher;

import java.io.File;
import java.io.StringWriter;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.jbake.app.Oven;

/**
 * Launcher for JBake.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Main {
	public static final String VERSION = "v2.0";

	private final String USAGE_PREFIX = "Usage: java -jar jbake.jar";
	
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

			checkIsSourceFolder(res.getSource());

			processDestinationFolderArgument(res.getDestination());
		} catch (IllegalArgumentException iae) {
			System.err.println(iae.getMessage());
			System.exit(0);
		} catch (CmdLineException e) {
			printUsage(parser);
		}

		return res;
	}

	private void processDestinationFolderArgument(File f) {
		if (!f.exists()) {
			f.mkdir();
		}

		checkIsFolder(f);
	}

	private void checkIsSourceFolder(File f) {
		if (!f.exists()) {
			throw new IllegalArgumentException("Source folder MUST exist");
		}

		checkIsFolder(f);
	}

	private void checkIsFolder(File f) {
		if (!f.isDirectory()) {
			throw new IllegalArgumentException("You should provide path to folder, not file");
		}
	}

	private void printUsage(CmdLineParser parser) {
		StringWriter sw = new StringWriter();
		sw.append(USAGE_PREFIX);
		parser.printSingleLineUsage(sw, null);
		System.out.println(sw.toString());
		parser.printUsage(System.out);
		System.exit(0);
	}

}
