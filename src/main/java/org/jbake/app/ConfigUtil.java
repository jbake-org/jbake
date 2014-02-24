package org.jbake.app;

import java.io.File;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class ConfigUtil {

    public final static String DATE_FORMAT = "date.format";
    public final static String URI_NO_EXTENSION = "uri.noExtension";

    public static CompositeConfiguration load(File source) throws ConfigurationException {
        CompositeConfiguration config = new CompositeConfiguration();
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
        return config;
    }
}
