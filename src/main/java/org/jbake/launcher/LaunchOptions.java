package org.jbake.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class LaunchOptions {
	@Argument(index = 0, usage = "source folder of site content (with templates and assets), if not supplied will default to current directory", metaVar = "<source>")
	private String source;
	
	@Argument(index = 1, usage = "destination folder for output, if not supplied will default to a folder called \"output\" in the current directory", metaVar = "<destination>")
	private String destination;
	
	@Option(name = "-b", aliases = {"--bake"}, usage="start baking (this is the default action if no option is supplied)")
	private boolean bake;
	
	@Option(name = "-i", aliases = {"--init"}, usage="initialises required folder structure with default templates")
	private boolean init;
	
	@Option(name = "-s", aliases = {"--server"}, usage="runs HTTP server to serve out baked site, if no <value> is supplied will default to a folder called \"output\" in the current directory")
	private boolean runServer;
	
	@Option(name = "-h", aliases = {"--help"}, usage="prints this message")
	private boolean helpNeeded;

    @Option(name = "--reset", usage="clears the local cache, enforcing rendering from scratch")
    private boolean clearCache;
    
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

    public boolean isBake() {
		return bake || !(isHelpNeeded() || isRunServer() || isInit());
	}
}
