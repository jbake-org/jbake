package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class ConfigUtil {

	private static final String NEW_CONFIG_FILE_NAME = "jbake.properties";

	private static final String OLD_CONFIG_FILE_NAME = "custom.properties";

	private final static Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    public final static String DATE_FORMAT = "date.format";

    public static CompositeConfiguration load(File source) throws ConfigurationException {
        CompositeConfiguration config = new CompositeConfiguration();
        config.setListDelimiter(',');
        File customConfigFile = new File(source, OLD_CONFIG_FILE_NAME);
        if (customConfigFile.exists()) {
        	LOGGER.warn(String.format("You defined a part of your JBake configuration through custom.properties file located "
        					+ "\nat \"%s\".\n"
        					+ "Usage of this file has been deprecated, according to https://github.com/jbake-org/jbake/issues/50.\n"
        					+ "Your configuration should be moved to a file named %s", 
        					customConfigFile.getAbsolutePath(),
        					NEW_CONFIG_FILE_NAME));
            config.addConfiguration(new PropertiesConfiguration(customConfigFile));
            File newConfigFile = new File(source, NEW_CONFIG_FILE_NAME);
            if(newConfigFile.exists()) {
            	LOGGER.warn(String.format("As a %s file already exists, we won't move the old %s file automatically. Please fix config.", NEW_CONFIG_FILE_NAME, OLD_CONFIG_FILE_NAME));
            } else {
            	File archiveFile = new File(source, "~"+OLD_CONFIG_FILE_NAME);
            	LOGGER.info(String.format("As no %s file exists, we will auto-rename old file to new file name.", NEW_CONFIG_FILE_NAME));
            	try {
	            	FileUtils.moveFile(customConfigFile, newConfigFile);
            	} catch(Exception e) {
            		LOGGER.debug("something went wrong while auto-moving "+OLD_CONFIG_FILE_NAME, e);
            	}
            }
        }
        customConfigFile = new File(source, NEW_CONFIG_FILE_NAME);
        if (customConfigFile.exists()) {
            config.addConfiguration(new PropertiesConfiguration(customConfigFile));
        }
        config.addConfiguration(new PropertiesConfiguration("default.properties"));
        return config;
    }
}
