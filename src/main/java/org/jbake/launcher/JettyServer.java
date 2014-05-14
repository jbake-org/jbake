package org.jbake.launcher;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.Resource;


/**
 * Provides Jetty server related functions
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class JettyServer {

	/**
	 * Run Jetty web server serving out supplied path on supplied port
	 * 
	 * @param path
	 * @param port
	 */
	public static void run(String path, String port) {
		Server server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(Integer.parseInt(port));
        server.addConnector(connector);

        MyResourceHandler resource_handler = new MyResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase(path);
        
        DefaultHandler defaultHandler = new DefaultHandler();

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, defaultHandler });
        server.setHandler(handlers);
 
        System.out.println("Serving out contents of: [" + path + "] on http://localhost:" + port + "/");
        System.out.println("(To stop server hit CTRL-C)");
        
        try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class MyResourceHandler extends ResourceHandler{
   
   @Override
   protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType) {
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.addHeader("Access-Control-Allow-Headers", "x-requested-with");
      response.addHeader("Access-Control-Allow-Credentials", "true");
      super.doResponseHeaders(response, resource, mimeType);
   }
}
