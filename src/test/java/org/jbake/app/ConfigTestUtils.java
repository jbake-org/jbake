package org.jbake.app;

import java.io.File;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;

/**
 * Some (well, currently, only one) method for config management.
 * @author ndx
 *
 */
public class ConfigTestUtils {

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

}
