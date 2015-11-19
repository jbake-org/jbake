package org.jbake.launcher;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.jbake.app.Oven;

public class CustomFSChangeListener implements FileListener {
	
	private LaunchOptions options;
	private CompositeConfiguration config;
	
	public CustomFSChangeListener(LaunchOptions res, CompositeConfiguration config) {
		this.options = res;
		this.config = config;
	}

	@Override
	public void fileCreated(FileChangeEvent event) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("File created: " + event.getFile().getURL());
		exec();
	}

	@Override
	public void fileDeleted(FileChangeEvent event) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("File deleted: " + event.getFile().getURL());
		exec();
	}

	@Override
	public void fileChanged(FileChangeEvent event) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("File changed: " + event.getFile().getURL());
		exec();
	}

	private void exec() {
		final Oven oven = new Oven(options.getSource(), options.getDestination(), config, options.isClearCache());
		oven.setupPaths();
		oven.bake();
	}
	
}
