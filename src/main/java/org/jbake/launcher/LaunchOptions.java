package org.jbake.launcher;

import java.io.File;
 
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

class LaunchOptions {
	@Argument(index = 0, usage = "source folder of site content (with templates and assets) defaults to current working directory", metaVar = "source_folder")
	private File source = new File(".");

	@Argument(index = 1, usage = "destination folder for baked output, defaults to a folder called \"baked\" in the current working directory", metaVar = "destination_folder")
	private File destination = null;

	@Option(name = "-h", aliases = {"--help"}, usage="prints this message")
	private boolean isHelpNeeded;

	File getSource() {
		return source;
	}

	File getDestination() {
		return destination;
	}

	boolean isHelpNeeded() {
		return isHelpNeeded;
	}
}
