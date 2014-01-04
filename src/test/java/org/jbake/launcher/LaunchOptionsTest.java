package org.jbake.launcher;

import java.io.File;
import static org.fest.assertions.Assertions.*;

import junit.framework.Assert;

import org.jbake.launcher.LaunchOptions;
import org.junit.Test;
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
	
	@Test
	public void runServer() throws Exception {
		String[] args = {"-s"};
		LaunchOptions res = new LaunchOptions();
		CmdLineParser parser = new CmdLineParser(res);
		parser.parseArgument(args);
		
		Assert.assertTrue(res.isRunServer());
	}
	
	@Test
	public void runServerWithFolder() throws Exception {
		String[] args = {"-s", "/tmp"};
		LaunchOptions res = new LaunchOptions();
		CmdLineParser parser = new CmdLineParser(res);
		parser.parseArgument(args);
		
		Assert.assertTrue(res.isRunServer());
		assertThat(res.getSource()).isEqualTo(new File("/tmp"));
	}
	
	@Test
	public void init() throws Exception {
		String[] args = {"-i"};
		LaunchOptions res = new LaunchOptions();
		CmdLineParser parser = new CmdLineParser(res);
		parser.parseArgument(args);
		
		Assert.assertTrue(res.isInit());
	}
	
	@Test
	public void bakeNoArgs() throws Exception {
		String[] args = {};
		LaunchOptions res = new LaunchOptions();
		CmdLineParser parser = new CmdLineParser(res);
		parser.parseArgument(args);
		
		Assert.assertFalse(res.isHelpNeeded());
		Assert.assertFalse(res.isRunServer());
		Assert.assertFalse(res.isInit());
		Assert.assertEquals(".", res.getSource().getPath());
		Assert.assertEquals(null, res.getDestination());
	}
	
	@Test
	public void bakeWithArgs() throws Exception {
		String[] args = {"/tmp/source", "/tmp/destination"};
		LaunchOptions res = new LaunchOptions();
		CmdLineParser parser = new CmdLineParser(res);
		parser.parseArgument(args);
		
		Assert.assertFalse(res.isHelpNeeded());
		Assert.assertFalse(res.isRunServer());
		Assert.assertFalse(res.isInit());
		assertThat(res.getSource()).isEqualTo(new File("/tmp/source"));
		assertThat(res.getDestination()).isEqualTo(new File("/tmp/destination"));
	}
}
