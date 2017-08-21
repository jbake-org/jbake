package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.parser.Engines;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.MessageDigest;

/**
 * Provides File related functions
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
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
            	//Accept if input  is a non-hidden file with registered extension
            	//or if a non-hidden and not-ignored directory 
                return   !pathname.isHidden() && (pathname.isFile()
                        && Engines.getRecognizedExtensions().contains(fileExt(pathname))) || (directoryOnlyIfNotIgnored(pathname));
            }
        };
    }
    
    /**
     * Gets the list of files that are not content files based on their extension.
     * 
     * @return FileFilter object
     */
    public static FileFilter getNotContentFileFilter() {
        return new FileFilter() {

            @Override
            public boolean accept(File pathname) {
            	//Accept if input  is a non-hidden file with NOT-registered extension
            	//or if a non-hidden and not-ignored directory 
                return  !pathname.isHidden() && (pathname.isFile()
                		//extension should not be from registered content extensions
                        && !Engines.getRecognizedExtensions().contains(fileExt(pathname))) 
                			|| (directoryOnlyIfNotIgnored(pathname));
            }
        };
    }
    
    /**
     * Ignores directory (and children) if it contains a file named ".jbakeignore".
     * @param file {@link File}
     * @return {@link Boolean} true/false
     */
    public static boolean directoryOnlyIfNotIgnored(File file){
    	boolean accept = false;
    	
    	FilenameFilter ignoreFile = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.equalsIgnoreCase(".jbakeignore");
			}
		}; 
    	
    	accept = file.isDirectory() && (file.listFiles(ignoreFile).length == 0);
    	
    	return accept;
    }

    public static boolean isExistingFolder(File f) {
        return null != f && f.exists() && f.isDirectory();
    }

    /**
     * Works out the folder where JBake is running from.
     *
     * @return File referencing folder JBake is running from
     * @throws Exception	when application is not able to work out where is JBake running from
     */
    public static File getRunningLocation() throws Exception {
        String codePath = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(codePath, "UTF-8");
        File codeFile = new File(decodedPath);
        if (!codeFile.exists()) {
            throw new Exception("Cannot locate running location of JBake!");
        }
        File codeFolder = codeFile.getParentFile().getParentFile();
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
     * @throws Exception if any IOException of SecurityException occured
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
            if (files!=null) {
                for (File file : files) {
                    updateDigest(digest, file, buffer);
                }
            }
        }
    }

    public static String findExtension(CompositeConfiguration config, String docType) {
    	String extension = config.getString("template."+docType+".extension");
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
	 *         Needed to transform Windows path separators into slashes.
	 */
	public static String asPath(File file) {
		if(file == null) {
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
		if(path == null) {
			return null;
		}
		return path.replace(File.separator, "/");
	}
}
