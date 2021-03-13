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

    /**
     * Copy one asset file at a time.
     *
     * @param asset The asset file to copy
     */
    public void copySingleFile(File asset) {
        try {
            if ( !asset.isDirectory() ) {
                String targetPath = config.getDestinationFolder().getCanonicalPath() + File.separatorChar + assetSubPath(asset);
                LOGGER.info("Copying single asset file to [{}]", targetPath);
                copyFile(asset, new File(targetPath));
            } else {
                LOGGER.info("Skip copying single asset file [{}]. Is a directory.", asset.getPath());
            }
        } catch (IOException io) {
            LOGGER.error("Failed to copy the asset file.", io);
        }
    }

    /**
     * Determine if a given file is an asset file.
     * @param path to the file to validate.
     * @return true if the path provided points to a file in the asset folder.
     */
    public boolean isAssetFile(File path) {
        boolean isAsset = false;

        try {
            if(FileUtil.directoryOnlyIfNotIgnored(path.getParentFile())) {
                if (FileUtil.isFileInDirectory(path, config.getAssetFolder())) {
                    isAsset = true;
                } else if (FileUtil.isFileInDirectory(path, config.getContentFolder())
                    && FileUtil.getNotContentFileFilter().accept(path)) {
                    isAsset = true;
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Unable to determine the path to asset file {}", path.getPath(), ioe);
        }
        return isAsset;
    }

    /**
     * Responsible for copying any asset files that exist within the content directory.
     *
     * @param path of the content directory
     */
    public void copyAssetsFromContent(File path) {
        copy(path, config.getDestinationFolder(), FileUtil.getNotContentFileFilter());
    }

    /**
     * Accessor method to the collection of errors generated during the bake
     *
     * @return a list of errors.
     */
    public List<Throwable> getErrors() {
        return new ArrayList<>(errors);
    }

    private String assetSubPath(File asset) throws IOException {
        // First, strip asset folder from file path
        String targetFolder = asset.getCanonicalPath().replace(config.getAssetFolder().getCanonicalPath() + File.separatorChar, "");
        // And just to be sure, let's also remove the content folder, as some assets are copied from here.
        targetFolder = targetFolder.replace(config.getContentFolder().getCanonicalPath() + File.separatorChar, "");
        return targetFolder;
    }

    private void copy(File sourceFolder, File targetFolder, final FileFilter filter) {
        final File[] assets = sourceFolder.listFiles(filter);
        if (assets != null) {
            Arrays.sort(assets);
            for (File asset : assets) {
                final File target = new File(targetFolder, asset.getName());
                if (asset.isFile()) {
                    copyFile(asset, target);
                } else if (asset.isDirectory()) {
                    copy(asset, target, filter);
                }
            }
        }
    }

    private void copyFile(File asset, File targetFolder) {
        try {
            FileUtils.copyFile(asset, targetFolder);
            LOGGER.info("Copying [{}]... done!", asset.getPath());
        } catch (IOException e) {
            LOGGER.error("Copying [{}]... failed!", asset.getPath(), e);
            errors.add(e);
        }
    }
}
