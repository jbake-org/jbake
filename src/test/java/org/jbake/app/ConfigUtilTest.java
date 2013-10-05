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
		
//		File path = new File(this.getClass().getResource("/").getFile());
//		System.out.println(path.getPath());
//		for (File file : path.listFiles()) {
//			if (file.getName().equalsIgnoreCase("custom.properties")) {
//				BufferedReader reader = new BufferedReader(new FileReader(file));
//				List<String> fileContents = IOUtils.readLines(reader);
//				for (String line : fileContents) {
//					System.out.println(line);
//				}
//			}
//			System.out.println(file.getPath());
//		}
		
		// check custom.properties values exist
		Assert.assertEquals("testing123", config.getString("test.property"));
	}
}
