package org.jbake.app;

import junit.framework.Assert;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.utils.TestUtils;
import org.junit.Test;

public class ConfigUtilTest {

	@Test
	public void load() throws Exception {
		CompositeConfiguration config = TestUtils.loadTestConfig();
		
		// check default.properties values exist
		Assert.assertEquals("output", config.getString("destination.folder"));	
		
		// check custom.properties values exist
		Assert.assertEquals("testing123", config.getString("test.property"));
	}
}
