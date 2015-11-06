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
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Asset {

    private static final Logger LOGGER = LoggerFactory.getLogger(Asset.class);

    private File source;
    private File destination;
    private CompositeConfiguration config;
    private final List<String> errors = new LinkedList<String>();
    private final boolean ignoreHidden;

    /**
     * Creates an instance of Asset.
     */
    public Asset(File source, File destination, CompositeConfiguration config) {
        this.source = source;
        this.config = config;
        this.destination = destination;
        ignoreHidden = config.getBoolean(ConfigUtil.Keys.ASSET_IGNORE_HIDDEN, false);
    }

    /**
     * Copy all files from supplied path.
     *
     * @param path The starting path
     */
    public void copy(File path) {
        File[] assets = path.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !ignoreHidden || !file.isHidden();
            }
        });
        if (assets != null) {
            Arrays.sort(assets);
            for (File asset : assets) {
                if (asset.isFile()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Copying [").append(asset.getPath()).append("]...");
                    File destFile = new File(asset.getPath().replace(source.getPath() + File.separator + config.getString(ConfigUtil.Keys.ASSET_FOLDER), destination.getPath()));
                    try {
                        FileUtils.copyFile(asset, destFile);
                        sb.append("done!");
                        LOGGER.info(sb.toString());
                    } catch (IOException e) {
                        sb.append("failed!");
                        LOGGER.error(sb.toString(), e);
                        e.printStackTrace();
                        errors.add(e.getMessage());
                    }
                }

                if (asset.isDirectory()) {
                    copy(asset);
                }
            }
        }
    }

    public List<String> getErrors() {
        return new ArrayList<String>(errors);
    }

}
