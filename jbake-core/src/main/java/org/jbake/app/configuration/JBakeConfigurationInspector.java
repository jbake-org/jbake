package org.jbake.app.configuration;

import org.jbake.app.FileUtil;
import org.jbake.app.JBakeException;
import org.jbake.app.JBakeException.SystemExit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class JBakeConfigurationInspector {

    private static final Logger LOGGER = LoggerFactory.getLogger(JBakeConfigurationInspector.class);

    private final JBakeConfiguration configuration;

    public JBakeConfigurationInspector(JBakeConfiguration configuration) {
        this.configuration = configuration;
    }

    public void inspect() throws JBakeException {
        ensureSource();
        ensureTemplateFolder();
        ensureContentFolder();
        ensureDestination();
        checkAssetFolder();
    }

    private void ensureSource() throws JBakeException {
        File source = configuration.getSourceFolder();
        if (!FileUtil.isExistingFolder(source)) {
            throw new JBakeException(SystemExit.CONFIGURATION_ERROR, "Error: Source folder must exist: " + source.getAbsolutePath());
        }
        if (!configuration.getSourceFolder().canRead()) {
            throw new JBakeException(SystemExit.CONFIGURATION_ERROR, "Error: Source folder is not readable: " + source.getAbsolutePath());
        }
    }

    private void ensureTemplateFolder() {
        File path = configuration.getTemplateFolder();
        checkRequiredFolderExists(JBakeProperty.TEMPLATE_FOLDER, path);
    }

    private void ensureContentFolder() {
        File path = configuration.getContentFolder();
        checkRequiredFolderExists(JBakeProperty.CONTENT_FOLDER, path);
    }

    private void ensureDestination() {
        File destination = configuration.getDestinationFolder();
        if (!destination.exists()) {
            destination.mkdirs();
        }
        if (!destination.canWrite()) {
            throw new JBakeException(SystemExit.CONFIGURATION_ERROR, "Error: Destination folder is not writable: " + destination.getAbsolutePath());
        }
    }

    private void checkAssetFolder() {
        File path = configuration.getAssetFolder();
        if (!path.exists()) {
            LOGGER.warn("No asset folder '{}' was found!", path.getAbsolutePath());
        }
    }

    private void checkRequiredFolderExists(String folderName, File path) {
        if (!FileUtil.isExistingFolder(path)) {
            throw new JBakeException(SystemExit.CONFIGURATION_ERROR, "Error: Required folder cannot be found! Expected to find [" + folderName + "] at: " + path.getAbsolutePath());
        }
    }


}
