package org.jbake.app;

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
	
	private File validHTMLFile;
	private File invalidHTMLFile;
	private File validMarkdownFile;
	private File invalidMarkdownFile;
	
	private String validHeader = "title=This is a Title\nstatus=draft\ntype=post\n~~~~~~";
	private String invalidHeader = "title=This is a Title\n~~~~~~";
	
	@Before
	public void createSampleFile() throws IOException {
		validHTMLFile = folder.newFile("valid.html");
		PrintWriter out = new PrintWriter(validHTMLFile);
		out.println(validHeader);
		out.println("<p>This is a test.</p>");
		out.close();
		
		invalidHTMLFile = folder.newFile("invalid.html");
		out = new PrintWriter(invalidHTMLFile);
		out.println(invalidHeader);
		out.close();
		
		validMarkdownFile = folder.newFile("valid.md");
		out = new PrintWriter(validMarkdownFile);
		out.println(validHeader);
		out.println("# This is a test");
		out.close();
		
		invalidMarkdownFile = folder.newFile("invalid.md");
		out = new PrintWriter(invalidMarkdownFile);
		out.println(invalidHeader);
		out.println("# This is a test");
		out.close();
	}
	
	@Test
	public void parseValidHTMLFile() {
		Parser parser = new Parser();
		Map<String, Object> map = parser.processFile(validHTMLFile);
		Assert.assertNotNull(map);
		Assert.assertEquals(map.get("status"), "draft");
		Assert.assertEquals(map.get("type"), "post");
	}
	
	@Test
	public void parseInvalidHTMLFile() {
		Parser parser = new Parser();
		Map<String, Object> map = parser.processFile(invalidHTMLFile);
		Assert.assertNull(map);
	}
	
	@Test
	public void parseValidMarkdownFile() {
		Parser parser = new Parser();
		Map<String, Object> map = parser.processFile(validMarkdownFile);
		Assert.assertNotNull(map);
		Assert.assertEquals(map.get("status"), "draft");
		Assert.assertEquals(map.get("type"), "post");
		Assert.assertEquals(map.get("body"), "<h1>This is a test</h1>\n");
	}
	
	@Test
	public void parseInvalidMarkdownFile() {
		Parser parser = new Parser();
		Map<String, Object> map = parser.processFile(invalidMarkdownFile);
		Assert.assertNull(map);
	}
}
