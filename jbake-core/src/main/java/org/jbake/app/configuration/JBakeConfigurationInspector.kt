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
        val source = configuration.getSourceFolder()
        if (!FileUtil.isExistingFolder(source)) {
            throw JBakeException(
                SystemExit.CONFIGURATION_ERROR,
                "Error: Source folder must exist: " + source.getAbsolutePath()
            )
        }
        if (!configuration.getSourceFolder().canRead()) {
            throw JBakeException(
                SystemExit.CONFIGURATION_ERROR,
                "Error: Source folder is not readable: " + source.getAbsolutePath()
            )
        }
    }

    private fun ensureTemplateFolder() {
        val path = configuration.getTemplateFolder()
        checkRequiredFolderExists(PropertyList.TEMPLATE_FOLDER.getKey(), path)
    }

    private fun ensureContentFolder() {
        val path = configuration.getContentFolder()
        checkRequiredFolderExists(PropertyList.CONTENT_FOLDER.getKey(), path)
    }

    private fun ensureDestination() {
        val destination = configuration.getDestinationFolder()
        if (!destination.exists()) {
            destination.mkdirs()
        }
        if (!destination.canWrite()) {
            throw JBakeException(
                SystemExit.CONFIGURATION_ERROR,
                "Error: Destination folder is not writable: " + destination.getAbsolutePath()
            )
        }
    }

    private fun checkAssetFolder() {
        val path = configuration.getAssetFolder()
        if (!path.exists()) {
            LOGGER.warn("No asset folder '{}' was found!", path.getAbsolutePath())
        }
    }

    private fun checkRequiredFolderExists(folderName: String?, path: File) {
        if (!FileUtil.isExistingFolder(path)) {
            throw JBakeException(
                SystemExit.CONFIGURATION_ERROR,
                "Error: Required folder cannot be found! Expected to find [" + folderName + "] at: " + path.getAbsolutePath()
            )
        }
    }


    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(JBakeConfigurationInspector::class.java)
    }
}
