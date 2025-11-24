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
 *
 * @author Jonathan Bullock [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
 */
class Asset {

    /** A list of all errors occurred during asset copying */
    internal val errors: MutableList<Throwable> = LinkedList<Throwable>()

    private val config: JBakeConfiguration

    /**
     * @param source      Source file for the asset
     * @param destination Destination (target) directory for asset file
     * @param config      Project configuration
     */
    @Deprecated(
        """Use {@link #Asset(JBakeConfiguration)} instead.
      Compatibility constructor.
      Creates an instance of Asset."""
    )
    constructor(source: File, destination: File, config: CompositeConfiguration) {
        this.config = JBakeConfigurationFactory().createDefaultJbakeConfiguration(source, destination, config)
    }

    /** Creates an instance of Asset. */
    constructor(config: JBakeConfiguration) {
        this.config = config
    }

    /**
     * Copy all files from supplied path.
     * Copy all files from assets folder to destination folder read from configuration
     *
     * @param path The starting path
     */
    @JvmOverloads
    fun copy(path: File = config.assetFolder) {
        val filter = FileFilter { file ->
            (!config.assetIgnoreHidden || !file.isHidden()) && (file.isFile() || FileUtil.directoryOnlyIfNotIgnored(file, config))
        }
        copy(path, config.destinationFolder, filter)
    }

    /** Copy one asset file at a time. */
    fun copySingleFile(asset: File) {
        try {
            if (!asset.isDirectory()) {
                val targetPath =
                    (config.destinationFolder.getCanonicalPath() + File.separatorChar) + assetSubPath(asset)
                log.info("Copying single asset file to [{}]", targetPath)
                copyFile(asset, File(targetPath))
            } else {
                log.info("Skip copying single asset file [{}]. Is a directory.", asset.path)
            }
        } catch (io: IOException) {
            log.error("Failed to copy the asset file.", io)
        }
    }

    /**
     * Determine if a given file is an asset file.
     * @param path to the file to validate.
     * @return true if the path provided points to a file in the asset folder.
     */
    fun isAssetFile(path: File): Boolean {
        var isAsset = false

        try {
            if (FileUtil.directoryOnlyIfNotIgnored(path.getParentFile(), config)) {
                if (FileUtil.isFileInDirectory(path, config.assetFolder)) {
                    isAsset = true
                } else if (FileUtil.isFileInDirectory(path, config.contentFolder)
                    && FileUtil.getNotContentFileFilter(config).accept(path)
                ) {
                    isAsset = true
                }
            }
        } catch (ioe: IOException) {
            log.error("Unable to determine the path to asset file {}", path.path, ioe)
        }
        return isAsset
    }

    /**
     * Responsible for copying any asset files that exist within the content directory.
     *
     * @param path of the content directory
     */
    fun copyAssetsFromContent(path: File) {
        copy(path, config.destinationFolder, FileUtil.getNotContentFileFilter(config))
    }


    @Throws(IOException::class)
    private fun assetSubPath(asset: File): String {
        // First, strip asset folder from file path
        var targetFolder =
            asset.getCanonicalPath().replace(config.assetFolder.getCanonicalPath() + File.separatorChar, "")
        // And just to be sure, let's also remove the content folder, as some assets are copied from here.
        targetFolder = targetFolder.replace(config.contentFolder.getCanonicalPath() + File.separatorChar, "")
        return targetFolder
    }

    private fun copy(sourceFolder: File, targetFolder: File, filter: FileFilter) {
        val assets = sourceFolder.listFiles(filter)
        if (assets != null) {
            Arrays.sort(assets)
            for (asset in assets) {
                val target = File(targetFolder, asset.getName())
                if (asset.isFile()) {
                    copyFile(asset, target)
                } else if (asset.isDirectory()) {
                    copy(asset, target, filter)
                }
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

    companion object {
        private val log: Logger = LoggerFactory.getLogger(Asset::class.java)
    }
}
