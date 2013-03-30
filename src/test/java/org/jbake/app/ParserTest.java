package org.jbake.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ParserTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private File validFile;
	private File invalidFile;
	
	@Before
	public void createSampleFile() throws IOException {
		validFile = folder.newFile("valid.html");
		PrintWriter out = new PrintWriter(validFile);
		out.println("title=This is a Title");
		out.println("status=draft");
		out.println("type=post");
		out.println("~~~~~~");
		out.close();
		
		invalidFile = folder.newFile("invalid.html");
		out = new PrintWriter(invalidFile);
		out.println("title=This is a Title");
		out.println("~~~~~~");
		out.close();
	}
	
	@Test
	public void parseValidFile() {
		Parser parser = new Parser();
		Map<String, Object> map = parser.processFile(validFile);
		Assert.assertNotNull(map);
		Assert.assertEquals(map.get("status"), "draft");
		Assert.assertEquals(map.get("type"), "post");
	}
	
	@Test
	public void parseInvalidFile() {
		Parser parser = new Parser();
		Map<String, Object> map = parser.processFile(invalidFile);
		Assert.assertNull(map);
	}
}
