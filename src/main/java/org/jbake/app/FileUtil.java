package org.jbake.app;

import org.jbake.parser.Engines;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.MessageDigest;

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

    public static String sha1(File filename) throws Exception {
        InputStream fis = new FileInputStream(filename);
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("SHA-1");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        byte[] bytes = complete.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
