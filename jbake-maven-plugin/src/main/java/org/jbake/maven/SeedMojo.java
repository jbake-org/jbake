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

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * Seeds a new JBake Template into the (non-existing) directory defined by outputDirectory
 */
@Mojo(name = "seed", requiresProject = true, requiresDirectInvocation = true)
public class SeedMojo extends AbstractMojo {
	/**
	 * Location of the Seeding Zip
	 */
	@Parameter(property = "jbake.seedUrl", defaultValue = "https://github.com/jbake-org/jbake-template-bootstrap/zipball/master/", required = true)
	protected String seedUrl;

	/**
	 * Location of the Output Directory.
	 */
	@Parameter(property = "jbake.outputDirectory", defaultValue = "${project.basedir}/src/main/jbake", required = true)
	protected File outputDirectory;

    /**
     * Really force overwrite if output dir exists? defaults to false
     */
    @Parameter(property = "jbake.force", defaultValue = "false")
    protected Boolean force;

    public void execute() throws MojoExecutionException {
        if (outputDirectory.exists() && (! force))
            throw new MojoExecutionException(format("The outputDirectory %s must *NOT* exist. Invoke with jbake.force as true to disregard", outputDirectory.getName()));

		try {
            URL url = new URL(seedUrl);
            File tmpZipFile = File.createTempFile("jbake", ".zip");

            getLog().info(format("Downloading contents from %s into %s", seedUrl, tmpZipFile));

            final FileOutputStream fos = new FileOutputStream(tmpZipFile);
            int length = IOUtils.copy(url.openStream(), fos);

            fos.close();

            getLog().info(format("%d bytes downloaded. Unpacking into %s", length, outputDirectory));

            unpackZip(tmpZipFile);
		} catch (Exception e) {
			getLog().info("Oops", e);
			throw new MojoExecutionException("Failure when running: ", e);
		}
	}

    private void unpackZip(File tmpZipFile) throws IOException {
        ZipInputStream zis =
                new ZipInputStream(new FileInputStream(tmpZipFile));
        //get the zipped file list entry
        ZipEntry ze = zis.getNextEntry();

        while(ze!=null){
            if (ze.isDirectory()) {
                ze = zis.getNextEntry();
                continue;
            }

            String fileName = stripLeadingPath(ze.getName());
            File newFile = new File(outputDirectory + File.separator + fileName);

            new File(newFile.getParent()).mkdirs();

            FileOutputStream fos = new FileOutputStream(newFile);

            IOUtils.copy(zis, fos);

            fos.close();
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    private String stripLeadingPath(String name) {
        LinkedList<String> elements = new LinkedList<>(asList(name.split("/")));

        elements.pop();

        return join(elements.iterator(), '/');
    }
}
