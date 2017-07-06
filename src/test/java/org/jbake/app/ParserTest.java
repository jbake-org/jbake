package org.jbake.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.assertj.core.util.Arrays;
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
    private File validAsciiDocFileWithoutJBakeMetaData;

	private String validHeader = "title=This is a Title = This is a valid Title\nstatus=draft\ntype=post\ndate=2013-09-02\n~~~~~~";
	private String invalidHeader = "title=This is a Title\n~~~~~~";



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
		out.println("~~~~~~");
		out.println("----");
		out.close();

        validAsciiDocFileWithoutJBakeMetaData = folder.newFile("validwojbakemetadata.ad");
        out = new PrintWriter(validAsciiDocFileWithoutJBakeMetaData);
        out.println("= Hello: AsciiDoc!");
        out.println("Test User <user@test.org>");
        out.println("2013-09-02");
        out.println("");
        out.println("JBake now supports AsciiDoc documents without JBake meta data.");
        out.close();
	}
	
	public Map<String, String> getCommonTestPostData(){
		Map<String, String> data = new HashMap<String, String>();
		data.put("title", "This is a test post");
		data.put("type", "post");
		data.put("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		data.put("tags", "tagA,tagB");
		data.put("status", "published");
		data.put("body", "This content is for body of the post.");
		return data;
	}
	public String convertMapToStringHeader(Map<String,String> data){
		if (data == null) return null;
		StringBuffer header = new StringBuffer();
		Map<String, String> lockedMap =  Collections.unmodifiableMap(data);
		for (String key : lockedMap.keySet()) {
			if(key != "body")
				header.append(key).append("=").append(lockedMap.get(key)).append("\n");
		}
		if(!lockedMap.isEmpty()){
			header.append("~~~~~~\n");
		}
		if(lockedMap.containsKey("body")){
			header.append(lockedMap.get("body"));
		}
		return header.toString();
	}
	
	@Test
	public void parseValidHTMLFile() {
		Map<String, Object> map = parser.processFile(validHTMLFile);
		Assert.assertNotNull(map);
		Assert.assertEquals("draft", map.get("status"));
		Assert.assertEquals("post", map.get("type"));
		Assert.assertEquals("This is a Title = This is a valid Title", map.get("title"));
		Assert.assertNotNull(map.get("date"));
		Calendar cal = Calendar.getInstance();
		cal.setTime((Date) map.get("date"));
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
		Assert.assertEquals("draft", map.get("status"));
		Assert.assertEquals("post", map.get("type"));
		assertThat(map.get("body").toString())
			.contains("class=\"paragraph\"")
			.contains("<p>JBake now supports AsciiDoc.</p>");
//		Assert.assertEquals("<div id=\"preamble\">\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>JBake now supports AsciiDoc.</p>\n</div>\n</div>\n</div>", map.get("body"));
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
		Assert.assertEquals("published", map.get("status"));
		Assert.assertEquals("page", map.get("type"));
		assertThat(map.get("body").toString())
			.contains("class=\"paragraph\"")
			.contains("<p>JBake now supports AsciiDoc.</p>");
//		Assert.assertEquals("<div id=\"preamble\">\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>JBake now supports AsciiDoc.</p>\n</div>\n</div>\n</div>", map.get("body"));
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
		assertThat(map.get("body").toString())
			.contains("class=\"paragraph\"")
			.contains("<p>JBake now supports AsciiDoc.</p>")
			.contains("class=\"listingblock\"")
			.contains("class=\"content\"")
			.contains("<pre>")
			.contains("title=Example Header")
			.contains("date=2013-02-01")
			.contains("tags=tag1, tag2");
//		Assert.assertEquals("<div id=\"preamble\">\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>JBake now supports AsciiDoc.</p>\n</div>\n<div class=\"listingblock\">\n<div class=\"content\">\n<pre>title=Example Header\ndate=2013-02-01\ntype=post\ntags=tag1, tag2\nstatus=published\n~~~~~~</pre>\n</div>\n</div>\n</div>\n</div>", map.get("body"));
	}

    @Test
    public void parseValidAsciiDocFileWithoutJBakeMetaDataUsingDefaultTypeAndStatus() throws ConfigurationException {
        CompositeConfiguration defaultConfig = ConfigUtil.load(rootPath);
        defaultConfig.addProperty(ConfigUtil.Keys.DEFAULT_STATUS, "published");
        defaultConfig.addProperty(ConfigUtil.Keys.DEFAULT_TYPE, "page");
        Parser parser = new Parser(defaultConfig,rootPath.getPath());
        Map<String, Object> map = parser.processFile(validAsciiDocFileWithoutJBakeMetaData);
        Assert.assertNotNull(map);
        Assert.assertEquals("published", map.get("status"));
        Assert.assertEquals("page", map.get("type"));
        assertThat(map.get("body").toString())
                .contains("<p>JBake now supports AsciiDoc documents without JBake meta data.</p>");
    }
    
    	@Test
	public void parseCategoriesCheckDefaultCategory() throws IOException{
		File tempHtml = folder.newFile("test.html");
		PrintWriter out = new PrintWriter(tempHtml);
		out.print(convertMapToStringHeader(getCommonTestPostData()));
		out.close();
		Map<String, Object> map = parser.processFile(tempHtml);
		assertThat(map.get("categories"))
					.isNotNull()
					.isInstanceOf(String[].class)
					.isEqualTo(Arrays.array("Uncategorized"));
		
	}
	
	@Test
	public void parseCategoriesCheckNoDefaultCategory() throws IOException{
		File tempHtml = folder.newFile("test.html");
		PrintWriter out = new PrintWriter(tempHtml);
		out.print(convertMapToStringHeader(getCommonTestPostData()));
		out.close();
		config.setProperty("categories.enable", false);
		parser = new Parser(config,rootPath.getPath());
		Map<String, Object> map = parser.processFile(tempHtml);
		assertThat(map.get("categories"))
					.isNull();
	}
	
	@Test
	public void parseCategoriesCheckSpecificDefaultCategoryWithSanitize() throws IOException{
		File tempHtml = folder.newFile("test.html");
		BufferedWriter out = new BufferedWriter(new FileWriter(tempHtml));
		out.write(convertMapToStringHeader(getCommonTestPostData()));
		out.flush();
		out.close();
		config.setProperty("category.default", "Java Collections");
		config.setProperty("categories.sanitize", true);
		parser = new Parser(config,rootPath.getPath());
		Map<String, Object> map = parser.processFile(tempHtml);
		assertThat(map.get("categories"))
					.isNotNull()
					.isInstanceOf(String[].class)
					.isEqualTo(Arrays.array("Java-Collections"));
		
	}
	
	@Test
	public void parseCategoriesCheckMultipleCategories() throws IOException{
		File tempHtml = folder.newFile("test.html");
		PrintWriter out = new PrintWriter(tempHtml);
		Map<String,String> data = getCommonTestPostData();
		data.put("categories", "Java, J2EE, Spring Framework");
		out.print(convertMapToStringHeader(data));
		out.close();
		parser = new Parser(config,rootPath.getPath());
		Map<String, Object> map = parser.processFile(tempHtml);
		assertThat(map.get("categories"))
					.isNotNull()
					.isInstanceOf(String[].class)
					.isEqualTo(Arrays.array("Java","J2EE","Spring Framework"));
	}
	@Test
	public void parseCategoriesCheckMultipleCategoriesWithSanitize() throws IOException{
		File tempHtml = folder.newFile("test.html");
		PrintWriter out = new PrintWriter(tempHtml);
		Map<String,String> data = getCommonTestPostData();
		data.put("categories", "Java, J2EE, Spring Framework");
		out.print(convertMapToStringHeader(data));
		out.close();
		config.setProperty("categories.sanitize", true);
		parser = new Parser(config,rootPath.getPath());
		Map<String, Object> map = parser.processFile(tempHtml);
		assertThat(map.get("categories"))
					.isNotNull()
					.isInstanceOf(String[].class)
					.isEqualTo(Arrays.array("Java","J2EE","Spring-Framework"));
	}
}
