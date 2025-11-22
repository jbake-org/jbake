package org.jbake.app.configuration

import org.jbake.app.FileUtil
import org.jbake.app.JBakeException
import org.jbake.launcher.SystemExit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class JBakeConfigurationInspector(private val configuration: JBakeConfiguration) {
    @Throws(JBakeException::class)
    fun inspect() {
        ensureSource()
        ensureTemplateFolder()
        ensureContentFolder()
        ensureDestination()
        checkAssetFolder()
    }

    @Throws(JBakeException::class)
    private fun ensureSource() {
        val source = configuration.sourceFolder ?: throw JBakeException(SystemExit.CONFIGURATION_ERROR, "Error: Source folder is not configured.")
        if (!FileUtil.isExistingFolder(source))
            throw JBakeException(SystemExit.CONFIGURATION_ERROR, "Error: Source folder must exist: " + source.absolutePath)

        if (!configuration.sourceFolder!!.canRead())
            throw JBakeException(SystemExit.CONFIGURATION_ERROR, "Error: Source folder is not readable: " + source.absolutePath)
    }

    private fun ensureTemplateFolder() {
        val path = configuration.templateFolder
        checkRequiredFolderExists(PropertyList.TEMPLATE_FOLDER.key, path)
    }

    private fun ensureContentFolder() {
        val path = configuration.contentFolder
        checkRequiredFolderExists(PropertyList.CONTENT_FOLDER.key, path)
    }

    private fun ensureDestination() {
        val destination = configuration.destinationFolder
        if (!destination.exists()) {
            destination.mkdirs()
        }
        if (!destination.canWrite()) {
            throw JBakeException(
                SystemExit.CONFIGURATION_ERROR,
                "Error: Destination folder is not writable: " + destination.absolutePath
            )
        }
    }

    private fun checkAssetFolder() {
        val path = configuration.assetFolder
        if (!path.exists()) {
            log.warn("No asset folder '{}' was found!", path.absolutePath)
        }
    }

    private fun checkRequiredFolderExists(folderName: String?, path: File) {
        if (!FileUtil.isExistingFolder(path))
            throw JBakeException(SystemExit.CONFIGURATION_ERROR, "Error: Required folder cannot be found! Expected to find [" + folderName + "] at: " + path.absolutePath)
    }


    companion object {
        private val log: Logger = LoggerFactory.getLogger(JBakeConfigurationInspector::class.java)
    }
}
