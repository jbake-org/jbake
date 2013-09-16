package org.jbake.launcher;

import java.io.File;
 
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

class LaunchOptions {
	@Argument(index = 0, usage = "source folder of site content (with templates and assets), if not supplied will default to current working directory", metaVar = "source_folder")
	private File source = new File(".");

	@Argument(index = 1, usage = "destination folder for output, if not supplied will default to a folder called \"output\" in the current working directory, can be overridden by setting destination.folder in custom.properties", metaVar = "destination_folder")
	private File destination = null;

	@Option(name = "-h", aliases = {"--help"}, usage="prints this message")
	private boolean isHelpNeeded;
	
	@Option(name = "-s", aliases = {"--server"}, usage="serves out destination folder on port 8080")
	private boolean isRunServer;

	File getSource() {
		return source;
	}

	File getDestination() {
		return destination;
	}

	boolean isHelpNeeded() {
		return isHelpNeeded;
	}
	
	boolean isRunServer() {
		return isRunServer;
	}
}
