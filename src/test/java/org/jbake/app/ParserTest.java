package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ParserTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	public CompositeConfiguration config;
	public Parser parser;
	private File rootPath;
	
	private File validHTMLFile;
	private File invalidHTMLFile;
	private File validMarkdownFile;
	private File invalidMarkdownFile;
	private File validAsciiDocFile;
	private File invalidAsciiDocFile;
	private File validAsciiDocFileWithoutHeader;
	private File invalidAsciiDocFileWithoutHeader;
	private File validAsciiDocFileWithHeaderInContent;
	
	private String validHeader = "title=This is a Title\nstatus=draft\ntype=post\n~~~~~~";
	private String invalidHeader = "title=This is a Title\n~~~~~~";

  
	
	@Before
	public void createSampleFile() throws Exception {
		ConfigUtil.reset();
		rootPath = new File(this.getClass().getResource(".").getFile());
		config = ConfigUtil.load(rootPath);
		parser = new Parser(config,rootPath.getPath());
		
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
		
		validAsciiDocFile = folder.newFile("valid.ad");
		out = new PrintWriter(validAsciiDocFile);
		out.println(validHeader);
		out.println("= Hello, AsciiDoc!");
		out.println("Test User <user@test.org>");
		out.println("");
		out.println("JBake now supports AsciiDoc.");
		out.close();
		
		invalidAsciiDocFile = folder.newFile("invalid.ad");
		out = new PrintWriter(invalidAsciiDocFile);
		out.println(invalidHeader);
		out.println("= Hello, AsciiDoc!");
		out.println("Test User <user@test.org>");
		out.println("");
		out.println("JBake now supports AsciiDoc.");
		out.close();
		
		validAsciiDocFileWithoutHeader = folder.newFile("validwoheader.ad");
		out = new PrintWriter(validAsciiDocFileWithoutHeader);
		out.println("= Hello, AsciiDoc!");
		out.println("Test User <user@test.org>");
		out.println("2013-09-02");
		out.println(":jbake-status: published");
		out.println(":jbake-type: page");
		out.println("");
		out.println("JBake now supports AsciiDoc.");
		out.close();
		
		invalidAsciiDocFileWithoutHeader = folder.newFile("invalidwoheader.ad");
		out = new PrintWriter(invalidAsciiDocFileWithoutHeader);
		out.println("= Hello, AsciiDoc!");
		out.println("Test User <user@test.org>");
		out.println("2013-09-02");
		out.println(":jbake-status: published");
		out.println("");
		out.println("JBake now supports AsciiDoc.");
		out.close();
		
		validAsciiDocFileWithHeaderInContent = folder.newFile("validheaderincontent.ad");
		out = new PrintWriter(validAsciiDocFileWithHeaderInContent);
		out.println("= Hello, AsciiDoc!");
		out.println("Test User <user@test.org>");
		out.println("2013-09-02");
		out.println(":jbake-status: published");
		out.println(":jbake-type: page");
		out.println("");
		out.println("JBake now supports AsciiDoc.");
		out.println("");
		out.println("----");
		out.println("title=Example Header");
		out.println("date=2013-02-01");
		out.println("type=post");
		out.println("tags=tag1, tag2");
		out.println("status=published");
		out.println("~~~~~~");
		out.println("----");
		out.close();
	}
	
	@Test
	public void parseValidHTMLFile() {
		Map<String, Object> map = parser.processFile(validHTMLFile);
		Assert.assertNotNull(map);
		Assert.assertEquals("draft", map.get("status"));
		Assert.assertEquals("post", map.get("type"));
	}
	
	@Test
	public void parseInvalidHTMLFile() {
		Map<String, Object> map = parser.processFile(invalidHTMLFile);
		Assert.assertNull(map);
	}
	
	@Test
	public void parseValidMarkdownFile() throws Exception {
		Map<String, Object> map = parser.processFile(validMarkdownFile);
		Assert.assertNotNull(map);
		Assert.assertEquals("draft", map.get("status"));
		Assert.assertEquals("post", map.get("type"));
		Assert.assertEquals("<h1>This is a test</h1>\n", map.get("body"));
	}
	
	@Test
	public void parseInvalidMarkdownFile() {
		Parser parser = new Parser(config,rootPath.getPath());
		Map<String, Object> map = parser.processFile(invalidMarkdownFile);
		Assert.assertNull(map);
	}
	
	@Test
	public void parseValidAsciiDocFile() {
		Map<String, Object> map = parser.processFile(validAsciiDocFile);
		Assert.assertNotNull(map);
		Assert.assertEquals("draft", map.get("status"));
		Assert.assertEquals("post", map.get("type"));
		Assert.assertEquals("<div id=\"preamble\">\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>JBake now supports AsciiDoc.</p>\n</div>\n</div>\n</div>", map.get("body"));
	}
	
	@Test
	public void parseInvalidAsciiDocFile() {
		Parser parser = new Parser(config,rootPath.getPath());
		Map<String, Object> map = parser.processFile(invalidAsciiDocFile);
		Assert.assertNull(map);
	}
	
	@Test
	public void parseValidAsciiDocFileWithoutHeader() {
		Map<String, Object> map = parser.processFile(validAsciiDocFileWithoutHeader);
		Assert.assertNotNull(map);
		Assert.assertEquals("published", map.get("status"));
		Assert.assertEquals("page", map.get("type"));
		Assert.assertEquals("<div id=\"preamble\">\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>JBake now supports AsciiDoc.</p>\n</div>\n</div>\n</div>", map.get("body"));
	}
	
	@Test
	public void parseInvalidAsciiDocFileWithoutHeader() {
		Parser parser = new Parser(config,rootPath.getPath());
		Map<String, Object> map = parser.processFile(invalidAsciiDocFileWithoutHeader);
		Assert.assertNull(map);
	}
	
	@Test
	public void parseValidAsciiDocFileWithExampleHeaderInContent() {
		Map<String, Object> map = parser.processFile(validAsciiDocFileWithHeaderInContent);
		Assert.assertNotNull(map);
		Assert.assertEquals("published", map.get("status"));
		Assert.assertEquals("page", map.get("type"));
		Assert.assertEquals("<div id=\"preamble\">\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>JBake now supports AsciiDoc.</p>\n</div>\n<div class=\"listingblock\">\n<div class=\"content\">\n<pre>title=Example Header\ndate=2013-02-01\ntype=post\ntags=tag1, tag2\nstatus=published\n~~~~~~</pre>\n</div>\n</div>\n</div>\n</div>", map.get("body"));
	}
}
