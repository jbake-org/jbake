package org.jbake.app.configuration;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.jbake.app.JBakeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class ConfigUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);
    private final static String LEGACY_CONFIG_FILE = "custom.properties";
    private final static String CONFIG_FILE = "jbake.properties";
    private final static String DEFAULT_CONFIG_FILE = "default.properties";
    private static boolean LEGACY_CONFIG_FILE_WARNING_SHOWN = false;
    private static boolean LEGACY_CONFIG_FILE_EXISTS = false;

    private CompositeConfiguration load(File source) throws ConfigurationException {

        if (!source.exists()) {
            throw new JBakeException("The given source folder '" + source.getAbsolutePath() + "' does not exist.");
        }
        if (!source.isDirectory()) {
            throw new JBakeException("The given source folder is not a directory.");
        }

        CompositeConfiguration config = new CompositeConfiguration();
        config.setListDelimiter(',');
        File customConfigFile = new File(source, LEGACY_CONFIG_FILE);
        if (customConfigFile.exists()) {
        	LEGACY_CONFIG_FILE_EXISTS = true;
            config.addConfiguration(new PropertiesConfiguration(customConfigFile));
        }
        customConfigFile = new File(source, CONFIG_FILE);
        if (customConfigFile.exists()) {
            config.addConfiguration(new PropertiesConfiguration(customConfigFile));
        }
        config.addConfiguration(new PropertiesConfiguration(DEFAULT_CONFIG_FILE));
        config.addConfiguration(new SystemConfiguration());
        return config;
    }

    public static void displayLegacyConfigFileWarningIfRequired() {
    	if (LEGACY_CONFIG_FILE_EXISTS) {
        	if (!LEGACY_CONFIG_FILE_WARNING_SHOWN) {
	        	LOGGER.warn(String.format("You have defined a part of your JBake configuration in %s", LEGACY_CONFIG_FILE));
	        	LOGGER.warn(String.format("Usage of this file is being deprecated, please rename this file to: %s to remove this warning", CONFIG_FILE));
	        	LEGACY_CONFIG_FILE_WARNING_SHOWN = true;
        	}
    	}
    }

    public JBakeConfiguration loadConfig(File source) throws ConfigurationException {
        CompositeConfiguration configuration = load(source);
        DefaultJBakeConfiguration jBakeConfiguration = new DefaultJBakeConfiguration(source, configuration);
        jBakeConfiguration.setSourceFolder(source);

        return jBakeConfiguration;
    }
}
