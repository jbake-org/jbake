package org.jbake.app.configuration

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.JBakeException
import java.io.File


/**
 * A [JBakeConfiguration] factory
 */
class JBakeConfigurationFactory {
    @JvmField
    var configUtil: ConfigUtil

    init {
        this.configUtil = ConfigUtil()
    }

    /**
     * Creates a [DefaultJBakeConfiguration] using default.properties and jbake.properties
     *
     * @param sourceFolder The source folder of the project
     * @param destination The destination folder to render and copy files to
     * @param isClearCache Whether to clear database cache or not
     * @return A configuration by given parameters
     * @throws JBakeException if loading the configuration fails
     */
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(
        sourceFolder: File,
        destination: File?,
        isClearCache: Boolean
    ): DefaultJBakeConfiguration {
        return createDefaultJbakeConfiguration(sourceFolder, destination, null as File?, isClearCache)
    }

    /**
     * Creates a [DefaultJBakeConfiguration]
     * @param sourceFolder The source folder of the project
     * @param destination The destination folder to render and copy files to
     * @param propertiesFile The properties file for the project
     * @param isClearCache Whether to clear database cache or not
     * @return A configuration by given parameters
     * @throws JBakeException if loading the configuration fails
     */
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(
        sourceFolder: File,
        destination: File?,
        propertiesFile: File?,
        isClearCache: Boolean
    ): DefaultJBakeConfiguration {
        val configuration = this.configUtil.loadConfig(sourceFolder, propertiesFile) as DefaultJBakeConfiguration
        configuration.setDestinationFolder(destination)
        configuration.setClearCache(isClearCache)
        return configuration
    }

    /**
     * Creates a [DefaultJBakeConfiguration]
     *
     * This is a compatibility factory method
     *
     * @param sourceFolder The source folder of the project
     * @param destination The destination folder to render and copy files to
     * @param compositeConfiguration A given [CompositeConfiguration]
     * @param isClearCache Whether to clear database cache or not
     * @return A configuration by given parameters
     */
    @Deprecated("use {@link #createDefaultJbakeConfiguration(File, File, File, boolean)} instead")
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(
        sourceFolder: File?,
        destination: File?,
        compositeConfiguration: CompositeConfiguration?,
        isClearCache: Boolean
    ): DefaultJBakeConfiguration {
        val configuration = DefaultJBakeConfiguration(sourceFolder, compositeConfiguration)
        configuration.setDestinationFolder(destination)
        configuration.setClearCache(isClearCache)
        return configuration
    }

    /**
     * Creates a [DefaultJBakeConfiguration]
     *
     * This is a compatibility factory method
     *
     * @param sourceFolder The source folder of the project
     * @param destination The destination folder to render and copy files to
     * @param compositeConfiguration A given [CompositeConfiguration]
     * @return A configuration by given parameters
     */
    @Deprecated("use {@link #createDefaultJbakeConfiguration(File, File, File, boolean)} instead")
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(
        sourceFolder: File?,
        destination: File?,
        compositeConfiguration: CompositeConfiguration?
    ): DefaultJBakeConfiguration {
        val configuration = DefaultJBakeConfiguration(sourceFolder, compositeConfiguration)
        configuration.setDestinationFolder(destination)
        return configuration
    }

    /**
     * Creates a [DefaultJBakeConfiguration]
     *
     *
     * @param sourceFolder The source folder of the project
     * @param config A [CompositeConfiguration]
     * @return A configuration by given parameters
     */
    @Deprecated("")
    @Throws(JBakeException::class)
    fun createDefaultJbakeConfiguration(
        sourceFolder: File?,
        config: CompositeConfiguration?
    ): DefaultJBakeConfiguration {
        return DefaultJBakeConfiguration(sourceFolder, config)
    }

    /**
     * Creates a [DefaultJBakeConfiguration] with value site.host replaced
     * by http://localhost:[server.port].
     * The server.port is read from the project properties file.
     *
     * @param sourceFolder The source folder of the project
     * @param destinationFolder The destination folder to render and copy files to
     * @param isClearCache Whether to clear database cache or not
     * @return A configuration by given parameters
     * @throws JBakeException if loading the configuration fails
     */
    @Deprecated("use {@link #createJettyJbakeConfiguration(File, File, File, boolean)} instead")
    @Throws(JBakeException::class)
    fun createJettyJbakeConfiguration(
        sourceFolder: File,
        destinationFolder: File?,
        isClearCache: Boolean
    ): DefaultJBakeConfiguration {
        return createJettyJbakeConfiguration(sourceFolder, destinationFolder, null as File?, isClearCache)
    }

    /**
     * Creates a [DefaultJBakeConfiguration] with value site.host replaced
     * by http://localhost:[server.port].
     * The server.port is read from the project properties file.
     *
     * @param sourceFolder The source folder of the project
     * @param destinationFolder The destination folder to render and copy files to
     * @param propertiesFile The properties file for the project
     * @param isClearCache Whether to clear database cache or not
     * @return A configuration by given parameters
     * @throws JBakeException if loading the configuration fails
     */
    @Throws(JBakeException::class)
    fun createJettyJbakeConfiguration(
        sourceFolder: File,
        destinationFolder: File?,
        propertiesFile: File?,
        isClearCache: Boolean
    ): DefaultJBakeConfiguration {
        val configuration = this.configUtil.loadConfig(sourceFolder, propertiesFile) as DefaultJBakeConfiguration
        configuration.setDestinationFolder(destinationFolder)
        configuration.setClearCache(isClearCache)
        configuration.setSiteHost("http://" + configuration.getServerHostname() + ":" + configuration.getServerPort() + configuration.getServerContextPath())
        return configuration
    }

    fun setEncoding(charset: String): JBakeConfigurationFactory {
        this.configUtil.setEncoding(charset)
        return this
    }
}
