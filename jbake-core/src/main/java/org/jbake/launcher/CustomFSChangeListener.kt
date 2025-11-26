package org.jbake.launcher

import org.apache.commons.vfs2.FileChangeEvent
import org.apache.commons.vfs2.FileListener
import org.apache.commons.vfs2.FileObject
import org.jbake.app.Oven
import org.jbake.app.configuration.JBakeConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class CustomFSChangeListener(private val config: JBakeConfiguration) : FileListener {

    override fun fileCreated(event: FileChangeEvent) {
        log.info("File created event detected: {}", event.fileObject.url)
        exec(event.fileObject)
    }

    override fun fileDeleted(event: FileChangeEvent) {
        log.info("File deleted event detected: {}", event.fileObject.url)
        exec(event.fileObject)
    }

    override fun fileChanged(event: FileChangeEvent) {
        log.info("File changed event detected: {}", event.fileObject.url)
        exec(event.fileObject)
    }

    private fun exec(file: FileObject) {
        val oven = Oven(config)
        oven.bake(File(file.name.path))
    }

    private val log: Logger = LoggerFactory.getLogger(CustomFSChangeListener::class.java)
}
