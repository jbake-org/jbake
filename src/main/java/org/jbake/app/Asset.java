package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

/**
 * Deals with assets (static files such as css, js or image files).
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Asset {

	private File source;
	private File destination;
	
	/**
	 * Creates an instance of Asset.
	 * 
	 * @param source
	 * @param destination
	 */
	public Asset(File source, File destination) {
		this.source = source;
		this.destination = destination;
	}
	
	/**
	 * Copy all files from supplied path. 
	 * 
	 * @param path	The starting path
	 */
	public void copy(File path) {
		File[] assets = path.listFiles();
		if (assets != null) {
			Arrays.sort(assets);
			for (int i = 0; i < assets.length; i++) {
				if (assets[i].isFile()) {
					System.out.print("Copying [" + assets[i].getPath() + "]... ");
					File sourceFile = assets[i];
					File destFile = new File(sourceFile.getPath().replace(source.getPath()+File.separator+"assets", destination.getPath()));
					try {
						FileUtils.copyFile(sourceFile, destFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("done!");
				} 
				
				if (assets[i].isDirectory()) {
					copy(assets[i]);
				}
			}
		}
	}
}
