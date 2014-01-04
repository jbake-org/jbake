package org.jbake.app;

import java.io.File;
import java.io.FileFilter;
import java.net.URLDecoder;

/**
 * Provides File related functions
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class FileUtil {
	
	/**
	 * Filters files based on their file extension.
	 * 
	 * @return	Object for filtering files
	 */
	public static FileFilter getFileFilter() {
		return new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return !pathname.isFile()
						|| pathname.getPath().endsWith(".html")
						|| pathname.getPath().endsWith(".md")
						|| pathname.getPath().endsWith(".asciidoc")
						|| pathname.getPath().endsWith(".ad")
						|| pathname.getPath().endsWith(".adoc");
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
