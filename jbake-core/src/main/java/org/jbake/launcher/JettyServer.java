package org.jbake.launcher;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.jbake.app.JBakeException;
import org.jbake.app.JBakeException.SystemExit;
import org.jbake.app.configuration.JBakeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;


/**
 * Provides Jetty server related functions
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class JettyServer implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    private Server server;

    @Deprecated
    public void run(String resourceBase, String port) {
        LOGGER.warn("DEPRECATED. This method will be removed in the next major release. Use run(String resourceBase, JBakeConfiguration config) instead.");
        run(resourceBase, "/", "localhost", Integer.parseInt(port));
    }

    public void run(String resourceBase, JBakeConfiguration configuration) {
        run(resourceBase, configuration.getServerContextPath(), configuration.getServerHostname(), configuration.getServerPort());
    }

    /**
     * Run Jetty web server serving out supplied path on supplied port
     *
     * @param resourceBase Base directory for resources to be served
     * @param port         Required server port
     */
    private void run(String resourceBase, String contextPath, String hostname, int port) {
        try {
            server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setHost(hostname);
            connector.setPort(port);
            server.addConnector(connector);

            ResourceHandler resource_handler = new ResourceHandler();
            resource_handler.setDirectoriesListed(true);
            resource_handler.setWelcomeFiles(new String[]{"index", "index.html"});
            resource_handler.setResourceBase(resourceBase);

            ContextHandler contextHandler = new ContextHandler();
            contextHandler.setContextPath(contextPath);
            contextHandler.setHandler(resource_handler);

            HandlerList handlers = new HandlerList();

            handlers.setHandlers(new Handler[]{contextHandler, new DefaultHandler()});
            server.setHandler(handlers);

            LOGGER.info("Serving out contents of: [{}] on http://{}:{}{}", resourceBase, hostname, port, contextHandler.getContextPath());
            LOGGER.info("(To stop server hit CTRL-C)");

            server.start();
            server.join();
        } catch (Exception e) {
            throw new JBakeException(SystemExit.SERVER_ERROR, "unable to start the server", e);
        }
    }

    public boolean isStarted() {
        return server != null && server.isStarted();
    }

    @Override
    public void close() throws IOException {
        if (server.isRunning()) {
            try {
                server.stop();
            } catch (Exception e) {
                LOGGER.error("unable to stop server");
            }
        }

    }
}
