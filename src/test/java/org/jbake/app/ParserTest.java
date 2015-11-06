package org.jbake.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbake.app.Parser.CONTINUED_LINE_STARTER;
import static org.jbake.app.Parser.END_OF_HEADER;
import static org.jbake.app.Parser.EOL;
import static org.jbake.app.Parser.ContentBasicTags.body;
import static org.jbake.app.Parser.ContentBasicTags.date;
import static org.jbake.app.Parser.ContentBasicTags.status;
import static org.jbake.app.Parser.ContentBasicTags.tags;
import static org.jbake.app.Parser.ContentBasicTags.type;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.StringUtils;
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
	private File validAsciiDocFile;
	private File invalidAsciiDocFile;
	private File validAsciiDocFileWithoutHeader;
	private File invalidAsciiDocFileWithoutHeader;
	private File validAsciiDocFileWithHeaderInContent;
	private File validAsciiDocFileWithEmptyFirstLineInHeader;
	private File validAsciiDocFileWithBlankFirstLineInHeader;
	private File validAsciiDocFileWithEmptyRandomLineInHeader;
	private File validAsciiDocFileWithBlankRandomLineInHeader;
	private File validAsciiDocFileWithSpacesAroundEqualInHeader;
	private File validAsciiDocFileWithContinuedLineInHeader;
	
	private String validHeader = "title=This is a Title = This is a valid Title" + EOL 
			+ "status=draft" + EOL 
			+ "type=post"+ EOL 
			+ "date=2013-09-02"+ EOL 
			+ END_OF_HEADER;
	private String invalidHeader = "title=This is a Title" + EOL + END_OF_HEADER;
	
	private int rp = 2; // "random" number
	private int pos = validHeader.indexOf(EOL, rp); // position where to insert a line
	
	private String validHeaderWithEmptyFirstLineContent = "This is a test with empty first line in header.";
	private String validHeaderWithEmptyFirstLine = EOL + validHeader;
	private String validHeaderWithBlankFirstLineContent = "This is a test with blank first line in header.";
	private String validHeaderWithBlankFirstLine = "   " + EOL + validHeader;
	private String validHeaderWithEmptyRandomLineContent = "This is a test with empty random line in header.";
	private String validHeaderWithEmptyRandomLine = validHeader.substring(0, pos)
			+ EOL + validHeader.substring(pos + 1);
	private String validHeaderWithBlankRandomLineContent = "This is a test with blank random line in header.";
	private String validHeaderWithBlankRandomLine = validHeader.substring(0, pos)
			+ "   " + EOL + validHeader.substring(pos + 1);
	
	private String continuedTitle = "This is a Title = This is a valid Title";
	private String validHeaderWithSpacesAroundEqualContent = "This is a test with spaces around equals in header.";
	private String validHeaderWithSpacesAroundEqual = "title = " + continuedTitle + EOL 
			+ "status    =draft" + EOL 
			+ "type=    post"+ EOL 
			+ "date    =  2013-09-02"+ EOL 
			+ END_OF_HEADER;
	
	private String validHeaderWithContinuedLineContent = "This is a test with continued line in header.";
	private List<String> tagsSample = Arrays.asList(new String[] {"Concurso Público", "Database", "dbconsole", "oracle"});
	private String tagsEntryValue = StringUtils.join(tagsSample, ",");
	private String validHeaderWithContinuedLine = "title = " 
	        + CONTINUED_LINE_STARTER + continuedTitle + EOL 
			+ "status    =draft" + EOL 
			+ "type=    post" + EOL 
			+ "date    =  2013-09-02" + EOL 
			+ "tags=" + EOL 
			+ CONTINUED_LINE_STARTER + StringUtils.join(tagsSample, ',' + EOL + CONTINUED_LINE_STARTER) + EOL 
			+ END_OF_HEADER;
	
	@Before
	public void createSampleFile() throws Exception {
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
		out.println("= Hello: AsciiDoc!");
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
		out.println(END_OF_HEADER);
		out.println("----");
		out.close();
		
		validAsciiDocFileWithEmptyFirstLineInHeader = folder.newFile("validAsciiDocFileWithEmptyFirstLineInHeader.ad");
		out = new PrintWriter(validAsciiDocFileWithEmptyFirstLineInHeader);
		out.println(validHeaderWithEmptyFirstLine);
		out.println("<p>" + validHeaderWithEmptyFirstLineContent + "</p>");
		out.close();
		
		validAsciiDocFileWithBlankFirstLineInHeader = folder.newFile("validAsciiDocFileWithBlankFirstLineInHeader.ad");
		out = new PrintWriter(validAsciiDocFileWithBlankFirstLineInHeader);
		out.println(validHeaderWithBlankFirstLine);
		out.println("<p>" + validHeaderWithBlankFirstLineContent + "</p>");
		out.close();
		
		validAsciiDocFileWithEmptyRandomLineInHeader = folder.newFile("validAsciiDocFileWithEmptyRandomLineInHeader.ad");
		out = new PrintWriter(validAsciiDocFileWithEmptyRandomLineInHeader);
		out.println(validHeaderWithEmptyRandomLine);
		out.println("<p>" + validHeaderWithEmptyRandomLineContent + "</p>");
		out.close();
		
		validAsciiDocFileWithBlankRandomLineInHeader = folder.newFile("validAsciiDocFileWithBlankRandomLineInHeader.ad");		
		out = new PrintWriter(validAsciiDocFileWithBlankRandomLineInHeader);
		out.println(validHeaderWithBlankRandomLine);
		out.println("<p>" + validHeaderWithBlankRandomLineContent + "</p>");
		out.close();
		
		validAsciiDocFileWithSpacesAroundEqualInHeader = folder.newFile("validAsciiDocFileWithSpacesAroundEqualInHeader.ad");		
		out = new PrintWriter(validAsciiDocFileWithSpacesAroundEqualInHeader);
		out.println(validHeaderWithSpacesAroundEqual);
		out.println("<p>" + validHeaderWithSpacesAroundEqualContent + "</p>");
		out.close();
		
		validAsciiDocFileWithContinuedLineInHeader = folder.newFile("validAsciiDocFileWithContinuedLineInHeader.ad");		
		out = new PrintWriter(validAsciiDocFileWithContinuedLineInHeader);
		out.println(validHeaderWithContinuedLine);
		out.println("<p>" + validHeaderWithContinuedLineContent + "</p>");
		out.close();
		
	}
	
	@Test
	public void parseValidHTMLFile() {
		Map<String, Object> map = parser.processFile(validHTMLFile);
		Assert.assertNotNull(map);
		Assert.assertEquals("draft", map.get(status.name()));
		Assert.assertEquals("post", map.get(type.name()));
		Assert.assertEquals("This is a Title = This is a valid Title", map.get("title"));
		Assert.assertNotNull(map.get(date.name()));
		Calendar cal = Calendar.getInstance();
		cal.setTime((Date) map.get(date.name()));
		Assert.assertEquals(8, cal.get(Calendar.MONTH));
		Assert.assertEquals(2, cal.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(2013, cal.get(Calendar.YEAR));

	}
	
	@Test
	public void parseInvalidHTMLFile() {
		Map<String, Object> map = parser.processFile(invalidHTMLFile);
		Assert.assertNull(map);
	}
	
	@Test
	public void parseValidAsciiDocFile() {
		Map<String, Object> map = parser.processFile(validAsciiDocFile);
		Assert.assertNotNull(map);
		Assert.assertEquals("draft", map.get(status.name()));
		Assert.assertEquals("post", map.get(type.name()));
		assertThat(map.get(body.name()).toString())
			.contains("class=\"paragraph\"")
			.contains("<p>JBake now supports AsciiDoc.</p>");
//		Assert.assertEquals("<div id=\"preamble\">\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>JBake now supports AsciiDoc.</p>\n</div>\n</div>\n</div>", map.get(body.name()));
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
		Assert.assertEquals("Hello: AsciiDoc!", map.get("title"));
		Assert.assertEquals("published", map.get(status.name()));
		Assert.assertEquals("page", map.get(type.name()));
		assertThat(map.get(body.name()).toString())
			.contains("class=\"paragraph\"")
			.contains("<p>JBake now supports AsciiDoc.</p>");
//		Assert.assertEquals("<div id=\"preamble\">\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>JBake now supports AsciiDoc.</p>\n</div>\n</div>\n</div>", map.get(body.name()));
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
		Assert.assertEquals("published", map.get(status.name()));
		Assert.assertEquals("page", map.get(type.name()));
		assertThat(map.get(body.name()).toString())
			.contains("class=\"paragraph\"")
			.contains("<p>JBake now supports AsciiDoc.</p>")
			.contains("class=\"listingblock\"")
			.contains("class=\"content\"")
			.contains("<pre>")
			.contains("title=Example Header")
			.contains("date=2013-02-01")
			.contains("tags=tag1, tag2");
//		Assert.assertEquals("<div id=\"preamble\">\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>JBake now supports AsciiDoc.</p>\n</div>\n<div class=\"listingblock\">\n<div class=\"content\">\n<pre>title=Example Header\ndate=2013-02-01\ntype=post\ntags=tag1, tag2\nstatus=published\n~~~~~~</pre>\n</div>\n</div>\n</div>\n</div>", map.get(body.name()));
	}

	@Test
	public void parseValidAsciiDocFileWithEmptyFirstLineInHeader() {
		Map<String, Object> map = parser.processFile(validAsciiDocFileWithEmptyFirstLineInHeader);
		Assert.assertNotNull(map);
		assertThat(map.get(body.name()).toString())
			.contains(validHeaderWithEmptyFirstLineContent);
	}
	
	@Test
	public void parseValidAsciiDocFileWithBlankFirstLineInHeader() {
		Map<String, Object> map = parser.processFile(validAsciiDocFileWithBlankFirstLineInHeader);
		Assert.assertNotNull(map);
		assertThat(map.get(body.name()).toString())
			.contains(validHeaderWithBlankFirstLineContent);
	}
	
	@Test
	public void parseValidAsciiDocFileWithEmptyRandomLineInHeader() {
		Map<String, Object> map = parser.processFile(validAsciiDocFileWithEmptyRandomLineInHeader);
		Assert.assertNotNull(map);
		assertThat(map.get(body.name()).toString())
			.contains(validHeaderWithEmptyRandomLineContent);
	}
	
	@Test
	public void parseValidAsciiDocFileWithBlankRandomLineInHeader() {
		Map<String, Object> map = parser.processFile(validAsciiDocFileWithBlankRandomLineInHeader);
		Assert.assertNotNull(map);
		assertThat(map.get(body.name()).toString())
			.contains(validHeaderWithBlankRandomLineContent);
	}
	
	@Test
	public void parseValidAsciiDocFileWithSpacesAroundEqualInHeader() {
		Map<String, Object> map = parser.processFile(validAsciiDocFileWithSpacesAroundEqualInHeader);
		Assert.assertNotNull(map);
		assertThat(map.get(body.name()).toString())
			.contains(validHeaderWithSpacesAroundEqualContent);
	}
	
	@Test
	public void parseValidAsciiDocFileWithContinuedLineInHeader() {
		Map<String, Object> map = parser.processFile(validAsciiDocFileWithContinuedLineInHeader);
		Assert.assertNotNull(map);
		Assert.assertEquals(continuedTitle, map.get("title"));
		List<String> headerTagsValues = Arrays.asList((String[]) map.get(tags.name()));
		Assert.assertEquals(tagsEntryValue, StringUtils.join(headerTagsValues, ","));
		assertThat(map.get(body.name()).toString())
			.contains(validHeaderWithContinuedLineContent);
	}
	
}
