package org.jbake.launcher;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.jbake.app.ConfigUtil;

/**
 * Delegate responsible for watching the file system for changes.
 *
 * @author jmcgarr@gmail.com
 */
public class BakeWatcher {

    /**
     * Starts watching the file system for changes to trigger a bake.
     *
     * @param res Commandline options
     * @param config Configuration settings
     */
    public void start(final LaunchOptions res, CompositeConfiguration config) {
        try {
            FileSystemManager fsMan = VFS.getManager();
            FileObject listenPath = fsMan.resolveFile(res.getSource(), config.getString( ConfigUtil.Keys.CONTENT_FOLDER));
            FileObject templateListenPath = fsMan.resolveFile(res.getSource(), config.getString( ConfigUtil.Keys.TEMPLATE_FOLDER));
            FileObject assetPath = fsMan.resolveFile(res.getSource(), config.getString( ConfigUtil.Keys.ASSET_FOLDER));


            System.out.println("Watching for (content, template, asset) changes in [" + res.getSource() + "]");
            DefaultFileMonitor monitor = new DefaultFileMonitor(new CustomFSChangeListener(res, config));
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
