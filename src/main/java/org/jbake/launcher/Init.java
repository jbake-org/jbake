package org.jbake.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ZipUtil;

public class Init {

	private CompositeConfiguration config;
	
	public Init(CompositeConfiguration config) {
		this.config = config;
	}
	
	/**
	 * Initialises specified folder with contents of template file located in supplied folder. 
	 * 
	 * @param outputFolder
	 * @param templateLocationFolder
	 * @throws Exception
	 */
	public void run(File outputFolder, File templateLocationFolder) throws Exception {
		if (!outputFolder.canWrite()) {
            throw new Exception("Output folder is not writeable!");
        }
		
		File[] contents = outputFolder.listFiles();
		boolean safe = true;
		if (contents != null) {
			for (File content : contents) {
				if (content.isDirectory()) {
					if (content.getName().equalsIgnoreCase(config.getString("template.folder"))) {
						safe = false;
					}
					if (content.getName().equalsIgnoreCase(config.getString("content.folder"))) {
						safe = false;
					}
					if (content.getName().equalsIgnoreCase(config.getString("asset.folder"))) {
						safe = false;
					}
				}
			}
		}
		
		if (!safe) {
			throw new Exception("Output folder already contains structure!");
		}
		
		File templateFile = new File(templateLocationFolder, config.getString("base.template"));
		if (!templateFile.exists()) {
			throw new Exception("Cannot locate template file: " + templateFile.getPath());
		}
		ZipUtil.extract(new FileInputStream(templateFile), outputFolder);
	}
}
