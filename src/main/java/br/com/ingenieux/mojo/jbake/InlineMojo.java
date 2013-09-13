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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import winstone.Launcher;

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
	 * Listen Port
	 */
	@Parameter(property = "jbake.port", defaultValue = "8080")
	private Integer port;

	protected Launcher launcher = null;

	protected void stopServer() {
		launcher.shutdown();
	}

	protected void initServer() throws MojoExecutionException {
		try {
			Map<String, String> args = new HashMap<String, String>();

			args.put("webroot", outputDirectory.getAbsolutePath());
			args.put("httpPort", port.toString());
			args.put("httpListenAddress", listenAddress);

			launcher = new Launcher(args);
		} catch (Exception exc) {
			throw new MojoExecutionException("Ooops", exc);
		}
	}
}
