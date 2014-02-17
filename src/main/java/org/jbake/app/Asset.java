package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with assets (static files such as css, js or image files).
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Asset {

    private static final Logger LOGGER = LoggerFactory.getLogger(Asset.class);

    private File source;
	private File destination;
	private int errorCount = 0;
	
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
					LOGGER.info("Copying [{}]...", assets[i].getPath());
					File sourceFile = assets[i];
					File destFile = new File(sourceFile.getPath().replace(source.getPath()+File.separator+"assets", destination.getPath()));
					try {
						FileUtils.copyFile(sourceFile, destFile);
					} catch (IOException e) {
						e.printStackTrace();
						errorCount++;
					}
					LOGGER.info("done!");
				} 
				
				if (assets[i].isDirectory()) {
					copy(assets[i]);
				}
			}
		}
	}

	public int getErrorCount() {
		return errorCount;
	}
}
