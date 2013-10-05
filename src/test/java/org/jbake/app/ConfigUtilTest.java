package org.jbake.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ConfigUtilTest {

	@Test
	public void load() throws Exception {
		ConfigUtil.reset();
		CompositeConfiguration config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
		
		// check default.properties values exist
		Assert.assertEquals("output", config.getString("destination.folder"));	
		
		// check custom.properties values exist
		Assert.assertEquals("testing123", config.getString("test.property"));
	}
}
