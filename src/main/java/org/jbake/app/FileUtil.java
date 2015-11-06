package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.parser.Engines;

import java.io.*;
import java.net.URLDecoder;
import java.security.MessageDigest;

/**
 * Provides File related functions
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class FileUtil {

    /**
     * Filters files based on their file extension.
     *
     * @return Object for filtering files
     */
    public static FileFilter getFileFilter() {
        return new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return !pathname.isFile()
                        || Engines.getRecognizedExtensions().contains(fileExt(pathname));
            }
        };
    }

    public static boolean isExistingFolder(File f) {
        return null != f && f.exists() && f.isDirectory();
    }

    /**
     * Works out the folder where JBake is running from.
     *
     * @return File referencing folder JBake is running from
     * @throws Exception
     */
    public static File getRunningLocation() throws Exception {
        // work out where JBake is running from
        String codePath = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(codePath, "UTF-8");
        File codeFile = new File(decodedPath);
        if (!codeFile.exists()) {
            throw new Exception("Cannot locate running location of JBake!");
        }
        File codeFolder = codeFile.getParentFile();
        if (!codeFolder.exists()) {
            throw new Exception("Cannot locate running location of JBake!");
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
     * @throws Exception
     */
    public static String sha1(File sourceFile) throws Exception {
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
            InputStream fis = new FileInputStream(sourceFile);
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            fis.close();
        } else if (sourceFile.isDirectory()) {
            File[] files = sourceFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    updateDigest(digest, file, buffer);
                }
            }
        }
    }

    public static String findExtension(CompositeConfiguration config, String docType) {
        String extension = config.getString("template." + docType + ".extension");
        if (extension != null) {
            return extension;
        } else {
            return config.getString(Keys.OUTPUT_EXTENSION);
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
     * @return The result will have alle platform path separators replaced by "/".
     */
    public static String asPath(String path) {
        if (path == null) {
            return null;
        }
        return path.replace(File.separator, "/");
    }
}
