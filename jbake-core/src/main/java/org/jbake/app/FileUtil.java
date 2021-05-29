package org.jbake.app;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.engine.MarkupEngines;
import org.jbake.exception.JBakeException;
import org.jbake.launcher.SystemExit;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides File related functions
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class FileUtil {

    public static final String URI_SEPARATOR_CHAR = "/";

    private FileUtil() {}

    /**
     * Filters files based on their file extension.
     *
     * @param config the jbake configuration
     * @return Object for filtering files
     */
    public static FileFilter getFileFilter(JBakeConfiguration config) {
        //Accept if input  is a non-hidden file with registered extension
        //or if a non-hidden and not-ignored directory
        return pathname -> !pathname.isHidden() && (pathname.isFile()
                && MarkupEngines.getInstance().supportsExtension(fileExt(pathname))) || (directoryOnlyIfNotIgnored(pathname, config));
    }

    /**
     * Filters files based on their file extension.
     *
     * @return Object for filtering files
     * @deprecated use {@link #getFileFilter(JBakeConfiguration)} instead
     */
    @Deprecated
    public static FileFilter getFileFilter() {
        return pathname -> !pathname.isHidden() && (pathname.isFile()
            && MarkupEngines.getInstance().supportsExtension(fileExt(pathname))) || (directoryOnlyIfNotIgnored(pathname));
    }

    /**
     * Filters files based on their file extension - only find data files (i.e. files with .yaml or .yml extension)
     *
     * @return Object for filtering files
     */
    public static FileFilter getDataFileFilter() {
        return pathname -> "yaml".equalsIgnoreCase(fileExt(pathname)) || "yml".equalsIgnoreCase(fileExt(pathname));
    }

    /**
     * Gets the list of files that are not content files based on their extension.
     *
     * @param config the jbake configuration
     * @return FileFilter object
     */
    public static FileFilter getNotContentFileFilter(JBakeConfiguration config) {
        return pathname -> !pathname.isHidden() && (pathname.isFile()
            //extension should not be from registered content extensions
            && !MarkupEngines.getInstance().supportsExtension(fileExt(pathname)))
            || (directoryOnlyIfNotIgnored(pathname, config));
    }

    /**
     * Gets the list of files that are not content files based on their extension.
     *
     * @return FileFilter object
     * @deprecated use {@link #getNotContentFileFilter(JBakeConfiguration)} instead
     */
    @Deprecated
    public static FileFilter getNotContentFileFilter() {
        return pathname -> !pathname.isHidden() && (pathname.isFile()
                //extension should not be from registered content extensions
                && !MarkupEngines.getInstance().supportsExtension(fileExt(pathname)))
                || (directoryOnlyIfNotIgnored(pathname));
    }

    /**
     * Ignores directory (and children) if it contains a file named in the
     * configuration as a marker to ignore the directory.
     *
     * @param file the file to test
     * @param config the jbake configuration
     * @return true if file is directory and not ignored
     */
    public static boolean directoryOnlyIfNotIgnored(File file, JBakeConfiguration config) {
        FilenameFilter ignoreFile = (dir, name) -> name.equalsIgnoreCase(config.getIgnoreFileName());

        return file.isDirectory() &&  (file.listFiles(ignoreFile).length == 0);
    }

    /**
     * Ignores directory (and children) if it contains a file named ".jbakeignore".
     *
     * @param file the file to test
     * @return true if file is directory and not ignored
     * @deprecated use {@link #directoryOnlyIfNotIgnored(File, JBakeConfiguration)} instead
     */
    @Deprecated
    public static boolean directoryOnlyIfNotIgnored(File file) {
        FilenameFilter ignoreFile = (dir, name) -> name.equalsIgnoreCase(".jbakeignore");

        return file.isDirectory() && (file.listFiles(ignoreFile).length == 0);
    }

    public static boolean isExistingFolder(File f) {
        return null != f && f.exists() && f.isDirectory();
    }

    /**
     * Works out the folder where JBake is running from.
     *
     * @return File referencing folder JBake is running from
     * @throws Exception when application is not able to work out where is JBake running from
     */
    public static File getRunningLocation() throws UnsupportedEncodingException {
        String codePath = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(codePath, "UTF-8");
        File codeFile = new File(decodedPath);
        if (!codeFile.exists()) {
            throw new JBakeException(SystemExit.ERROR, "Cannot locate running location of JBake!");
        }
        File codeFolder = codeFile.getParentFile().getParentFile();
        if (!codeFolder.exists()) {
            throw new JBakeException(SystemExit.ERROR, "Cannot locate running location of JBake!");
        }

        return codeFolder;
    }

    public static String fileExt(File src) {
        String name = src.getName();
        return fileExt(name);
    }

    public static String fileExt(String name) {
        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            return name.substring(idx + 1);
        } else {
            return "";
        }
    }

    /**
     * Computes the hash of a file or directory.
     *
     * @param sourceFile the original file or directory
     * @return an hex string representing the SHA1 hash of the file or directory.
     * @throws Exception if any IOException of SecurityException occured
     */
    public static String sha1(File sourceFile) throws NoSuchAlgorithmException, IOException {
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("SHA-1");
        updateDigest(complete, sourceFile, buffer);
        byte[] bytes = complete.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static void updateDigest(final MessageDigest digest, final File sourceFile, final byte[] buffer) throws IOException {
        if (sourceFile.isFile()) {
            try (InputStream fis = new FileInputStream(sourceFile)) {
                int numRead;
                do {
                    numRead = fis.read(buffer);
                    if (numRead > 0) {
                        digest.update(buffer, 0, numRead);
                    }
                } while (numRead != -1);
            }
        } else if (sourceFile.isDirectory()) {
            File[] files = sourceFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    updateDigest(digest, file, buffer);
                }
            }
        }
    }

    /**
     * platform independent file.getPath()
     *
     * @param file the file to transform, or {@code null}
     * @return The result of file.getPath() with all path Separators beeing a "/", or {@code null}
     * Needed to transform Windows path separators into slashes.
     */
    public static String asPath(File file) {
        if (file == null) {
            return null;
        }
        return asPath(file.getPath());
    }

    /**
     * platform independent file.getPath()
     *
     * @param path the path to transform, or {@code null}
     * @return The result will have all platform path separators replaced by "/".
     */
    public static String asPath(String path) {
        if (path == null) {
            return null;
        }

        // On windows we have to replace the backslash
        if (!File.separator.equals(FileUtil.URI_SEPARATOR_CHAR)) {
            return path.replace(File.separator, FileUtil.URI_SEPARATOR_CHAR);
        } else {
            return path;
        }
    }

    /**
     * Given a file inside content it return
     * the relative path to get to the root.
     * <p>
     * Example: /content and /content/tags/blog will return '../..'
     *
     * @param sourceFile the file to calculate relative path for
     * @param rootPath the root path
     * @param config the jbake configuration
     * @return the relative path to get to the root
     */
    public static String getPathToRoot(JBakeConfiguration config, File rootPath, File sourceFile) {

        Path r = Paths.get(rootPath.toURI());
        Path s = Paths.get(sourceFile.getParentFile().toURI());
        Path relativePath = s.relativize(r);

        StringBuilder sb = new StringBuilder();

        sb.append(asPath(relativePath.toString()));

        if (config.getUriWithoutExtension()) {
            sb.append("/..");
        }
        if (sb.length() > 0) {  // added as calling logic assumes / at end.
            sb.append("/");
        }
        return sb.toString();
    }

    public static String getUriPathToDestinationRoot(JBakeConfiguration config, File sourceFile) {
        return getPathToRoot(config, config.getDestinationFolder(), sourceFile);
    }

    public static String getUriPathToContentRoot(JBakeConfiguration config, File sourceFile) {
        return getPathToRoot(config, config.getContentFolder(), sourceFile);
    }

    /**
     * Utility method to determine if a given file is located somewhere in the directory provided.
     *
     * @param file used to check if it is located in the provided directory.
     * @param directory to validate whether or not the provided file resides.
     * @return true if the file is somewhere in the provided directory, false if it is not.
     * @throws IOException if the canonical path for either of the input directories can't be determined.
     */
    public static boolean isFileInDirectory(File file, File directory) throws IOException {
        return (file.exists()
             && !file.isHidden()
             && directory.isDirectory()
             && file.getCanonicalPath().startsWith(directory.getCanonicalPath()));
    }
}
