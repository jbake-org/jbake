package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Deals with assets (static files such as css, js or image files).
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 *
 */
public class Asset {

    private static final Logger LOGGER = LoggerFactory.getLogger(Asset.class);

    private final File source;
	private final File destination;
	private CompositeConfiguration config;
	private final List<Throwable> errors = new LinkedList<Throwable>();
	private final boolean ignoreHidden;

	/**
	 * Creates an instance of Asset.
	 *
	 * @param source			Source file for the asset
	 * @param destination 		Destination (target) directory for asset file
	 * @param config			Project configuration
	 */
	public Asset(File source, File destination, CompositeConfiguration config) {
		this.source = source;
		this.destination = destination;
		this.config = config;
		this.ignoreHidden = config.getBoolean(ConfigUtil.Keys.ASSET_IGNORE_HIDDEN, false);
	}

	/**
	 * Copy all files from supplied path.
	 *
	 * @param path	The starting path
	 */
	public void copy(File path) {
        copy(path, destination);
    }

    private void copy(File sourceFolder, File targetFolder) {
        final File[] assets = sourceFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (!ignoreHidden || !file.isHidden()) && (file.isFile() || FileUtil.directoryOnlyIfNotIgnored(file));
            }
        });
        if (assets != null) {
            Arrays.sort(assets);
            for (File asset : assets) {
                final File target = new File(targetFolder, asset.getName());
                if (asset.isFile()) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Copying [").append(asset.getPath()).append("]... ");
                    try {
                        FileUtils.copyFile(asset, target);
                        sb.append("done!");
                        LOGGER.info(sb.toString());
                    } catch (IOException e) {
                        sb.append("failed!");
                        LOGGER.error(sb.toString(), e);
                        errors.add(e);
                    }
                } else if (asset.isDirectory()) {
                    copy(asset, target);
                }
            }
        }
    }
    
    public void copyAssetsFromContent(File path){
    	copyNonContentFiles(path, destination);
    }
    
    private void copyNonContentFiles(File sourceFolder, File targetFolder) {
        final File[] nonContents = sourceFolder.listFiles(FileUtil.getNotContentFileFilter());
        if (nonContents != null) {
            Arrays.sort(nonContents);
            for (File file : nonContents) {
                final File target = new File(targetFolder, file.getName());
                if (file.isFile()) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Copying [").append(file.getPath()).append("]... ");
                    try {
                        FileUtils.copyFile(file, target);
                        sb.append("done!");
                        LOGGER.info(sb.toString());
                    } catch (IOException e) {
                        sb.append("failed!");
                        LOGGER.error(sb.toString(), e);
                        errors.add(e);
                    }
                } else if (file.isDirectory()) {
                	copyNonContentFiles(file, target);
                }
            }
        }
    }

	public List<Throwable> getErrors() {
		return new ArrayList<Throwable>(errors);
	}

}
