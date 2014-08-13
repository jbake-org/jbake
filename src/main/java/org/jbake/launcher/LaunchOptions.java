package org.jbake.launcher;

import java.io.File;

import de.tototec.cmdoption.CmdOption;

public class LaunchOptions {
	@CmdOption(names = { "--source" }, args = { "<source>" }, description = "source folder of site content (with templates and assets), if not supplied will default to current directory")
	private String source;

	@CmdOption(names = { "--destination", "-d" }, args = { "<destination>" }, description = "destination folder for output, if not supplied will default to a folder called \"output\" in the current directory")
	private String destination;

	@CmdOption(names = { "--bake", "-b" }, description = "start baking (this is the default action if no option is supplied)")
	private boolean bake;

	@CmdOption(names = { "--init", "-i" }, description = "initialises required folder structure with default templates")
	private boolean init;

	@CmdOption(names = { "--server", "-s" }, description = "runs HTTP server to serve out baked site, if no <value> is supplied will default to a folder called \"output\" in the current directory")
	private boolean runServer;

	@CmdOption(names = { "--help", "-h" }, description = "prints this message", isHelp = true)
	private boolean helpNeeded;

	@CmdOption(names = { "--reset" }, description = "clears the local cache, enforcing rendering from scratch")
	private boolean clearCache;

	@CmdOption(names = { "--verbose", "-v" }, description = "be more verbose")
	private boolean verbose;

	public File getSource() {
		if (source != null) {
			return new File(source);
		} else {
			return new File(".");
		}
	}

	public String getSourceValue() {
		return source;
	}

	public File getDestination() {
		if (destination != null) {
			return new File(destination);
		} else {
			return null;
		}

	}

	public String getDestinationValue() {
		return destination;
	}

	public boolean isHelpNeeded() {
		return helpNeeded;
	}

	public boolean isRunServer() {
		return runServer;
	}

	public boolean isInit() {
		return init;
	}

	public boolean isClearCache() {
		return clearCache;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public boolean isBake() {
		return bake || !(isHelpNeeded() || isRunServer() || isInit());
	}
}
