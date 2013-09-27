package org.jbake.launcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.jbake.app.ConfigUtil;
import org.jbake.app.Oven;
import org.jbake.app.ZipUtil;

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

			CompositeConfiguration config = null;
			try {
				config = ConfigUtil.load(res.getSource());
			} catch (ConfigurationException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			if (res.isHelpNeeded()) {
				printUsage(parser);
			}
			
			if (res.isRunServer()) {
				if (res.getSource().getPath().equals(".")) {
					// use the default destination folder
					runServer(config.getString("destination.folder"), config.getString("server.port"));
				} else {
					runServer(res.getSource().getPath(), config.getString("server.port"));
				}
			}
			
			if (res.isInit()) {
				initStructure(config);
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
		JettyServer.run(path, port);
        System.exit(0);
	}
	
	private void initStructure(CompositeConfiguration config) {
		File outputFolder = new File(".");
		if (!outputFolder.canWrite()) {
            System.err.println("Error: Folder is not writable!");
            System.exit(1);
        }
		
		File[] contents = outputFolder.listFiles();
		boolean safe = true;
		if (contents != null) {
			for (File content : contents) {
				if (content.isDirectory()) {
					if (content.getName().equalsIgnoreCase(config.getString("template.folder"))) {
						safe = false;
					}
					if (content.getName().equalsIgnoreCase(config.getString("content.folder"))) {
						safe = false;
					}
					if (content.getName().equalsIgnoreCase(config.getString("asset.folder"))) {
						safe = false;
					}
				}
			}
		}
		
		if (!safe) {
			System.err.println("Error: Folder already contains structure!");
			System.exit(2);
		}
		
		String codeLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = null;
		try {
			decodedPath = URLDecoder.decode(codeLocation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println("Error: Cannot locate running location for JBake!");
			e.printStackTrace();
			System.exit(4);
		}
		File codeRef = new File(decodedPath);
		if (!codeRef.exists()) {
			System.err.println("Error: Cannot locate running location for JBake!");
			System.exit(4);
		}
		File sourcePath = codeRef.getParentFile();
		if (!sourcePath.exists()) {
			System.err.println("Error: Cannot locate running location for JBake!");
			System.exit(4);
		}
		File templateFile = new File(sourcePath, "base.zip");
		if (!templateFile.exists()) {
			System.err.println("Error: Cannot locate template file!");
			System.exit(4);
		}
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(templateFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.err.println("Error: Cannot locate template file!");
			System.exit(4);
		}
		if (fis != null) {
			try {
				ZipUtil.extract(fis, outputFolder);
			} catch (IOException e) {
				System.err.println("Error: Error occurred while extracting base template!");
				e.printStackTrace();
				System.exit(3);
			}
		} else {
			System.err.println("Error: Cannot locate template file!");
			System.exit(4);
		}
		
		System.out.println("Base folder structure successfully created.");
		System.exit(0);
	}
}
