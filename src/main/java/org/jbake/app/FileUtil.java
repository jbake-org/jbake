package org.jbake.app;

import java.io.File;
import java.io.FileFilter;

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
						|| pathname.getPath().endsWith(".ad");
			}
		};
	}

    public static boolean isExistingFolder(File f) {
        return null != f && f.exists() && f.isDirectory();
    }
}
