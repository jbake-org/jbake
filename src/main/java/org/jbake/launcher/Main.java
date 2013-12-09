package org.jbake.launcher;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Launcher for JBake.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Main {
//	public static final String VERSION = "v2.2";

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
                CompositeConfiguration config = ConfigUtil.load(options.getSource());
                Asset asset = new Asset(options.getSource(), options.getDestination());
                Oven oven = new Oven(options.getSource(), options.getDestination(), config, asset);

                if( options.isWatch() ) {
                    watch(config, oven, options, asset);
                } else {
                    oven.bake();
                }
            } catch (Exception e) {
    //			System.err.println(e.getMessage());
                e.printStackTrace();
            }
	}

    private void watch(CompositeConfiguration config, Oven oven, LaunchOptions options, Asset asset) throws Exception {
        final WatchService watcher = FileSystems.getDefault().newWatchService();
        final Map<WatchKey,Path> map = new HashMap<>();

        Files.walkFileTree(options.getSource().toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey register = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                map.put(register, dir);
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("Watching " + options.getSource() + " for changes...");

        for(;;) {
            WatchKey key = watcher.take();
            Path dir = map.get(key);
            for ( WatchEvent<?> event: key.pollEvents()){
                Crawler crawler = new Crawler(options.getSource(), config);

                WatchEvent.Kind kind = event.kind();

                WatchEvent<Path> ev = (WatchEvent<Path>) event;

                switch (kind.name()){
                    case "ENTRY_MODIFY":
                    case "ENTRY_CREATE":
                        System.out.println("Modified or created: " + dir.resolve(ev.context()));
                        if( asset.isAsset(dir.resolve(ev.context())) ) {
                            asset.copySingleAsset(dir.resolve(ev.context()).toFile());
                        } else {
                            //crawler.processSingleFile(dir.resolve(ev.context()).toFile());
                            //oven.bake(crawler.getPages(), crawler.getPosts(), crawler.getPostsByTags());
                            crawler.crawl(options.getSource());
                            oven.bake();
                        }
                        break;
                    case "ENTRY_DELETE":
                        System.out.println("Delete: "+ev.context());
                        break;
                    default:
                        System.out.println("Unknown event: " +kind.name());
                        break;
                }
            }
            boolean valid = key.reset();

            if (!valid) break;
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
			
			System.out.println("JBake " + config.getString("version") + " (" + config.getString("build.timestamp") + ") [http://jbake.org]");
			System.out.println();
			
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
