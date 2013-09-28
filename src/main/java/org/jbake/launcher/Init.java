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
	
	public void run(File outputFolder) throws Exception {
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
		
		// work out where JBake is running from
		String codePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(codePath, "UTF-8");
		File codeFile = new File(decodedPath);
		if (!codeFile.exists()) {
			throw new Exception("Cannot locate running location of JBake!");
		}
		File codeFolder = codeFile.getParentFile();
		if (!codeFolder.exists()) {
			throw new Exception("Cannot locate running location of JBake!");
		}

		// get reference to template file from running location
		File templateFile = new File(codeFolder, "base.zip");
		if (!templateFile.exists()) {
			throw new Exception("Cannot locate template file: " + templateFile.getPath());
		}
		
		ZipUtil.extract(new FileInputStream(templateFile), outputFolder);
	}
}
