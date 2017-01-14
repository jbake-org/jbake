package org.jbake.app;

import java.io.File;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigUtilTest {

	@Test
	public void load() throws Exception {
		CompositeConfiguration config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
		
		// check default.properties values exist
		assertEquals("output", config.getString(Keys.DESTINATION_FOLDER));
		
		// check custom.properties values exist
		assertEquals("testing123", config.getString("test.property"));

		assertEquals("http://www.jbake.org", config.getString(Keys.SITE_HOST));
	}

	@Test
	public void shouldHaveSiteConfiguredWhenServerRunning() throws Exception {
		CompositeConfiguration config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()), true);

		assertEquals("http://localhost:8820", config.getString(Keys.SITE_HOST));
	}
}
