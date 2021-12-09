package org.jbake.app.configuration;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jbake.exception.JBakeException;
import org.jbake.launcher.SystemExit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class ConfigUtil {

    public static final char LIST_DELIMITER = ',';
    public static final String DEFAULT_ENCODING = "UTF-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);
    public static final String LEGACY_CONFIG_FILE = "custom.properties";
    public static final String CONFIG_FILE = "jbake.properties";
    public static final String DEFAULT_CONFIG_FILE = "default.properties";
    private String encoding = DEFAULT_ENCODING;

    private CompositeConfiguration load(File source, File propertiesFile) throws ConfigurationException {

        if (!source.exists()) {
            throw new JBakeException(SystemExit.CONFIGURATION_ERROR, "The given source folder '" + source.getAbsolutePath() + "' does not exist.");
        }
        if (!source.isDirectory()) {
            throw new JBakeException(SystemExit.CONFIGURATION_ERROR,"The given source folder is not a directory.");
        }

        File legacyConfigFile = new File(source, LEGACY_CONFIG_FILE);
        File customConfigFile = propertiesFile != null ? propertiesFile : new File(source, CONFIG_FILE);

        CompositeConfiguration config = new CompositeConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(LIST_DELIMITER));

        if (legacyConfigFile.exists()) {
            displayLegacyConfigFileWarningIfRequired();
            config.addConfiguration(getFileBasedPropertiesConfiguration(legacyConfigFile));
        }
        if (customConfigFile.exists()) {
            config.addConfiguration(getFileBasedPropertiesConfiguration(customConfigFile));
        }
        URL defaultPropertiesLocation = this.getClass().getClassLoader().getResource(DEFAULT_CONFIG_FILE);
        if (defaultPropertiesLocation != null) {
            config.addConfiguration(getFileBasedPropertiesConfiguration(defaultPropertiesLocation));
        }

        config.addConfiguration(new SystemConfiguration());
        return config;
    }

    private PropertiesConfiguration getFileBasedPropertiesConfiguration(File propertiesFile) throws ConfigurationException {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
            new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(new Parameters().properties()
                    .setFile(propertiesFile)
                    .setEncoding(encoding)
                    .setThrowExceptionOnMissing(true)
                    .setListDelimiterHandler(new DefaultListDelimiterHandler(LIST_DELIMITER))
                    .setIncludesAllowed(false));
        return builder.getConfiguration();
    }

    private PropertiesConfiguration getFileBasedPropertiesConfiguration(URL propertiesFile) throws ConfigurationException {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
            new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(new Parameters().properties()
                    .setURL(propertiesFile)
                    .setEncoding(encoding)
                    .setThrowExceptionOnMissing(true)
                    .setListDelimiterHandler(new DefaultListDelimiterHandler(LIST_DELIMITER))
                    .setIncludesAllowed(false));
        return builder.getConfiguration();
    }

    private void displayLegacyConfigFileWarningIfRequired() {
        LOGGER.warn("You have defined a part of your JBake configuration in {}", LEGACY_CONFIG_FILE);
        LOGGER.warn("Usage of this file is being deprecated, please rename this file to: {} to remove this warning", CONFIG_FILE);
    }

    /**
     * Load a configuration.
     *
     * @param source the source directory of the project
     * @param propertiesFile the properties file for the project
     * @return the configuration
     * @throws JBakeException if unable to configure
     */
    public JBakeConfiguration loadConfig(File source, File propertiesFile) throws JBakeException {
        try {
            CompositeConfiguration configuration = load(source, propertiesFile);
            return new DefaultJBakeConfiguration(source, configuration);
        } catch (ConfigurationException e) {
            throw new JBakeException(SystemExit.CONFIGURATION_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Load a configuration.
     *
     * @param source the source directory of the project
     * @return the configuration
     * @throws ConfigurationException if unable to configure
     * @deprecated use {@link #loadConfig(File, File)} instead
     */
    @Deprecated
    public JBakeConfiguration loadConfig(File source) throws ConfigurationException {
        return loadConfig(source, null);
    }

    public String getEncoding() {
        return this.encoding;
    }

    public ConfigUtil setEncoding(String encoding) {
        if (Charset.isSupported(encoding)) {
            this.encoding = encoding;
        } else {
            this.encoding = DEFAULT_ENCODING;
            LOGGER.warn("Unsupported encoding '{}'. Using default encoding '{}'", encoding, this.encoding);
        }
        return this;
    }
}
