package org.jbake.launcher;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate responsible for watching the file system for changes.
 *
 * @author jmcgarr@gmail.com
 */
public class BakeWatcher {

    private Logger logger = LoggerFactory.getLogger(BakeWatcher.class);

    /**
     * Starts watching the file system for changes to trigger a bake.
     *
     * @deprecated use {@link BakeWatcher#start(JBakeConfiguration)} instead
     *
     * @param res    Commandline options
     * @param config Configuration settings
     */
    @Deprecated
    public void start(final LaunchOptions res, CompositeConfiguration config) {
        JBakeConfiguration configuration = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(res.getSource(), config);
        start(configuration);
    }

    /**
     * Starts watching the file system for changes to trigger a bake.
     *
     * @param config JBakeConfiguration settings
     */
    public void start(JBakeConfiguration config) {
        try {
            FileSystemManager fsMan = VFS.getManager();
            FileObject listenPath = fsMan.resolveFile(config.getContentFolder().toURI());
            FileObject templateListenPath = fsMan.resolveFile(config.getTemplateFolder().toURI());
            FileObject assetPath = fsMan.resolveFile(config.getAssetFolder().toURI());

            logger.info("Watching for (content, template, asset) changes in [{}]", config.getSourceFolder().getPath());
            DefaultFileMonitor monitor = new DefaultFileMonitor(new CustomFSChangeListener(config));
            monitor.setRecursive(true);
            monitor.addFile(listenPath);
            monitor.addFile(templateListenPath);
            monitor.addFile(assetPath);
            monitor.start();
        } catch (FileSystemException e) {
            logger.error("Problems watching filesystem changes", e);
        }
    }
}
