package org.jbake.launcher

import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.impl.DefaultFileMonitor
import org.jbake.app.configuration.JBakeConfiguration
import org.slf4j.Logger
import org.jbake.util.Logging.logger

/**
 * Delegate responsible for watching the file system for changes.
 */
class BakeWatcher {


    /**
     * Starts watching the file system for changes to trigger a bake.
     */
    fun start(config: JBakeConfiguration) {
        try {
            val fsMan = VFS.getManager()
            val listenPath = fsMan.resolveFile(config.contentDir.toURI())
            val templateListenPath = fsMan.resolveFile(config.templateDir.toURI())
            val assetPath = fsMan.resolveFile(config.assetDir.toURI())
            val dataPath = fsMan.resolveFile(config.dataDir.toURI())

            log.info("Watching for (content, data, template, asset) changes in [{}]", config.sourceDir!!.path)
            val monitor = DefaultFileMonitor(CustomFileSystemChangeListener(config))
            monitor.isRecursive = true
            monitor.addFile(listenPath)
            monitor.addFile(templateListenPath)
            monitor.addFile(assetPath)
            monitor.addFile(dataPath)
            monitor.start()
        }
        catch (e: FileSystemException) { log.error("Problems watching filesystem changes", e) }
    }

    private val log: Logger by logger()
}
