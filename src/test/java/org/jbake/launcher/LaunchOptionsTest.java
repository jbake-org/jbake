package org.jbake.launcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import junit.framework.Assert;

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
	public void bake() throws Exception {
		String[] args = {"-b"};
		LaunchOptions res = new LaunchOptions();
		CmdLineParser parser = new CmdLineParser(res);
		parser.parseArgument(args);

		Assert.assertTrue(res.isBake());
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
		Assert.assertTrue(res.isBake());
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
		Assert.assertTrue(res.isBake());
		assertThat(res.getSource()).isEqualTo(new File("/tmp/source"));
		assertThat(res.getDestination()).isEqualTo(new File("/tmp/destination"));
	}

	@Test
    public void create() throws Exception {
        String[] args = {"-c", "post.md"};
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        parser.parseArgument(args);

        Assert.assertTrue("--create should be true", res.isCreate());
        Assert.assertFalse("--bake should be false", res.isBake());
        Assert.assertEquals("post.md", res.getCreatedFileName());
    }
}
