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

import java.io.File;

import static spark.Spark.*;

import net_alchim31_livereload.LRServer;
import spark.Spark;

/**
 * Runs jbake on a folder while watching and serving a folder with it
 */
@Mojo(name = "inline", requiresDirectInvocation = true, requiresProject = false)
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

  /**
   * Use livereload.
   */
  @Parameter(property = "jbake.livereload", defaultValue = "true")
  private Boolean livereload;

  LRServer lrServer;

  protected void stopServer() throws MojoExecutionException {
    if (lrServer != null) {
      try {
        lrServer.stop();
      } catch (Exception e) {
        throw new MojoExecutionException("LiveReload Failure", e);
      }
    }

    stop();
  }

  protected void initServer() throws MojoExecutionException {
    externalStaticFileLocation(outputDirectory.getPath());

    ipAddress(listenAddress);
    port(this.port);



    if (Boolean.TRUE.equals(livereload)) {
      lrServer = new LRServer(35729, outputDirectory.toPath());
      try {
        lrServer.start();
      } catch (Exception e) {
        throw new MojoExecutionException("LiveReload Failure", e);
      }
    }

    awaitInitialization();
  }
}
