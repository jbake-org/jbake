package org.jbake.app

import org.apache.commons.io.FileUtils
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.util.Logging.logger
import org.slf4j.Logger
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.util.*

/**
 * Deals with assets (static files such as css, js or image files).
 */
class Asset(private val config: JBakeConfiguration) {

    /** A list of all errors occurred during asset copying */
    internal val errors: MutableList<Throwable> = LinkedList<Throwable>()


    /** Copy all files from assets directory to destination directory read from configuration */
    fun copy() = copy(config.assetDir)

    /** Copy all files from assets directory to destination directory read from configuration */
    fun copy(startingPath: File) {
        val filter = FileFilter { file ->
            (!config.assetIgnoreHidden || !file.isHidden())
                && (file.isFile() || FileUtil.directoryOnlyIfNotIgnored(file, config))
        }
        copy(startingPath, config.destinationDir, filter)
    }

    /** Copy one asset file at a time. */
    fun copySingleFile(asset: File) {
        if (asset.isDirectory()) {
            log.info("Skip copying single asset file [${asset.path}]. Is a directory.")
            return
        }

        try {
            val targetFile = config.destinationDir.toPath().resolve(assetSubPath(asset)).toFile()
            log.info("Copying single asset file to [${targetFile.path}]")
            copyFile(asset, targetFile)
        } catch (io: IOException) {
            log.error("Failed to copy the asset file.", io)
        }
    }

    /**
     * @return true if the path provided points to a file in the asset directory.
     */
    fun isAssetFile(fileToValidate: File): Boolean {
        try {
            if (!FileUtil.directoryOnlyIfNotIgnored(fileToValidate.parentFile, config))
                return false

            if (FileUtil.isFileInDirectory(fileToValidate, config.assetDir))
                return true

            if (FileUtil.isFileInDirectory(fileToValidate, config.contentDir)
                    && FileUtil.getNotContentFileFilter(config).accept(fileToValidate))
                return true
        }
        catch (ioe: IOException) {
            log.error("Unable to determine the path to asset file ${fileToValidate.path}", ioe)
        }
        return false
    }

    /**
     * Responsible for copying any asset files that exist within the content directory.
     */
    fun copyAssetsFromContent(contentDirectoryPath: File) {
        copy(contentDirectoryPath, config.destinationDir, FileUtil.getNotContentFileFilter(config))
    }


    @Throws(IOException::class)
    private fun assetSubPath(asset: File): String {
        val assetPath = asset.toPath()
        val assetDirPath = config.assetDir.toPath()
        val contentDirPath = config.contentDir.toPath()

        // Try to get relative path from asset directory.
        val relativePath = try {
            when {
                assetPath.startsWith(assetDirPath) -> assetDirPath.relativize(assetPath)
                // Asset is in content directory, strip that path
                assetPath.startsWith(contentDirPath) -> contentDirPath.relativize(assetPath)
                // Fallback to the file name
                else -> assetPath.fileName ?: assetPath
            }
        }
        // On error, just return the last component.
        catch (e: Exception) { assetPath.fileName ?: assetPath }

        return relativePath.toString()
    }

    private fun copy(sourceDir: File, targetDir: File, filter: FileFilter) {
        val assets = sourceDir.listFiles(filter)
        if (assets != null) {
            Arrays.sort(assets)
            for (asset in assets) {
                val target = targetDir.resolve(asset.getName())
                if (asset.isFile()) copyFile(asset, target)
                else if (asset.isDirectory()) copy(asset, target, filter)
            }
        }
    }

    private fun copyFile(asset: File, targetDir: File) {
        try {
            FileUtils.copyFile(asset, targetDir)
            log.info("Copying [${asset.path}]... done!")
        } catch (e: IOException) {
            log.error("Copying [${asset.path}]... failed!", e)
            errors.add(e)
        } catch (e: IllegalArgumentException) {
            log.error("Copying [${asset.path}]... failed!", e)
            errors.add(e)
        }
    }

    private val log: Logger by logger()
}
