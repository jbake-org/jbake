package org.jbake.launcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

import de.tototec.cmdoption.CmdlineParser;

public class LaunchOptionsTest {

	@Test
	public void showHelp() throws Exception {
		String[] args = { "-h" };
		LaunchOptions res = new LaunchOptions();
		CmdlineParser parser = new CmdlineParser(res);
		parser.parse(args);

		Assert.assertTrue(res.isHelpNeeded());
	}

	@Test
	public void runServer() throws Exception {
		String[] args = { "-s" };
		LaunchOptions res = new LaunchOptions();
		CmdlineParser parser = new CmdlineParser(res);
		parser.parse(args);

		Assert.assertTrue(res.isRunServer());
	}

	@Test
	public void runServerWithFolder() throws Exception {
		String[] args = { "-s", "--source", "/tmp" };
		LaunchOptions res = new LaunchOptions();
		CmdlineParser parser = new CmdlineParser(res);
		parser.parse(args);

		Assert.assertTrue(res.isRunServer());
		assertThat(res.getSource()).isEqualTo(new File("/tmp"));
	}

	@Test
	public void init() throws Exception {
		String[] args = { "-i" };
		LaunchOptions res = new LaunchOptions();
		CmdlineParser parser = new CmdlineParser(res);
		parser.parse(args);

		Assert.assertTrue(res.isInit());
	}

	@Test
	public void bake() throws Exception {
		String[] args = { "-b" };
		LaunchOptions res = new LaunchOptions();
		CmdlineParser parser = new CmdlineParser(res);
		parser.parse(args);

		Assert.assertTrue(res.isBake());
	}

	@Test
	public void bakeNoArgs() throws Exception {
		String[] args = {};
		LaunchOptions res = new LaunchOptions();
		CmdlineParser parser = new CmdlineParser(res);
		parser.parse(args);

		Assert.assertFalse(res.isHelpNeeded());
		Assert.assertFalse(res.isRunServer());
		Assert.assertFalse(res.isInit());
		Assert.assertTrue(res.isBake());
		Assert.assertEquals(".", res.getSource().getPath());
		Assert.assertEquals(null, res.getDestination());
	}

	@Test
	public void bakeWithArgs() throws Exception {
		String[] args = { "--source", "/tmp/source", "--destination", "/tmp/destination" };
		LaunchOptions res = new LaunchOptions();
		CmdlineParser parser = new CmdlineParser(res);
		parser.parse(args);

		Assert.assertFalse(res.isHelpNeeded());
		Assert.assertFalse(res.isRunServer());
		Assert.assertFalse(res.isInit());
		Assert.assertTrue(res.isBake());
		assertThat(res.getSource()).isEqualTo(new File("/tmp/source"));
		assertThat(res.getDestination()).isEqualTo(new File("/tmp/destination"));
	}
}
