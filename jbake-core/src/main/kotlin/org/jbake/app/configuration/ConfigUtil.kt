package org.jbake.app.configuration

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.SystemConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler
import org.apache.commons.configuration2.ex.ConfigurationException
import org.jbake.app.JBakeExitException
import org.jbake.app.SystemExit.CONFIG_ERROR
import org.jbake.util.Logging
import org.jbake.util.Logging.logger
import org.slf4j.Logger
import java.io.File
import java.net.URL
import java.nio.charset.Charset

/**
 * Provides Configuration related functions.
 */
class ConfigUtil {
    var encoding: String = DEFAULT_ENCODING
        private set

    @Throws(ConfigurationException::class)
    private fun load(sourceDir: File, propertiesFile: File?): CompositeConfiguration {
        if (!sourceDir.exists())
            throw JBakeExitException(CONFIG_ERROR, "The given source dir '" + sourceDir.absolutePath + "' does not exist.")
        if (!sourceDir.isDirectory)
            throw JBakeExitException(CONFIG_ERROR, "The given source dir is not a directory.")

        val legacyConfigFile = File(sourceDir, LEGACY_CONFIG_FILE)
        val customConfigFile = propertiesFile ?: File(sourceDir, CONFIG_FILE)

        val config = CompositeConfiguration().apply {
            listDelimiterHandler = DefaultListDelimiterHandler(LIST_DELIMITER)
        }

        if (legacyConfigFile.exists()) {
            log.warn("Part of your JBake configuration is in a deprecated '$LEGACY_CONFIG_FILE'. Rename it to '$CONFIG_FILE'.")
            config.addConfiguration(getFileBasedPropertiesConfiguration(legacyConfigFile))
        }
        if (customConfigFile.exists())
            config.addConfiguration(getFileBasedPropertiesConfiguration(customConfigFile))

        val defaultPropertiesLocation = this.javaClass.classLoader.getResource(DEFAULT_CONFIG_FILE)
        defaultPropertiesLocation?.let {
            config.addConfiguration(getFileBasedPropertiesConfiguration(it))
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

    @Throws(JBakeExitException::class)
    fun loadConfig(source: File, propertiesFile: File? = null): JBakeConfiguration {
        try {
            val configuration = load(source, propertiesFile)
            return DefaultJBakeConfiguration(source, configuration)
        } catch (e: ConfigurationException) {
            throw JBakeExitException(CONFIG_ERROR, e.message ?: "Failed loading config from ${source.path} and props ${propertiesFile?.path}", e)
        }
    }

    fun setEncoding(encoding: String): ConfigUtil {
        if (Charset.isSupported(encoding)) {
            this.encoding = encoding
        } else {
            this.encoding = DEFAULT_ENCODING
            log.warn("Unsupported encoding '$encoding'. Using default encoding '$this.encoding'.")
        }
        return this
    }

    companion object {
        const val LIST_DELIMITER: Char = ','
        const val DEFAULT_ENCODING: String = "UTF-8"
        private val log: Logger by Logging.logger()
        const val LEGACY_CONFIG_FILE: String = "custom.properties"
        const val CONFIG_FILE: String = "jbake.properties"
        const val DEFAULT_CONFIG_FILE: String = "default.properties"
    }
}
