package org.jbake.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

			Oven oven = new Oven();
			CompositeConfiguration config = null;
			try {
				config = oven.loadConfig();
			} catch (ConfigurationException e) {
				e.printStackTrace();
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
		
		InputStream is = getClass().getResourceAsStream("/base.zip");
		if (is != null) {
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry;
			byte[] buffer = new byte[1024];
			
			try {
				while ((entry = zis.getNextEntry()) != null) {
					File outputFile = new File(outputFolder.getCanonicalPath() + File.separatorChar + entry.getName());
					File outputParent = new File(outputFile.getParent());
					outputParent.mkdirs();
					
					if (entry.isDirectory()) {
						if (!outputFile.exists()) {
							outputFile.mkdir();
						}
					} else {
						FileOutputStream fos = new FileOutputStream(outputFile);
						
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
		
						fos.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Error: Cannot locate base structure!");
			System.exit(3);
		}
		
		System.out.println("Base folder structure successfully created.");
		System.exit(0);
	}
}
