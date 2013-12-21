package org.jbake.launcher;

import java.io.File;
 
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class LaunchOptions {
	@Argument(index = 0, usage = "source folder of site content (with templates and assets), if not supplied will default to current working directory", metaVar = "source_folder")
	private File source = new File(".");

	@Argument(index = 1, usage = "destination folder for output, if not supplied will default to a folder called \"output\" in the current working directory", metaVar = "destination_folder")
	private File destination = null;
	
	@Option(name = "-i", aliases = {"--init"}, usage="initialises required folder structure with default templates")
	private boolean init;
	
	@Option(name = "-s", aliases = {"--server"}, usage="runs HTTP server to serve out destination folder")
	private boolean runServer;
	
	@Option(name = "-h", aliases = {"--help"}, usage="prints this message")
	private boolean helpNeeded;

    @Option(name = "--reset", usage="clears the local cache, enforcing rendering from scratch")
    private boolean clearCache;

	public File getSource() {
		return source;
	}

	public File getDestination() {
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
}
