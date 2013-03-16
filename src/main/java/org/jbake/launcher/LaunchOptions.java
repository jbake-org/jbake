package org.jbake.launcher;

import java.io.File;
 
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

class LaunchOptions {
	@Argument(index = 0, usage = "source of your blog posts (with templates and assets)", metaVar = "source_folder")
	private File source = new File(".");

	@Argument(index = 1, usage = "destination folder for baked artifacts", metaVar = "destination_folder")
	private File destination = new File("baked");

	@Option(name = "-h", aliases = {"--help"}, usage="prints this message")
	private boolean isHelpNeeded;

	
	LaunchOptions() {}

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
