package org.jbake.app;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

import org.jbake.parser.Engines;

/**
 * Provides File related functions
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class FileUtil {

    /**
     * Creates a temporary directory.
     * @return Temporary directory.
     */
    public static File createTempDirectory() {
        return new File("");
    }
    
    /**
     * Copies recursively all files and directories from source folder to destination folder.
     * @param sourceFolder where elements are got.
     * @param destinationFolder where elements are copied.
     * @throws IOException
     */
    public static void copyRecursive(Path sourceFolder, Path destinationFolder) throws IOException {
        Files.walkFileTree(sourceFolder, new CopyDirVisitor(sourceFolder, destinationFolder));
    }

    /**
     * Copies recursively all files and directories from source folder to a new created temp directory.
     * @param sourceFolder where elements are got.
     * @return Temp directory.
     * @throws IOException
     */
    public static Path copyRecursiveToTempDirectory(Path sourceFolder) throws IOException {
        Path toFolder = Files.createTempDirectory("jbake-");
        copyRecursive(sourceFolder, toFolder);
        return toFolder;
    }
    
    /**
     * Filters files based on their file extension.
     * 
     * @return Object for filtering files
     */
    public static FileFilter getFileFilter() {
        return new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return !pathname.isFile() || Engines.getRecognizedExtensions().contains(fileExt(pathname));
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
     * @param sourceFile
     *            the original file or directory
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

    private static void updateDigest(final MessageDigest digest, final File sourceFile, final byte[] buffer)
            throws IOException {
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

    /**
     * platform independent file.getPath()
     * 
     * @param file
     *            the file to transform, or {@code null}
     * @return The result of file.getPath() with all path Separators beeing a "/", or {@code null} Needed to transform
     *         Windows path separators into slashes.
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
     * @param path
     *            the path to transform, or {@code null}
     * @return The result will have alle platform path separators replaced by "/".
     */
    public static String asPath(String path) {
        if (path == null) {
            return null;
        }
        return path.replace(File.separator, "/");
    }

    private static class CopyDirVisitor extends SimpleFileVisitor<Path> {

        private Path fromPath;
        private Path toPath;

        public CopyDirVisitor(Path fromPath, Path toPath) {
            this.fromPath = fromPath;
            this.toPath = toPath;
        }

        private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }
}
