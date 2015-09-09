package org.jbake.app;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.CompositeConfiguration;
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

	private final File destination;
	private final List<String> errors = new LinkedList<String>();
	private final boolean ignoreHidden;

	/**
	 * Creates an instance of Asset.
	 */
	public Asset(File source, File destination, CompositeConfiguration config) {
		this.destination = destination;
		ignoreHidden = config.getBoolean(ConfigUtil.Keys.ASSET_IGNORE_HIDDEN, false);
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
                return !ignoreHidden || !file.isHidden();
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
                        errors.add(e.getMessage());
                    }
                } else if (asset.isDirectory()) {
                    copy(asset, target);
                }
            }
        }
    }

    public List<String> getErrors() {
        return new ArrayList<String>(errors);
    }

}
