package org.jbake.launcher

import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.handler.DefaultHandler
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler
import org.jbake.app.JBakeException
import org.jbake.app.configuration.JBakeConfiguration
import org.slf4j.Logger
import org.jbake.util.Logging.logger
import java.io.Closeable
import java.io.IOException

/**
 * Provides Jetty server related functions
 */
class JettyServer : Closeable {
    private var server: Server? = null

    @Deprecated("")
    fun run(resourceBase: String, port: String) {
        log.warn("DEPRECATED. This method will be removed in the next major release. Use run(String resourceBase, JBakeConfiguration config) instead.")
        run(resourceBase, "/", "localhost", port.toInt())
    }

    fun run(resourceBase: String, configuration: JBakeConfiguration) {
        run(resourceBase, configuration.serverContextPath, configuration.serverHostname, configuration.serverPort)
    }

    /**
     * Run Jetty web server serving out supplied path on supplied port
     *
     * @param resourceBase Base directory for resources to be served
     * @param port         Required server port
     */
    private fun run(resourceBase: String, contextPath: String, hostname: String, port: Int) {
        try {
            server = Server()
            val connector = ServerConnector(server)
            connector.host = hostname
            connector.port = port
            server!!.addConnector(connector)

            val resource_handler = ResourceHandler()
            resource_handler.isDirectoriesListed = true
            resource_handler.welcomeFiles = arrayOf("index", "index.html")
            resource_handler.resourceBase = resourceBase

            val contextHandler = ContextHandler()
            contextHandler.contextPath = contextPath
            contextHandler.handler = resource_handler

            val handlers = HandlerList()

            handlers.handlers = arrayOf<Handler>(contextHandler, DefaultHandler())
            server!!.handler = handlers

            log.info("Serving out contents of: [{}] on http://{}:{}{}", resourceBase, hostname, port, contextHandler.contextPath)
            log.info("(To stop server hit CTRL-C)")

            server!!.start()
            server!!.join()
        } catch (e: Exception) {
            throw JBakeException(SystemExit.SERVER_ERROR, "unable to start the server", e)
        }
    }

    val isStarted: Boolean
        get() = server != null && server!!.isStarted

    @Throws(IOException::class)
    override fun close() {
        if (server!!.isRunning) {
            try {
                server!!.stop()
            } catch (e: Exception) {
                log.error("unable to stop server")
            }
        }
    }

    private val log: Logger by logger()
}
