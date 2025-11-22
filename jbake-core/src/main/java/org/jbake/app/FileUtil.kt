package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.parser.Engines
import java.io.*
import java.net.URLDecoder
import java.nio.file.Paths
import java.security.MessageDigest

/**
 * Provides File related functions
 *
 * @author Jonathan Bullock [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
 */
object FileUtil {
    const val URI_SEPARATOR_CHAR: String = "/"

    /**
     * Filters files based on their file extension.
     *
     * @param config the jbake configuration
     * @return Object for filtering files
     */
    fun getFileFilter(config: JBakeConfiguration): FileFilter {
        return object : FileFilter {
            override fun accept(pathname: File): Boolean {
                //Accept if input  is a non-hidden file with registered extension
                //or if a non-hidden and not-ignored directory
                return !pathname.isHidden() && (pathname.isFile()
                        && Engines.recognizedExtensions
                    .contains(fileExt(pathname))) || (directoryOnlyIfNotIgnored(pathname, config))
            }
        }
    }

    @get:Deprecated("use {@link #getFileFilter(JBakeConfiguration)} instead")
    val fileFilter: FileFilter
        /**
         * Filters files based on their file extension.
         *
         * @return Object for filtering files
         */
        get() = object : FileFilter {
            override fun accept(pathname: File): Boolean {
                //Accept if input  is a non-hidden file with registered extension
                //or if a non-hidden and not-ignored directory
                return !pathname.isHidden() && (pathname.isFile()
                        && Engines.recognizedExtensions
                    .contains(fileExt(pathname))) || (directoryOnlyIfNotIgnored(
                    pathname
                ))
            }
        }

    val dataFileFilter: FileFilter
        /**
         * Filters files based on their file extension - only find data files (i.e. files with .yaml or .yml extension)
         *
         * @return Object for filtering files
         */
        get() = object : FileFilter {
            override fun accept(pathname: File): Boolean {
                return "yaml".equals(
                    fileExt(pathname),
                    ignoreCase = true
                ) || "yml".equals(fileExt(pathname), ignoreCase = true)
            }
        }

    /**
     * Gets the list of files that are not content files based on their extension.
     *
     * @param config the jbake configuration
     * @return FileFilter object
     */
    fun getNotContentFileFilter(config: JBakeConfiguration): FileFilter {
        return object : FileFilter {
            override fun accept(pathname: File): Boolean {
                //Accept if input  is a non-hidden file with NOT-registered extension
                //or if a non-hidden and not-ignored directory
                return !pathname.isHidden() && (pathname.isFile() //extension should not be from registered content extensions
                        && !Engines.recognizedExtensions.contains(fileExt(pathname)))
                        || (directoryOnlyIfNotIgnored(pathname, config))
            }
        }
    }

    @get:Deprecated("use {@link #getNotContentFileFilter(JBakeConfiguration)} instead")
    val notContentFileFilter: FileFilter
        /**
         * Gets the list of files that are not content files based on their extension.
         *
         * @return FileFilter object
         */
        get() = object : FileFilter {
            override fun accept(pathname: File): Boolean {
                //Accept if input  is a non-hidden file with NOT-registered extension
                //or if a non-hidden and not-ignored directory
                return !pathname.isHidden() && (pathname.isFile() //extension should not be from registered content extensions
                        && !Engines.recognizedExtensions
                    .contains(fileExt(pathname)))
                        || (directoryOnlyIfNotIgnored(pathname))
            }
        }

    /**
     * Ignores directory (and children) if it contains a file named in the
     * configuration as a marker to ignore the directory.
     *
     * @param file the file to test
     * @param config the jbake configuration
     * @return true if file is directory and not ignored
     */
    fun directoryOnlyIfNotIgnored(file: File, config: JBakeConfiguration): Boolean {

        val ignoreFile: FilenameFilter = object : FilenameFilter {
            override fun accept(dir: File?, name: String): Boolean {
                return name.equals(config.ignoreFileName, ignoreCase = true)
            }
        }

        return file.isDirectory() && (file.listFiles(ignoreFile).size == 0)
    }

    /**
     * Ignores directory (and children) if it contains a file named ".jbakeignore".
     *
     * @param file the file to test
     * @return true if file is directory and not ignored
     */
    @Deprecated("use {@link #directoryOnlyIfNotIgnored(File, JBakeConfiguration)} instead")
    fun directoryOnlyIfNotIgnored(file: File): Boolean {
        var accept = false

        val ignoreFile: FilenameFilter = object : FilenameFilter {
            override fun accept(dir: File?, name: String): Boolean {
                return name.equals(".jbakeignore", ignoreCase = true)
            }
        }

        accept = file.isDirectory() && (file.listFiles(ignoreFile).size == 0)

        return accept
    }

