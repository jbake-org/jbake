package org.jbake.maven

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.zip.ZipInputStream

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

/**
 * Seeds a new JBake Template into the (non-existing) directory defined by outputDirectory
 */
@Mojo(name = "seed", requiresProject = true, requiresDirectInvocation = true)
class SeedMojo : AbstractMojo() {
    /**
     * Location of the Seeding Zip
     */
    @Parameter(
        property = "jbake.seedUrl",
        defaultValue = "https://github.com/jbake-org/jbake-template-bootstrap/zipball/master/",
        required = true
    )
    protected var seedUrl: String? = null

    /**
     * Location of the Output Directory.
     */
    @Parameter(property = "jbake.outputDirectory", defaultValue = "\${project.basedir}/src/main/jbake", required = true)
    protected var outputDirectory: File? = null

    /**
     * Really force overwrite if output dir exists? defaults to false
     */
    @Parameter(property = "jbake.force", defaultValue = "false")
    protected var force: Boolean? = null

    @Throws(MojoExecutionException::class)
    override fun execute() {
        if (outputDirectory!!.exists() && (!force!!)) throw MojoExecutionException(
            String.format(
                "The outputDirectory %s must *NOT* exist. Invoke with jbake.force as true to disregard",
                outputDirectory!!.getName()
            )
        )

        try {
            val url = URL(seedUrl)
            val tmpZipFile = File.createTempFile("jbake", ".zip")

            log.info(String.format("Downloading contents from %s into %s", seedUrl, tmpZipFile))

            val fos = FileOutputStream(tmpZipFile)
            val length = IOUtils.copy(url.openStream(), fos)

            fos.close()

            log.info(String.format("%d bytes downloaded. Unpacking into %s", length, outputDirectory))

            unpackZip(tmpZipFile)
        } catch (e: Exception) {
            log.info("Oops", e)
            throw MojoExecutionException("Failure when running: ", e)
        }
    }

    @Throws(IOException::class)
    private fun unpackZip(tmpZipFile: File) {
        val zis =
            ZipInputStream(FileInputStream(tmpZipFile))
        //get the zipped file list entry
        var ze = zis.getNextEntry()

        while (ze != null) {
            if (ze.isDirectory) {
                ze = zis.getNextEntry()
                continue
            }

            val fileName = stripLeadingPath(ze.getName())
            val newFile = File(outputDirectory.toString() + File.separator + fileName)

            File(newFile.getParent()).mkdirs()

            val fos = FileOutputStream(newFile)

            IOUtils.copy(zis, fos)

            fos.close()
            ze = zis.getNextEntry()
        }

        zis.closeEntry()
        zis.close()
    }

    private fun stripLeadingPath(name: String): String? {
        val elements =
            LinkedList(Arrays.asList(*name.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()))

        elements.pop()

        return StringUtils.join(elements.iterator(), '/')
    }
}
