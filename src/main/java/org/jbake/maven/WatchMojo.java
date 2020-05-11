package org.jbake.maven;

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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.jbake.maven.util.DirWatcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Runs jbake on a folder while watching for changes
 */
@Mojo(name = "watch", requiresDirectInvocation = true, requiresProject = false)
public class WatchMojo extends GenerateMojo {

  public void executeInternal() throws MojoExecutionException {
    reRender();

    Long lastProcessed = System.currentTimeMillis();

    getLog().info(
        "Now listening for changes on path " + inputDirectory.getPath());

    initServer();

    DirWatcher dirWatcher = null;

    try {
      dirWatcher = new DirWatcher(inputDirectory);
      final AtomicBoolean done = new AtomicBoolean(false);
      final BufferedReader reader = new BufferedReader(
          new InputStreamReader(System.in));

      (new Thread() {
        @Override
        public void run() {
          try {
            getLog()
                .info("Running. Enter a blank line to finish. Anything else forces re-rendering.");

            while (true) {
              String line = reader.readLine();

              if (isBlank(line)) {
                break;
              }

              reRender();
            }
          } catch (Exception exc) {
            getLog().info("Ooops", exc);
          } finally {
            done.set(true);
          }
        }
      }).start();

      dirWatcher.start();

      do {
        Long result = dirWatcher.processEvents();

        if (null == result) {
          // do nothing on purpose
        } else if (result >= lastProcessed) {
          getLog().info("Refreshing");

          super.reRender();

          lastProcessed = Long.valueOf(System.currentTimeMillis());
        }
      } while (!done.get());
    } catch (Exception exc) {
      getLog().info("Oops", exc);

      throw new MojoExecutionException("Oops", exc);
    } finally {
      getLog().info("Finishing");

      if (null != dirWatcher)
        dirWatcher.stop();

      stopServer();
    }
  }

  protected void stopServer() throws MojoExecutionException {
  }

  protected void initServer() throws MojoExecutionException {
  }
}
