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

import com.orientechnologies.orient.core.Orient;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jbake.app.ConfigUtil;
import org.jbake.app.Oven;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs jbake on a folder
 */
@Mojo(name = "generate", requiresProject = false)
public class GenerateMojo extends AbstractMojo {

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
   * Set if cache is present or clear
   */
  @Parameter(property = "jbake.isClearCache", defaultValue = "false", required = true)
  protected boolean isClearCache;

  /**
   * Custom configuration properties.
   * These properties override the properties in <code>jbake.properties</code>.
   * In the templates the properties can be accessed using the prefix <code>config.</code>
   * e.g. <code>config.foo</code> for the property <code>&lt;foo>bar&lt/foo></code>.
   */
  @Parameter(required = false)
  protected Map<String, Object> properties = new HashMap<>();

  public final void execute() throws MojoExecutionException {
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

  protected void executeInternal() throws MojoExecutionException {
    reRender();
  }

  protected void reRender() throws MojoExecutionException {
    try {
      // TODO: Smells bad. A lot
      Orient.instance().startup();

      // TODO: At some point, reuse Oven
      Oven oven = new Oven(inputDirectory, outputDirectory, createConfiguration(), isClearCache);

      oven.setupPaths();

      oven.bake();
    } catch (Exception e) {
      getLog().info("Oops", e);

      throw new MojoExecutionException("Failure when running: ", e);
    }
  }

  private CompositeConfiguration createConfiguration() throws ConfigurationException {
    final CompositeConfiguration config = new CompositeConfiguration();
    config.addConfiguration(new MapConfiguration(properties));
    config.addConfiguration(ConfigUtil.load(inputDirectory));
    return config;
  }

}
