package org.jbake.app.configuration

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.JBakeException
import java.io.File


/**
 * A [JBakeConfiguration] factory
 */
class JBakeConfigurationFactory {

    @JvmField var configUtil: ConfigUtil = ConfigUtil()

    /**
     * Creates a [DefaultJBakeConfiguration] using default.properties and jbake.properties
     *
     * @param sourceFolder The source folder of the project
     * @param isClearCache Whether to clear database cache or not
     * @return A configuration by given parameters
     * @throws JBakeException if loading the configuration fails
     */
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(
        sourceFolder: File,
        destination: File,
        isClearCache: Boolean
    ): DefaultJBakeConfiguration {
        return createDefaultJbakeConfiguration(sourceFolder, destination, null as File?, isClearCache)
    }

    /**
     * Creates a [DefaultJBakeConfiguration]
     * @param sourceFolder The source folder of the project
     * @param propertiesFile The properties file for the project
     * @param isClearCache Whether to clear database cache or not
     * @return A configuration by given parameters
     * @throws JBakeException if loading the configuration fails
     */
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(
        sourceFolder: File,
        destination: File,
        propertiesFile: File?,
        isClearCache: Boolean,
    ): DefaultJBakeConfiguration {
        val configuration = this.configUtil.loadConfig(sourceFolder, propertiesFile) as DefaultJBakeConfiguration
        configuration.destinationFolder = destination
        configuration.clearCache = isClearCache
        return configuration
    }

    /**
     * Creates a [DefaultJBakeConfiguration]. This is a compatibility factory method.
     * @param isClearCache Whether to clear database cache or not
     */
    @Deprecated("use {@link #createDefaultJbakeConfiguration(File, File, File, boolean)} instead")
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(
        sourceFolder: File,
        destination: File,
        compositeConfiguration: CompositeConfiguration,
        isClearCache: Boolean
    )
        = DefaultJBakeConfiguration(sourceFolder, compositeConfiguration).apply {
            destinationFolder = destination
            clearCache = isClearCache
        }

    /**
     * Creates a [DefaultJBakeConfiguration]. This is a compatibility factory method.
     */
    @Deprecated("use {@link #createDefaultJbakeConfiguration(File, File, File, boolean)} instead")
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(
        sourceFolder: File,
        destination: File,
        compositeConfiguration: CompositeConfiguration
    )
        = DefaultJBakeConfiguration(sourceFolder, compositeConfiguration).apply { destinationFolder = destination }

    /**
     * Creates a [DefaultJBakeConfiguration]
     */
    @Deprecated("")
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(sourceFolder: File, config: CompositeConfiguration)
        = DefaultJBakeConfiguration(sourceFolder, config)

    /**
     * Creates a [DefaultJBakeConfiguration] with value site.host replaced
     * by http://localhost:[server.port].
     * The server.port is read from the project properties file.
     *
     * @param destinationFolder The destination folder to render and copy files to
     * @param isClearCache Whether to clear database cache or not
     * @throws JBakeException if loading the configuration fails
     */
    @Deprecated("use {@link #createJettyJbakeConfiguration(File, File, File, boolean)} instead")
    @Throws(JBakeException::class)
    fun createJettyJbakeConfiguration(
        sourceFolder: File,
        destinationFolder: File,
        isClearCache: Boolean
    ): DefaultJBakeConfiguration {
        return createJettyJbakeConfiguration(sourceFolder, destinationFolder, null as File?, isClearCache)
    }

    /**
     * Creates a [DefaultJBakeConfiguration] with value site.host replaced
     * by http://localhost:[server.port].
     * The server.port is read from the project properties file.
     *
     * @param destinationFolder The destination folder to render and copy files to
     * @param propertiesFile The properties file for the project
     * @param isClearCache Whether to clear database cache or not
     */
    @Throws(JBakeException::class)
    fun createJettyJbakeConfiguration(
        sourceFolder: File,
        destinationFolder: File?,
        propertiesFile: File?,
        isClearCache: Boolean
    ): DefaultJBakeConfiguration {
        val conf = this.configUtil.loadConfig(sourceFolder, propertiesFile) as DefaultJBakeConfiguration
        conf.destinationFolder = destinationFolder ?: File("./output")
        conf.clearCache = isClearCache
        conf.siteHost = "http://" + conf.serverHostname + ":" + conf.serverPort + conf.serverContextPath
        return conf
    }

    fun setEncoding(charset: String) = this.apply { configUtil.setEncoding(charset) }
}
