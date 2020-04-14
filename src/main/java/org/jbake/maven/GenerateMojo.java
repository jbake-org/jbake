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

import com.orientechnologies.orient.core.Orient;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jbake.app.Oven;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;

import java.io.File;

/**
 * Runs jbake on a folder
 */
@Mojo(name = "generate", requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateMojo extends AbstractMojo {
  @Parameter(defaultValue="${project}")
  protected MavenProject project;

  /**
   * Location of the Output Directory.
   */
  @Parameter(property = "jbake.outputDirectory",
             defaultValue = "${project.build.directory}/${project.build.finalName}",
             required = true)
  protected File outputDirectory;

  /**
   * Location of the Output Directory.
   */
  @Parameter(property = "jbake.inputDirectory", defaultValue = "${project.basedir}/src/main/jbake",
             required = true)
  protected File inputDirectory;

  /**
   * Breaks the build when {@code true} and errors occur during baking in JBake oven.
   */
  @Parameter(property = "jbake.failOnError", defaultValue = "true")
  protected boolean failOnError;

  /**
   * Set if cache is present or clear
   */
  @Parameter(property = "jbake.isClearCache", defaultValue = "false", required = true)
  protected boolean isClearCache;

  public final void execute() throws MojoExecutionException, MojoFailureException {
    try {
      executeInternal();
    } finally {
      closeQuietly();
    }
  }

  protected final void closeQuietly() {
    try {
      Orient.instance().shutdown();
    } catch (Exception e) {
      getLog().warn("Oops", e);
    }
  }

  protected void executeInternal() throws MojoExecutionException, MojoFailureException {
    reRender();
  }

  protected void reRender() throws MojoExecutionException, MojoFailureException {
    try {
      // TODO: Smells bad. A lot
      Orient.instance().startup();

      // TODO: At some point, reuse Oven
      Oven oven = new Oven(createConfiguration());

      oven.bake();
      if (failOnError && !oven.getErrors().isEmpty()) {
          throw new MojoFailureException("Baked with " + oven.getErrors().size() + " errors. Check output above for details!");
      }
    } catch (Exception e) {
      getLog().info("Oops", e);

      throw new MojoExecutionException("Failure when running: ", e);
    }
  }

  protected JBakeConfiguration createConfiguration() throws ConfigurationException {
	  DefaultJBakeConfiguration jBakeConfiguration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(inputDirectory);
    jBakeConfiguration.getCompositeConfiguration().addConfiguration(new MapConfiguration(this.project.getProperties()));
    jBakeConfiguration.setDestinationFolder(outputDirectory);
    jBakeConfiguration.setClearCache(isClearCache);
    return jBakeConfiguration;
  }

}