    fun isExistingFolder(f: File): Boolean {
        return f.exists() && f.isDirectory()
    }

    @JvmStatic
    @get:Throws(Exception::class)
    val runningLocation: File
        /**
         * Works out the folder where JBake is running from.
         *
         * @return File referencing folder JBake is running from
         * @throws Exception when application is not able to work out where is JBake running from
         */
        get() {
            val codePath =
                FileUtil::class.java.getProtectionDomain().getCodeSource().getLocation().getPath()
            val decodedPath = URLDecoder.decode(codePath, "UTF-8")
            val codeFile = File(decodedPath)
            if (!codeFile.exists()) {
                throw Exception("Cannot locate running location of JBake!")
            }
            val codeFolder = codeFile.getParentFile().getParentFile()
            if (!codeFolder.exists()) {
                throw Exception("Cannot locate running location of JBake!")
            }

            return codeFolder
        }

    fun fileExt(src: File): String {
        val name = src.getName()
        return fileExt(name)
    }

    fun fileExt(name: String): String {
        val idx = name.lastIndexOf('.')
        if (idx > 0) {
            return name.substring(idx + 1)
        } else {
            return ""
        }
    }

    /**
     * Computes the hash of a file or directory.
     *
     * @param sourceFile the original file or directory
     * @return an hex string representing the SHA1 hash of the file or directory.
     * @throws Exception if any IOException of SecurityException occured
     */
    fun sha1(sourceFile: File): String {
        val buffer = ByteArray(1024)
        val complete = MessageDigest.getInstance("SHA-1")
        updateDigest(complete, sourceFile, buffer)
        val bytes = complete.digest()
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    @Throws(IOException::class)
    private fun updateDigest(digest: MessageDigest, sourceFile: File, buffer: ByteArray) {
        if (sourceFile.isFile()) {
            FileInputStream(sourceFile).use { fis ->
                var numRead: Int
                do {
                    numRead = fis.read(buffer)
                    if (numRead > 0) {
                        digest.update(buffer, 0, numRead)
                    }
                } while (numRead != -1)
            }
        } else if (sourceFile.isDirectory()) {
            val files = sourceFile.listFiles()
            if (files != null) {
                for (file in files) {
                    updateDigest(digest, file, buffer)
                }
            }
        }
    }

    /**
     * platform independent file.getPath()
     *
     * @param file the file to transform, or `null`
     * @return The result of file.getPath() with all path Separators beeing a "/", or `null`
     * Needed to transform Windows path separators into slashes.
     */
    fun asPath(file: File): String {
        //if (file == null) return null

        return asPath(file.getPath())
    }

    /**
     * platform independent file.getPath()
     *
     * @param path the path to transform, or `null`
     * @return The result will have all platform path separators replaced by "/".
     */
    fun asPath(path: String): String {
        //if (path == null) return null

        // On Windows we have to replace the backslash
        return if (File.separator == URI_SEPARATOR_CHAR) path
            else path.replace(File.separator, URI_SEPARATOR_CHAR)
    }

    /**
     * Given a file inside content it return
     * the relative path to get to the root.
     *
     *
     * Example: /content and /content/tags/blog will return '../..'
     *
     * @param sourceFile the file to calculate relative path for
     * @param rootPath the root path
     * @param config the jbake configuration
     * @return the relative path to get to the root
     */
    fun getPathToRoot(config: JBakeConfiguration, rootPath: File, sourceFile: File): String {
        val r = Paths.get(rootPath.toURI())
        val s = Paths.get(sourceFile.getParentFile().toURI())
        val relativePath = s.relativize(r)

        val sb = StringBuilder()

        sb.append(asPath(relativePath.toString()))

        if (config.uriWithoutExtension) {
            sb.append("/..")
        }
        if (sb.length > 0) {  // added as calling logic assumes / at end.
            sb.append("/")
        }
        return sb.toString()
    }

    fun getUriPathToDestinationRoot(config: JBakeConfiguration, sourceFile: File): String {
        return getPathToRoot(config, config.destinationFolder!!, sourceFile)
    }

    @JvmStatic
    fun getUriPathToContentRoot(config: JBakeConfiguration, sourceFile: File): String {
        return getPathToRoot(config, config.contentFolder!!, sourceFile)
    }

    /**
     * Utility method to determine if a given file is located somewhere in the directory provided.
     *
     * @param file used to check if it is located in the provided directory.
     * @param directory to validate whether or not the provided file resides.
     * @return true if the file is somewhere in the provided directory, false if it is not.
     * @throws IOException if the canonical path for either of the input directories can't be determined.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun isFileInDirectory(file: File, directory: File): Boolean {
        return (file.exists()
                && !file.isHidden() && directory.isDirectory()
                && file.getCanonicalPath().startsWith(directory.getCanonicalPath()))
    }
}
