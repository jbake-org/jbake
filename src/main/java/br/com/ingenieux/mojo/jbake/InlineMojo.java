package br.com.ingenieux.mojo.jbake;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import winstone.Launcher;

/**
 * Runs jbake on a folder while watching and serving a folder with it
 */
@Mojo(name = "inline", requiresDirectInvocation = true)
public class InlineMojo extends GenerateMojo {
	/**
	 * Location of the Output Directory.
	 */
	@Parameter(property = "jbake.outputDirectory", defaultValue = "${project.build.directory}/${project.build.finalName}", required = true)
	private File outputDirectory;

	/**
	 * Location of the Output Directory.
	 */
	@Parameter(property = "jbake.inputDirectory", defaultValue = "${project.basedir}/src/main/jbake", required = true)
	private File inputDirectory;

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

	public void execute() throws MojoExecutionException {
		super.execute();
		
		getLog().info("Now listening for changes on path " + inputDirectory.getPath());

		Map<String, String> args = new HashMap<String, String>();

		Launcher launcher = null;

		try {
			args.put("webroot", outputDirectory.getAbsolutePath());
			args.put("httpPort", port.toString());
			args.put("httpListenAddress", listenAddress);

			launcher = new Launcher(args);

			final Path inPath = FileSystems.getDefault().getPath(
					inputDirectory.getAbsolutePath());
			final WatchService watchService = inPath.getFileSystem()
					.newWatchService();

			inPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY,
					StandardWatchEventKinds.ENTRY_DELETE);

			final AtomicBoolean done = new AtomicBoolean(false);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			
			(new Thread() {
				@Override
				public void run() {
					try {
						getLog().info("Running. Hit <ENTER> to finish");
						reader.readLine();
					} catch (Exception exc) {
					} finally {
						done.set(true);
					}
				}
			}).run();

			do {
				WatchKey watchKey = watchService.take();
				Thread.sleep(1000L);

				if (watchKey.reset()) {
					watchKey.cancel();
					watchService.close();
					return;
				}
				
				List<WatchEvent<?>> events = watchKey.pollEvents();
				
				if (! events.isEmpty()) {
					getLog().info("Refreshing");
					super.execute();
				}
			} while (! done.get());
		} catch (Exception exc) {
			getLog().info("Oops", exc);

			throw new MojoExecutionException("Oops", exc);
		} finally {
			getLog().info("Finishing");
			launcher.shutdown();
		}
	}
}
