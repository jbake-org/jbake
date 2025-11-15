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
 *
 * @author jmcgarr@gmail.com
 */
class BakeWatcher {
    private val logger: Logger = LoggerFactory.getLogger(BakeWatcher::class.java)

    /**
     * Starts watching the file system for changes to trigger a bake.
     *
     * @param res    Commandline options
     * @param config Configuration settings
     */
    @Deprecated(
        """use {@link BakeWatcher#start(JBakeConfiguration)} instead

      """
    )
    fun start(res: LaunchOptions, config: CompositeConfiguration?) {
        val configuration: JBakeConfiguration =
            JBakeConfigurationFactory().createDefaultJbakeConfiguration(res.getSource(), config)
        start(configuration)
    }

    /**
     * Starts watching the file system for changes to trigger a bake.
     *
     * @param config JBakeConfiguration settings
     */
    fun start(config: JBakeConfiguration) {
        try {
            val fsMan = VFS.getManager()
            val listenPath = fsMan.resolveFile(config.contentFolder!!.toURI())
            val templateListenPath = fsMan.resolveFile(config.templateFolder!!.toURI())
            val assetPath = fsMan.resolveFile(config.assetFolder!!.toURI())
            val dataPath = fsMan.resolveFile(config.dataFolder!!.toURI())

            logger.info(
                "Watching for (content, data, template, asset) changes in [{}]",
                config.sourceFolder!!.getPath()
            )
            val monitor = DefaultFileMonitor(CustomFSChangeListener(config))
            monitor.setRecursive(true)
            monitor.addFile(listenPath)
            monitor.addFile(templateListenPath)
            monitor.addFile(assetPath)
            monitor.addFile(dataPath)
            monitor.start()
        } catch (e: FileSystemException) {
            logger.error("Problems watching filesystem changes", e)
        }
    }
}
