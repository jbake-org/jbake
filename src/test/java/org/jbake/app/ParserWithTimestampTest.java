package org.jbake.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ParserWithTimestampTest {

	private static final String TIMESTAMP_KEY = "timestamp";

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	public CompositeConfiguration config;
	public Parser parser;
	private File rootPath;
	
	private File validHTMLFile;
	
	private String validHeader = "title=This is a Title\nstatus=draft\ntype=post\ndate=2013-09-02\n~~~~~~";
	private String invalidHeader = "title=This is a Title\n~~~~~~";

	/**
	 * Adds to default config an in-memory config. Useful for tests where additional values are set.
	 * @param path path of config to load from system
	 * @param values map of additional config infos
	 * @return a composite config merging all these infos
	 * @throws ConfigurationException
	 */
	public static CompositeConfiguration load(File path, Map<String, Object> values) throws ConfigurationException {
		CompositeConfiguration config = ConfigUtil.load(path);
		config.addConfiguration(new MapConfiguration(values));
		return config;
	}
  
	
	@Before
	public void createSampleFile() throws Exception {
		rootPath = new File(this.getClass().getResource(".").getFile());
		config = load(rootPath, new TreeMap<String, Object>() {{ put(Parser.BAKED_TIMESTAMP, TIMESTAMP_KEY); }});
		parser = new Parser(config,rootPath.getPath());
		
		validHTMLFile = folder.newFile("valid.html");
		PrintWriter out = new PrintWriter(validHTMLFile);
		out.println(validHeader);
		out.println("<p>This is a test.</p>");
		out.close();
		
	}
	
	@Test
	public void parseValidHTMLFile() {
		Map<String, Object> map = parser.processFile(validHTMLFile);
		Assert.assertNotNull(map);
		Assert.assertEquals("draft", map.get("status"));
		Assert.assertEquals("post", map.get("type"));
		Assert.assertNotNull(map.get("date"));
		Calendar cal = Calendar.getInstance();
		cal.setTime((Date) map.get("date"));
		Assert.assertEquals(8, cal.get(Calendar.MONTH));
		Assert.assertEquals(2, cal.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(2013, cal.get(Calendar.YEAR));

		Assert.assertNotNull(map.get(TIMESTAMP_KEY));
	}
}
