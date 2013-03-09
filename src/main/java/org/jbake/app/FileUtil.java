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
				if (pathname.isFile()) {
					if (pathname.getPath().endsWith(".html")) {
						return true;
					} else if (pathname.getPath().endsWith(".md")) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
		};
	}
}
