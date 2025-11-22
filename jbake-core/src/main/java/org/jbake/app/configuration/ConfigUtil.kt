package org.jbake.app.configuration

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.SystemConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler
import org.apache.commons.configuration2.ex.ConfigurationException
import org.jbake.app.JBakeException
import org.jbake.launcher.SystemExit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.charset.Charset

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
 */
class ConfigUtil {
    var encoding: String = DEFAULT_ENCODING
        private set

    @Throws(ConfigurationException::class)
    private fun load(source: File, propertiesFile: File?): CompositeConfiguration {
        if (!source.exists()) {
            throw JBakeException(
                SystemExit.CONFIGURATION_ERROR,
                "The given source folder '" + source.getAbsolutePath() + "' does not exist."
            )
        }
        if (!source.isDirectory()) {
            throw JBakeException(SystemExit.CONFIGURATION_ERROR, "The given source folder is not a directory.")
        }

        val legacyConfigFile = File(source, LEGACY_CONFIG_FILE)
        val customConfigFile = if (propertiesFile != null) propertiesFile else File(source, CONFIG_FILE)

        val config = CompositeConfiguration()
        config.setListDelimiterHandler(DefaultListDelimiterHandler(LIST_DELIMITER))

        if (legacyConfigFile.exists()) {
            displayLegacyConfigFileWarningIfRequired()
            config.addConfiguration(getFileBasedPropertiesConfiguration(legacyConfigFile))
        }
        if (customConfigFile.exists()) {
            config.addConfiguration(getFileBasedPropertiesConfiguration(customConfigFile))
        }
        val defaultPropertiesLocation = this.javaClass.getClassLoader().getResource(DEFAULT_CONFIG_FILE)
        if (defaultPropertiesLocation != null) {
            config.addConfiguration(getFileBasedPropertiesConfiguration(defaultPropertiesLocation))
        }

        config.addConfiguration(SystemConfiguration())
        return config
    }

    @Throws(ConfigurationException::class)
    private fun getFileBasedPropertiesConfiguration(propertiesFile: File?): PropertiesConfiguration? {
        val builder =
            FileBasedConfigurationBuilder(PropertiesConfiguration::class.java)
                .configure(
                    Parameters().properties()
                        .setFile(propertiesFile)
                        .setEncoding(encoding)
                        .setThrowExceptionOnMissing(true)
                        .setListDelimiterHandler(DefaultListDelimiterHandler(LIST_DELIMITER))
                        .setIncludesAllowed(false)
                )
        return builder.getConfiguration()
    }

    @Throws(ConfigurationException::class)
    private fun getFileBasedPropertiesConfiguration(propertiesFile: URL?): PropertiesConfiguration? {
        val builder =
            FileBasedConfigurationBuilder(PropertiesConfiguration::class.java)
                .configure(
                    Parameters().properties()
                        .setURL(propertiesFile)
                        .setEncoding(encoding)
                        .setThrowExceptionOnMissing(true)
                        .setListDelimiterHandler(DefaultListDelimiterHandler(LIST_DELIMITER))
                        .setIncludesAllowed(false)
                )
        return builder.getConfiguration()
    }

    private fun displayLegacyConfigFileWarningIfRequired() {
        log.warn("You have defined a part of your JBake configuration in {}", LEGACY_CONFIG_FILE)
        log.warn(
            "Usage of this file is being deprecated, please rename this file to: {} to remove this warning",
            CONFIG_FILE
        )
    }

    /**
     * Load a configuration.
     *
     * @param source the source directory of the project
     * @param propertiesFile the properties file for the project
     * @return the configuration
     * @throws JBakeException if unable to configure
     */
    @Throws(JBakeException::class)
    fun loadConfig(source: File, propertiesFile: File?): JBakeConfiguration {
        try {
            val configuration = load(source, propertiesFile)
            return DefaultJBakeConfiguration(source, configuration)
        } catch (e: ConfigurationException) {
            throw JBakeException(SystemExit.CONFIGURATION_ERROR, e.message, e)
        }
    }

    /**
     * Load a configuration.
     *
     * @param source the source directory of the project
     * @return the configuration
     * @throws ConfigurationException if unable to configure
     */
    @Deprecated("use {@link #loadConfig(File, File)} instead")
    @Throws(ConfigurationException::class)
    fun loadConfig(source: File): JBakeConfiguration {
        return loadConfig(source, null)
    }

    fun setEncoding(encoding: String): ConfigUtil {
        if (Charset.isSupported(encoding)) {
            this.encoding = encoding
        } else {
            this.encoding = DEFAULT_ENCODING
            log.warn("Unsupported encoding '{}'. Using default encoding '{}'", encoding, this.encoding)
        }
        return this
    }

    companion object {
        const val LIST_DELIMITER: Char = ','
        const val DEFAULT_ENCODING: String = "UTF-8"
        private val log: Logger = LoggerFactory.getLogger(ConfigUtil::class.java)
        const val LEGACY_CONFIG_FILE: String = "custom.properties"
        const val CONFIG_FILE: String = "jbake.properties"
        const val DEFAULT_CONFIG_FILE: String = "default.properties"
    }
}
