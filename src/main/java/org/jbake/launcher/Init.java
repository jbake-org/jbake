package org.jbake.launcher;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.ZipUtil;

import java.io.File;
import java.io.FileInputStream;

/**
 * Initialises sample folder structure with pre-defined template
 * 
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 *
 */
public class Init {

	private CompositeConfiguration config;
	
	public Init(CompositeConfiguration config) {
		this.config = config;
	}
	
	/**
	 * Performs checks on output folder before extracting template file
	 * 
	 * @param outputFolder						Target directory for extracting template file
	 * @param templateLocationFolder  Source location for template file
	 * @param templateType						Type of the template to be used
	 * @throws Exception							if required folder structure can't be achieved without content overwriting
	 */
	public void run(File outputFolder, File templateLocationFolder, String templateType) throws Exception {
		if (!outputFolder.canWrite()) {
            throw new Exception("Output folder is not writeable!");
        }
		
		File[] contents = outputFolder.listFiles();
		boolean safe = true;
		if (contents != null) {
			for (File content : contents) {
				if (content.isDirectory()) {
					if (content.getName().equalsIgnoreCase(config.getString(Keys.TEMPLATE_FOLDER))) {
						safe = false;
					}
					if (content.getName().equalsIgnoreCase(config.getString(Keys.CONTENT_FOLDER))) {
						safe = false;
					}
					if (content.getName().equalsIgnoreCase(config.getString(Keys.ASSET_FOLDER))) {
						safe = false;
					}
				}
			}
		}
		
		if (!safe) {
			throw new Exception(String.format("Output folder '%s' already contains structure!",
                    outputFolder.getAbsolutePath()));
		}
		if (config.getString("example.project."+templateType) != null) {
			File templateFile = new File(templateLocationFolder, config.getString("example.project."+templateType));
			if (!templateFile.exists()) {
				throw new Exception("Cannot find example project file: " + templateFile.getPath());
			}
			ZipUtil.extract(new FileInputStream(templateFile), outputFolder);
		} else {
			throw new Exception("Cannot locate example project type: " + templateType);
		}
	}
}
