package org.jbake.app.configuration

import org.jbake.app.FileUtil
import org.jbake.app.JBakeException
import org.jbake.app.SystemExit
import org.jbake.util.Logging.logger
import org.jbake.util.warn
import org.slf4j.Logger
import java.io.File

class JBakeConfigurationInspector(private val configuration: JBakeConfiguration) {


    /**
     * Checks source path contains required sub-directorys (i.e. templates) and setups up variables for them.
     * Creates destination directory if it does not exist.
     *
     * @throws JBakeException If template or contents directory don't exist
     */
    @Throws(JBakeException::class)
    fun inspect() {
        ensureSource()
        ensureTemplateDir()
        ensureContentDir()
        ensureDestination()
        checkAssetDir()
    }

    @Throws(JBakeException::class)
    private fun ensureSource() {
        val source = configuration.sourceDir ?: throw JBakeException(SystemExit.CONFIG_ERROR, "Source dir is not configured.")
        if (!FileUtil.isExistingDirectory(source))
            throw JBakeException(SystemExit.CONFIG_ERROR, "Source dir must exist: " + source.absolutePath)

        if (!configuration.sourceDir!!.canRead())
            throw JBakeException(SystemExit.CONFIG_ERROR, "Source dir is not readable: " + source.absolutePath)
    }

    private fun ensureTemplateDir() {
        val path = configuration.templateDir
        checkRequiredDirExists(PropertyList.TEMPLATE_FOLDER.key, path)
    }

    private fun ensureContentDir() {
        val path = configuration.contentDir
        checkRequiredDirExists(PropertyList.CONTENT_FOLDER.key, path)
    }

    private fun ensureDestination() {
        val destination = configuration.destinationDir
        if (!destination.exists()) destination.mkdirs()
        if (!destination.canWrite())
            throw JBakeException(SystemExit.CONFIG_ERROR, "Destination dir is not writable: " + destination.absolutePath)
    }

    private fun checkAssetDir() {
        val path = configuration.assetDir
        if (!path.exists())
            log.warn { "No asset dir '${path.absolutePath}' was found!" }
    }

    private fun checkRequiredDirExists(folderName: String?, path: File) {
        if (!FileUtil.isExistingDirectory(path))
            throw JBakeException(SystemExit.CONFIG_ERROR, "Required dir cannot be found! Expected to find [" + folderName + "] at: " + path.absolutePath)
    }

    private val log: Logger by logger()
}
