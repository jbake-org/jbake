package org.jbake.launcher

import org.apache.commons.vfs2.FileChangeEvent
import org.apache.commons.vfs2.FileListener
import org.apache.commons.vfs2.FileObject
import org.jbake.app.Oven
import org.jbake.app.configuration.JBakeConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class CustomFSChangeListener(private val config: JBakeConfiguration?) : FileListener {
    @Throws(Exception::class)
    override fun fileCreated(event: FileChangeEvent) {
        LOGGER.info("File created event detected: {}", event.getFileObject().getURL())
        exec(event.getFileObject())
    }

    @Throws(Exception::class)
    override fun fileDeleted(event: FileChangeEvent) {
        LOGGER.info("File deleted event detected: {}", event.getFileObject().getURL())
        exec(event.getFileObject())
    }

    @Throws(Exception::class)
    override fun fileChanged(event: FileChangeEvent) {
        LOGGER.info("File changed event detected: {}", event.getFileObject().getURL())
        exec(event.getFileObject())
    }

    private fun exec(file: FileObject) {
        val oven = Oven(config)
        oven.bake(File(file.getName().getPath()))
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CustomFSChangeListener::class.java)
    }
}
