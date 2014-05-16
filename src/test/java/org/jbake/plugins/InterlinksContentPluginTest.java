package org.jbake.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.junit.Test;

public class InterlinksContentPluginTest {

	@Test
	public void test() throws ContentPluginException {

		CompositeConfiguration config = new CompositeConfiguration();
		config.addProperty("interlinks.coollink", "http://www.cool-link.org");

		String mdContent = "This is a [cool link](coollink>) that links to this site.";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("body", mdContent);

		String expected = "This is a [cool link](http://www.cool-link.org) that links to this site.";

		InterlinksContentPlugin plugin = new InterlinksContentPlugin();

		plugin.parseMarkdown(map, config);

		assertEquals(expected, map.get("body"));
	}

}
