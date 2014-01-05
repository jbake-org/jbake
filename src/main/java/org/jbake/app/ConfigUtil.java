package org.jbake.app;

import java.io.File;

import javax.inject.Singleton;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
@Singleton
public class ConfigUtil {
	
	public final static String DATE_FORMAT = "date.format";
	
	private CompositeConfiguration config;
	
	public CompositeConfiguration load(File source) throws ConfigurationException {
		if (config == null) {
			config = new CompositeConfiguration();
			config.setListDelimiter(',');
			File customConfigFile = new File(source, "custom.properties");
			if (customConfigFile.exists()) {
				config.addConfiguration(new PropertiesConfiguration(customConfigFile));
			}
			customConfigFile = new File(source, "jbake.properties");
			if (customConfigFile.exists()) {
				config.addConfiguration(new PropertiesConfiguration(customConfigFile));
			}
			config.addConfiguration(new PropertiesConfiguration("default.properties"));
		}
		return config;
	}
	
	public void reset() {
		config = null;
	}
}
