package org.jbake.app;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.configuration.CompositeConfiguration;
import org.junit.Test;

public class ConfigUtilTest {

	@Test
	public void load() throws Exception {
		CompositeConfiguration config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
		
		// check default.properties values exist
		Assert.assertEquals("output", config.getString("destination.folder"));	
		
		// check custom.properties values exist
		Assert.assertEquals("testing123", config.getString("test.property"));
	}
}
