package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.parser.Engines
import java.io.*
import java.io.File.separator
import java.net.URLDecoder
import java.nio.file.Paths
import java.security.MessageDigest

/**
 * Provides File related functions
 */
object FileUtil {
    const val URI_SEPARATOR_CHAR: String = "/"

    /** Filters files based on their file extension. */
    fun getFileFilter(config: JBakeConfiguration): FileFilter {
        return FileFilter { pathname ->
            // Accept if input is a non-hidden file with registered extension, or if a non-hidden and not-ignored directory.
            !pathname.isHidden() && (pathname.isFile()
                && Engines.recognizedExtensions
                .contains(fileExt(pathname))) || (directoryOnlyIfNotIgnored(pathname, config))
        }
    }

    /** Filters files based on their file extension. */
    @get:Deprecated("use {@link #getFileFilter(JBakeConfiguration)} instead")
    val fileFilter: FileFilter
        get() = FileFilter { pathname ->
            // Accept if input is a non-hidden file with registered extension, or if a non-hidden and not-ignored directory.
            val fileWithExtension = pathname.isFile() && Engines.recognizedExtensions.contains(fileExt(pathname))

            !pathname.isHidden() && fileWithExtension || directoryOnlyIfNotIgnored(pathname)
        }

    /** Filters files based on their file extension - only find data files (i.e. files with .yaml or .yml extension). */
    val dataFileFilter: FileFilter
        get() = FileFilter { pathname -> fileExt(pathname).lowercase().let { it == "yaml" || it == "yml" } }

    /** Gets the list of files that are not content files based on their extension. */
    fun getNotContentFileFilter(config: JBakeConfiguration): FileFilter {
        return FileFilter { pathname ->
            // Accept if input  is a non-hidden file with NOT-registered extension or if a non-hidden and not-ignored directory.
            (!pathname.isHidden() && (pathname.isFile() //extension should not be from registered content extensions
                && !Engines.recognizedExtensions.contains(fileExt(pathname)))
                || (directoryOnlyIfNotIgnored(pathname, config)))
        }
    }

    @get:Deprecated("use {@link #getNotContentFileFilter(JBakeConfiguration)} instead")
    /** Gets the list of files that are not content files based on their extension. */
    val notContentFileFilter: FileFilter
        get() = FileFilter { pathname ->
            // Accept if input is a non-hidden file with NOT-registered extension, or if a non-hidden and not-ignored directory.
            (!pathname.isHidden() && (pathname.isFile() // Extension should not be from registered content extensions.
                && !Engines.recognizedExtensions
                .contains(fileExt(pathname)))
                || (directoryOnlyIfNotIgnored(pathname)))
        }

    /**
     * Ignores directory (and children) if it contains a file named in the configuration as a marker to ignore the directory.
     *
     * @return true if file is directory and not ignored
     */
    fun directoryOnlyIfNotIgnored(dir: File, config: JBakeConfiguration): Boolean {
        val ignoreFile = FilenameFilter { _, name -> name.equals(config.ignoreFileName, ignoreCase = true) }
        return dir.isDirectory && dir.listFiles(ignoreFile).isEmpty()
        // TODO: Use dir.resolve(config.ignoreFileName).toFile().exists() from Java NIO
    }

    /**
     * Ignores directory (and children) if it contains a file named ".jbakeignore".
     *
     * @return true if file is directory and not ignored
     */
    @Deprecated("use {@link #directoryOnlyIfNotIgnored(File, JBakeConfiguration)} instead")
    fun directoryOnlyIfNotIgnored(file: File): Boolean {
        val ignoreFile = FilenameFilter { _, name -> name.equals(".jbakeignore", ignoreCase = true) }
        return file.isDirectory && file.listFiles(ignoreFile).isEmpty()
    }

    fun isExistingFolder(f: File) = f.exists() && f.isDirectory()

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
            // Check for system property first (set by build tools during tests)
            val classesPath = System.getProperty("jbake.buildOutputDir")
            if (classesPath != null) {
                val classesDir = File(classesPath)
                if (classesDir.exists()) return classesDir
            }

            val codePath = FileUtil::class.java.getProtectionDomain().codeSource.location.path
            val decodedPath = URLDecoder.decode(codePath, "UTF-8")
            val codeFile = File(decodedPath)
            if (!codeFile.exists())
                throw Exception("Cannot locate running location of JBake!")

            val codeFolder = codeFile.getParentFile().getParentFile()
            if (!codeFolder.exists())
                throw Exception("Cannot locate running location of JBake!")

            return codeFolder
        }

    // TBD: Could be replaced with Kotlin extension function File.extension.
    fun fileExt(src: File): String = fileExt(src.name)

    fun fileExt(name: String): String = name.substringAfterLast(".", "")

    /**
     * Computes the hash of a file or directory.
     *
     * @return A hex string representing the SHA1 hash of the file or directory.
     * @throws Exception if any IOException of SecurityException occured
     */
    @Throws(IOException::class)
    fun sha1(sourceFile: File): String {
        val digester = MessageDigest.getInstance("SHA-1")
        updateDigest(digester, sourceFile, ByteArray(1024))
        return digester.digest().joinToString("") { "%02x".format(it) }
    }

    @Throws(IOException::class)
    private fun updateDigest(digest: MessageDigest, sourceFile: File, buffer: ByteArray) {

        if (sourceFile.isFile()) {
            FileInputStream(sourceFile).use { fis ->
                var numRead: Int
                do {
                    numRead = fis.read(buffer)
                    if (numRead > 0)
                        digest.update(buffer, 0, numRead)
                } while (numRead != -1)
            }
        }
        else if (sourceFile.isDirectory()) {
            val files = sourceFile.listFiles()?.sorted() ?: return
            for (file in files) {
                updateDigest(digest, file, buffer)
            }
        }
    }

    /**
     * Platform-independent file.getPath(). Needed to transform Windows path separators into slashes.
     *
     * @return The result of file.getPath() with all path Separators beeing a "/", or `null`
     */
    fun asPath(file: File): String =
        if (separator == URI_SEPARATOR_CHAR) file.path
        // On Windows we have to replace the backslash.
        else file.path.replace(separator, URI_SEPARATOR_CHAR)

    /**
     * Given a file inside content it return the relative path to get to the root.
     * Example: /content and /content/tags/blog will return '../..'
     *
     * @param sourceFile the file to calculate relative path for
     * @return the relative path to get to the root
     */
    fun getPathToRoot(config: JBakeConfiguration, rootPath: File, sourceFile: File): String {
        val root = Paths.get(rootPath.toURI())
        val source = Paths.get(sourceFile.getParentFile().toURI())
        val relativePath = source.relativize(root)

        return buildString {
            append(asPath(relativePath.toFile()))
            if (config.uriWithoutExtension) append("/..")
            if (isNotEmpty()) append("/") // The calling logic assumes / at end.
        }
    }

    fun getUriPathToDestinationRoot(config: JBakeConfiguration, sourceFile: File)
        = getPathToRoot(config, config.destinationFolder, sourceFile)

    @JvmStatic
    fun getUriPathToContentRoot(config: JBakeConfiguration, sourceFile: File)
        = getPathToRoot(config, config.contentFolder, sourceFile)

    /**
     * Utility method to determine if a given file is located somewhere in the directory provided.
     *
     * @return true if the file is somewhere in the provided directory, false if it is not.
     * @throws IOException if the canonical path for either of the input directories can't be determined.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun isFileInDirectory(file: File, directory: File): Boolean =
        file.exists() && !file.isHidden && directory.isDirectory() && file.getCanonicalPath().startsWith(directory.getCanonicalPath())

}
