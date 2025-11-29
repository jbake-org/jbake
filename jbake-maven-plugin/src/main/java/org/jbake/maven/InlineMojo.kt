package org.jbake.maven

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.jbake.app.JBakeException
import spark.Spark

/**
 * Runs jbake on a directory while watching and serving a directory with it
 */
@Mojo(name = "inline", requiresDirectInvocation = true, requiresProject = false)
class InlineMojo : WatchMojo() {

    @Parameter(property = "jbake.listenAddress", defaultValue = "127.0.0.1")
    private val listenAddress: String? = null

    /** Name of the index file (a list of all pages). */
    @Parameter(property = "jbake.indexFile", defaultValue = "index.html")
    private val indexFile: String? = null

    /** Listen Port */
    @Parameter(property = "jbake.port")
    private val port: Int? = null
        get() {
            if (field != null) return field
            try { return createConfiguration().serverPort }
            catch (e: JBakeException) { return 8820 }
        }

    @Throws(MojoExecutionException::class)
    override fun stopServer() {
        log.info("Stopping embedded web server")
        Spark.stop()
    }

    @Throws(MojoExecutionException::class)
    override fun initServer() {
        log.info("Starting embedded web server at http://$listenAddress:${this.port} serving: ${outputDirectory!!.path}")
        Spark.externalStaticFileLocation(outputDirectory!!.path)
        Spark.ipAddress(listenAddress)
        Spark.port(this.port!!)
        Spark.init()
        Spark.awaitInitialization()
        log.info("Embedded web server ready at http://$listenAddress:${this.port}")
    }
}
