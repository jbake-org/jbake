package org.jbake.launcher;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.jbake.app.configuration.JBakeConfiguration;

/**
 * Delegate responsible for watching the file system for changes.
 *
 * @author jmcgarr@gmail.com
 */
public class BakeWatcher {

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

            System.out.println("Watching for (content, template, asset) changes in [" + config.getSourceFolder().getPath() + "]");
            DefaultFileMonitor monitor = new DefaultFileMonitor(new CustomFSChangeListener(config));
            monitor.setRecursive(true);
            monitor.addFile(listenPath);
            monitor.addFile(templateListenPath);
            monitor.addFile(assetPath);
            monitor.start();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }
}
