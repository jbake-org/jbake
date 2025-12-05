package org.jbake.app.configuration

import org.jbake.app.JBakeExitException
import java.io.File


/**
 * A [JBakeConfiguration] factory.
 */
class JBakeConfigurationFactory {

    @JvmField var configUtil: ConfigUtil = ConfigUtil()

    /**
     * Creates a [DefaultJBakeConfiguration] using default.properties and jbake.properties
     *
     * @param sourceDir The source directory of the project
     * @param destination The destination directory to render and copy files to
     * @param isClearCache Whether to clear database cache or not
     * @throws JBakeExitException if loading the configuration fails
     */
    @Throws(JBakeExitException::class)
    fun createDefaultJbakeConfiguration(sourceDir: File, destination: File, isClearCache: Boolean): DefaultJBakeConfiguration
        = createDefaultJbakeConfiguration(sourceDir, destination, null as File?, isClearCache)

    /**
     * Creates a [DefaultJBakeConfiguration]
     * @param sourceDir The source directory of the project
     * @param destination The destination directory to render and copy files to
     * @param propertiesFile The properties file for the project
     * @param isClearCache Whether to clear database cache or not
     * @throws JBakeExitException if loading the configuration fails
     */
    @Throws(JBakeExitException::class)
    fun createDefaultJbakeConfiguration(
        sourceDir: File,
        destination: File,
        propertiesFile: File?,
        isClearCache: Boolean,
    ): DefaultJBakeConfiguration {
        val configuration = this.configUtil.loadConfig(sourceDir, propertiesFile) as DefaultJBakeConfiguration
        configuration.destinationDir = destination
        configuration.clearCache = isClearCache
        return configuration
    }


    /**
     * Creates a [DefaultJBakeConfiguration] with value site.host replaced
     * by http://localhost:[server.port].
     * The server.port is read from the project properties file.
     *
     * @param destinationDir The destination directory to render and copy files to
     * @param isClearCache Whether to clear database cache or not
     * @throws JBakeExitException if loading the configuration fails
     */
    @Deprecated("use {@link #createJettyJbakeConfiguration(File, File, File, boolean)} instead")
    @Throws(JBakeExitException::class)
    fun createJettyJbakeConfiguration(sourceDir: File, destinationDir: File, isClearCache: Boolean)
        = createJettyJbakeConfiguration(sourceDir, destinationDir, null as File?, isClearCache)

    /**
     * Creates a [DefaultJBakeConfiguration] with value site.host replaced
     * by http://localhost:[server.port].
     * The server.port is read from the project properties file.
     *
     * @param destinationDir The destination directory to render and copy files to
     * @param propertiesFile The properties file for the project
     * @param isClearCache Whether to clear database cache or not
     */
    @Throws(JBakeExitException::class)
    fun createJettyJbakeConfiguration(
        sourceDir: File,
        destinationDir: File?,
        propertiesFile: File?,
        isClearCache: Boolean
    ): DefaultJBakeConfiguration {
        val conf = this.configUtil.loadConfig(sourceDir, propertiesFile) as DefaultJBakeConfiguration
        conf.destinationDir = destinationDir ?: File("./output")
        conf.clearCache = isClearCache
        conf.siteHost = "http://" + conf.serverHostname + ":" + conf.serverPort + conf.serverContextPath
        return conf
    }

    fun setEncoding(charset: String) = this.apply { configUtil.setEncoding(charset) }
}
