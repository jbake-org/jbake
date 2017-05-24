package org.jbake.launcher;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.jbake.app.Oven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomFSChangeListener implements FileListener {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(CustomFSChangeListener.class);
	
	private LaunchOptions options;
	private CompositeConfiguration config;
	
	public CustomFSChangeListener(LaunchOptions res, CompositeConfiguration config) {
		this.options = res;
		this.config = config;
	}

	@Override
	public void fileCreated(FileChangeEvent event) throws Exception {
		LOGGER.info("File created event detected: " + event.getFile().getURL());
		exec();
	}

	@Override
	public void fileDeleted(FileChangeEvent event) throws Exception {
		LOGGER.info("File deleted event detected: " + event.getFile().getURL());
		exec();
	}

	@Override
	public void fileChanged(FileChangeEvent event) throws Exception {
		LOGGER.info("File changed event detected: " + event.getFile().getURL());
		exec();
	}

	private void exec() {
		final Oven oven = new Oven(options.getSource(), options.getDestination(), config, options.isClearCache());
		oven.setupPaths();
		oven.bake();
	}
	
}
