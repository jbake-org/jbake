package br.com.ingenieux.mojo.jbake;

/*
 * Copyright 2013 ingenieux Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

import java.io.File;

/**
 * Runs jbake on a folder while watching and serving a folder with it
 */
@Mojo(name = "inline", requiresDirectInvocation = true)
public class InlineMojo extends WatchMojo {
	/**
	 * Listen Port
	 */
	@Parameter(property = "jbake.listenAddress", defaultValue = "127.0.0.1")
	private String listenAddress;

    /**
     * Index File
     */
    @Parameter(property = "jbake.indexFile", defaultValue = "index.html")
    private String indexFile;

    /**
	 * Listen Port
	 */
	@Parameter(property = "jbake.port", defaultValue = "8080")
	private Integer port;

    Server server = new Server();

    class Server extends Verticle {
        {
            vertx = VertxFactory.newVertx();
        }

        @Override
        public void start() {
            vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
                @Override
                public void handle(HttpServerRequest req) {
                    String file = req.path().endsWith("/") ? req.path() + indexFile : req.path();

                    if (new File(outputDirectory + file).isDirectory()) {
                        req.response().setStatusCode(301).putHeader("Location", file + "/").close();
                    } else {
                        req.response().sendFile(outputDirectory.getAbsolutePath() + file);
                    }
                }
            }).listen(port, listenAddress);
        }
    }

	protected void stopServer() {
        server.stop();
	}

	protected void initServer() throws MojoExecutionException {
        server.start();
	}
}
