package org.jbake.app

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.io.FileUtils
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.util.*

/**
 * Deals with assets (static files such as css, js or image files).
 */
class Asset {

    /** A list of all errors occurred during asset copying */
    internal val errors: MutableList<Throwable> = LinkedList<Throwable>()

    private val config: JBakeConfiguration

    @Deprecated("""Use {@link #Asset(JBakeConfiguration)} instead. Compatibility constructor.  Creates an instance of Asset.""")
    constructor(assetSource: File, destination: File, config: CompositeConfiguration) {
        this.config = JBakeConfigurationFactory().createDefaultJbakeConfiguration(assetSource, destination, config)
    }

    /** Creates an instance of Asset. */
    constructor(config: JBakeConfiguration) {
        this.config = config
    }

    /** Copy all files from assets folder to destination folder read from configuration */
    fun copy() = copy(config.assetFolder)

    /** Copy all files from assets folder to destination folder read from configuration */
    fun copy(startingPath: File) {
        val filter = FileFilter { file ->
            (!config.assetIgnoreHidden || !file.isHidden())
                && (file.isFile() || FileUtil.directoryOnlyIfNotIgnored(file, config))
        }
        copy(startingPath, config.destinationFolder, filter)
    }

    /** Copy one asset file at a time. */
    fun copySingleFile(asset: File) {
        if (asset.isDirectory()) {
            log.info("Skip copying single asset file [{}]. Is a directory.", asset.path)
            return
        }

        try {
            val targetFile = config.destinationFolder.toPath().resolve(assetSubPath(asset)).toFile()
            log.info("Copying single asset file to [{}]", targetFile.path)
            copyFile(asset, targetFile)
        } catch (io: IOException) {
            log.error("Failed to copy the asset file.", io)
        }
    }

    /**
     * @return true if the path provided points to a file in the asset folder.
     */
    fun isAssetFile(fileToValidate: File): Boolean {
        try {
            if (!FileUtil.directoryOnlyIfNotIgnored(fileToValidate.parentFile, config))
                return false

            if (FileUtil.isFileInDirectory(fileToValidate, config.assetFolder))
                return true

            if (FileUtil.isFileInDirectory(fileToValidate, config.contentFolder)
                    && FileUtil.getNotContentFileFilter(config).accept(fileToValidate))
                return true
        }
        catch (ioe: IOException) {
            log.error("Unable to determine the path to asset file {}", fileToValidate.path, ioe)
        }
        return false
    }

    /**
     * Responsible for copying any asset files that exist within the content directory.
     */
    fun copyAssetsFromContent(contentDirectoryPath: File) {
        copy(contentDirectoryPath, config.destinationFolder, FileUtil.getNotContentFileFilter(config))
    }


    @Throws(IOException::class)
    private fun assetSubPath(asset: File): String {
        val assetPath = asset.toPath()
        val assetFolderPath = config.assetFolder.toPath()
        val contentFolderPath = config.contentFolder.toPath()

        // Try to get relative path from asset folder.
        val relativePath = try {
            when {
                assetPath.startsWith(assetFolderPath) -> assetFolderPath.relativize(assetPath)
                // Asset is in content folder, strip that path
                assetPath.startsWith(contentFolderPath) -> contentFolderPath.relativize(assetPath)
                // Fallback to the file name
                else -> assetPath.fileName ?: assetPath
            }
        }
        // On error, just return the last component.
        catch (e: Exception) { assetPath.fileName ?: assetPath }

        return relativePath.toString()
    }

    private fun copy(sourceFolder: File, targetFolder: File, filter: FileFilter) {
        val assets = sourceFolder.listFiles(filter)
        if (assets != null) {
            Arrays.sort(assets)
            for (asset in assets) {
                val target = File(targetFolder, asset.getName())
                if (asset.isFile()) copyFile(asset, target)
                else if (asset.isDirectory()) copy(asset, target, filter)
            }
        }
    }

    private fun copyFile(asset: File, targetFolder: File) {
        try {
            FileUtils.copyFile(asset, targetFolder)
            log.info("Copying [{}]... done!", asset.path)
        } catch (e: IOException) {
            log.error("Copying [{}]... failed!", asset.path, e)
            errors.add(e)
        } catch (e: IllegalArgumentException) {
            log.error("Copying [{}]... failed!", asset.path, e)
            errors.add(e)
        }
    }

    private val log: Logger = LoggerFactory.getLogger(Asset::class.java)
}
