package org.jbake.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.MapConfiguration;

public class TestUtils {
	public static CompositeConfiguration loadTestConfig() throws IOException {
		Properties prop = new Properties();
        prop.load(TestUtils.class.getResourceAsStream("/config/custom.properties"));
        Properties dp = new Properties();
        dp.load(TestUtils.class.getResourceAsStream("/default.properties"));
		return new CompositeConfiguration(Arrays.asList( new MapConfiguration(dp), new MapConfiguration(prop) ));
	}
}
