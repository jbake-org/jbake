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
    private var seedUrl: String? = null

    /**
     * Location of the Output Directory.
     */
    @Parameter(property = "jbake.outputDirectory", defaultValue = $$"${project.basedir}/src/main/jbake", required = true)
    private var outputDirectory: File? = null

    /**
     * Really force overwrite if output dir exists? defaults to false
     */
    @Parameter(property = "jbake.force", defaultValue = "false")
    private var force: Boolean? = null

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
        // Get the zipped file list entry.
        var ze = zis.getNextEntry()

        while (ze != null) {
            if (ze.isDirectory) {
                ze = zis.getNextEntry()
                continue
            }

            val fileName = stripLeadingPath(ze.getName())
            val newFile = File(outputDirectory.toString() + PathConstants.fS + fileName)

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
            LinkedList(listOf(*name.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()))

        elements.pop()

        return StringUtils.join(elements.iterator(), '/')
    }
}
