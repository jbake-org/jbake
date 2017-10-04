package org.jbake.launcher;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides Jetty server related functions
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class JettyServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    /**
     * Run Jetty web server serving out supplied path on supplied port
     *
     * @param path Base directory for resourced to be served
     * @param port Required server port
     */
    public void run(String path, String port) {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setHost("localhost");
        connector.setPort(Integer.parseInt(port));
        server.addConnector(connector);

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});

        resource_handler.setResourceBase(path);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, new DefaultHandler()});
        server.setHandler(handlers);

        LOGGER.info("Serving out contents of: [{}] on http://localhost:{}/", path, port);
        LOGGER.info("(To stop server hit CTRL-C)");

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
