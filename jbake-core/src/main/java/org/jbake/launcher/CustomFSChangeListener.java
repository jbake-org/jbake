package org.jbake.launcher;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.jbake.app.Oven;
import org.jbake.app.configuration.JBakeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CustomFSChangeListener implements FileListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(CustomFSChangeListener.class);

    private JBakeConfiguration config;

    public CustomFSChangeListener(JBakeConfiguration config) {
        this.config = config;
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        LOGGER.info("File created event detected: {}", event.getFile().getURL());
        exec(event.getFile());
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        LOGGER.info("File deleted event detected: {}", event.getFile().getURL());
        exec(event.getFile());
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        LOGGER.info("File changed event detected: {}", event.getFile().getURL());
        exec(event.getFile());
    }

    private void exec(FileObject file) {
        final Oven oven = new Oven(config);
        oven.bake(new File(file.getName().getPath()));
    }
}
