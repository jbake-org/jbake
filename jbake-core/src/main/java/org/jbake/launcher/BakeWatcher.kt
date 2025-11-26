package org.jbake.launcher

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.impl.DefaultFileMonitor
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Delegate responsible for watching the file system for changes.
 */
class BakeWatcher {

    /**
     * Starts watching the file system for changes to trigger a bake.
     */
    @Deprecated("""use {@link BakeWatcher#start(JBakeConfiguration)} instead""")
    fun start(launchOptions: LaunchOptions, config: CompositeConfiguration) {
        val configuration = JBakeConfigurationFactory().createDefaultJbakeConfiguration(launchOptions.getSource(), config)
        start(configuration)
    }

    /**
     * Starts watching the file system for changes to trigger a bake.
     */
    fun start(config: JBakeConfiguration) {
        try {
            val fsMan = VFS.getManager()
            val listenPath = fsMan.resolveFile(config.contentFolder.toURI())
            val templateListenPath = fsMan.resolveFile(config.templateFolder.toURI())
            val assetPath = fsMan.resolveFile(config.assetFolder.toURI())
            val dataPath = fsMan.resolveFile(config.dataFolder.toURI())

            log.info("Watching for (content, data, template, asset) changes in [{}]", config.sourceFolder!!.path)
            val monitor = DefaultFileMonitor(CustomFSChangeListener(config))
            monitor.isRecursive = true
            monitor.addFile(listenPath)
            monitor.addFile(templateListenPath)
            monitor.addFile(assetPath)
            monitor.addFile(dataPath)
            monitor.start()
        }
        catch (e: FileSystemException) { log.error("Problems watching filesystem changes", e) }
    }

    private val log: Logger = LoggerFactory.getLogger(BakeWatcher::class.java)
}
