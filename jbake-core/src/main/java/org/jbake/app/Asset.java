package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FileUtils;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
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
 */
public class Asset {

    private static final Logger LOGGER = LoggerFactory.getLogger(Asset.class);
    private final List<Throwable> errors = new LinkedList<>();
    private JBakeConfiguration config;

    /**
     * @param source      Source file for the asset
     * @param destination Destination (target) directory for asset file
     * @param config      Project configuration
     * @deprecated Use {@link #Asset(JBakeConfiguration)} instead.
     * Compatibility constructor.
     * Creates an instance of Asset.
     */
    @Deprecated
    public Asset(File source, File destination, CompositeConfiguration config) {
        this.config = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(source, destination, config);
    }

    /**
     * Creates an instance of Asset.
     *
     * @param config The project configuration. @see{{@link JBakeConfiguration}}
     */
    public Asset(JBakeConfiguration config) {
        this.config = config;
    }

    /**
     * Copy all files from assets folder to destination folder
     * read from configuration
     */
    public void copy() {
        copy(config.getAssetFolder());
    }

    /**
     * Copy all files from supplied path.
     *
     * @param path The starting path
     */
    public void copy(File path) {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (!config.getAssetIgnoreHidden() || !file.isHidden()) && (file.isFile() || FileUtil.directoryOnlyIfNotIgnored(file));
            }
        };
        copy(path, config.getDestinationFolder(), filter);
    }

    private void copy(File sourceFolder, File targetFolder, final FileFilter filter) {

        final File[] assets = sourceFolder.listFiles(filter);
        if (assets != null) {
            Arrays.sort(assets);
            for (File asset : assets) {
                final File target = new File(targetFolder, asset.getName());
                if (asset.isFile()) {
                    try {
                        FileUtils.copyFile(asset, target);
                        LOGGER.info("Copying [{}]... done!", asset.getPath());
                    } catch (IOException e) {
                        LOGGER.error("Copying [{}]... failed!", asset.getPath(), e);
                        errors.add(e);
                    }
                } else if (asset.isDirectory()) {
                    copy(asset, target, filter);
                }
            }
        }
    }

    public void copyAssetsFromContent(File path) {
        copy(path, config.getDestinationFolder(), FileUtil.getNotContentFileFilter());
    }


    public List<Throwable> getErrors() {
        return new ArrayList<>(errors);
    }

}
