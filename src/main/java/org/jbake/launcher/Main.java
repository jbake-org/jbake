package org.jbake.launcher;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.ConfigUtil;
import org.jbake.app.FileUtil;
import org.jbake.app.Oven;

import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;
import de.tototec.cmdoption.DefaultUsageFormatter;

/**
 * Launcher for JBake.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Main {

	private final LaunchOptions launchOptions;

	public Main(final LaunchOptions launchOptions) {
		this.launchOptions = launchOptions;
	}

	/**
	 * Runs the app with the given arguments.
	 * 
	 * FIXME: Only this method should call System.exit(). All others should be
	 * // free of System.exit() calls to be easier to embedd. Instead a //
	 * JBakeException should be used.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		final LaunchOptions res = new LaunchOptions();
		final CmdlineParser parser = new CmdlineParser(res);
		parser.setProgramName("jbake");

		try {
			parser.parse(args);
			if (res.isHelpNeeded()) {
				parser.setUsageFormatter(new DefaultUsageFormatter(true, 100));
				parser.usage();
				System.exit(0);
			}
		} catch (CmdlineParserException e) {
			parser.usage();
			System.exit(1);
		}

		try {
			new Main(res).run();
		} catch (JBakeException e) {
			// TODO: localize
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

	public static LaunchOptions parseOption(String[] args) {
		final LaunchOptions options = new LaunchOptions();
		final CmdlineParser parser = new CmdlineParser(options);
		parser.setProgramName("jbake");
		parser.parse(args);
		return options;
	}

	private void bake() {
		try {
			Oven oven = new Oven(launchOptions.getSource(), launchOptions.getDestination(), launchOptions.isClearCache());
			oven.setupPaths();
			oven.bake();
			final List<String> errors = oven.getErrors();
			if (!errors.isEmpty()) {
				// TODO: decide, if we want the all error here
				String msg = MessageFormat.format("JBake failed with {0} errors:", errors.size());
				int errNr = 1;
				for (String error : errors) {
					msg += MessageFormat.format("{0}. {1}", errNr, error);
					++errNr;
				}
				throw new JBakeException(msg);
			}
		} catch (Exception e) {
			throw new JBakeException("An error occured while baking. " + e.getMessage(), e);
		}
	}

	/**
	 * @throws JBakeException
	 *             When errors occur.
	 */
	public void run() {
		CompositeConfiguration config = null;
		try {
			config = ConfigUtil.load(launchOptions.getSource());
		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw new JBakeException("Configuration error: " + e.getMessage(),
					e);
		}

		System.out.println("JBake " + config.getString("version") + " (" + config.getString("build.timestamp") + ") [http://jbake.org]");
		System.out.println();

		if (launchOptions.isInit()) {
			if (launchOptions.getSourceValue() != null) {
				// if type has been supplied then use it
				initStructure(config, launchOptions.getSourceValue());
			} else {
				// default to freemarker if no value has been supplied
				initStructure(config, "freemarker");
			}
		}

		if (launchOptions.isBake()) {
			bake();
		}

		if (launchOptions.isRunServer()) {
			if (launchOptions.getSource().getPath().equals(".")) {
				// use the default destination folder
				runServer(config.getString("destination.folder"), config.getString("server.port"));
			} else {
				runServer(launchOptions.getSource().getPath(), config.getString("server.port"));
			}
		}

	}

	private void runServer(String path, String port) {
		JettyServer.run(path, port);
	}

	private void initStructure(CompositeConfiguration config, String type) {
		Init init = new Init(config);
		try {
			File codeFolder = FileUtil.getRunningLocation();
			init.run(new File("."), codeFolder, type);
			if (launchOptions.isVerbose()) {
				System.out.println("Base folder structure successfully created.");
			}
		} catch (Exception e) {
			throw new JBakeException("Failed to initalise structure!", e);
		}
	}
}
