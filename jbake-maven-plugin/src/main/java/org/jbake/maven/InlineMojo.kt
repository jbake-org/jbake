package org.jbake.maven

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.jbake.app.JBakeException
import spark.Spark

/**
 * Runs jbake on a folder while watching and serving a folder with it
 */
@Mojo(name = "inline", requiresDirectInvocation = true, requiresProject = false)
class InlineMojo : WatchMojo() {

    /** Listen Port */
    @Parameter(property = "jbake.listenAddress", defaultValue = "127.0.0.1")
    private val listenAddress: String? = null

    /** Index File */
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
    override fun stopServer()
        = Spark.stop()

    @Throws(MojoExecutionException::class)
    override fun initServer() {
        Spark.externalStaticFileLocation(outputDirectory!!.path)
        Spark.ipAddress(listenAddress)
        Spark.port(this.port!!)
        Spark.init()
        Spark.awaitInitialization()
    }
}
