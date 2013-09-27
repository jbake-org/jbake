package org.jbake.app;

import junit.framework.Assert;

import org.jbake.launcher.LaunchOptions;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class LaunchOptionsTest {

	@Test
	public void showHelp() throws Exception {
		String[] args = {"-h"};
		LaunchOptions res = new LaunchOptions();
		CmdLineParser parser = new CmdLineParser(res);
		parser.parseArgument(args);
		
		Assert.assertTrue(res.isHelpNeeded());
	}
}
