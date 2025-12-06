package org.jbake.launcher

import org.apache.commons.vfs2.FileChangeEvent
import org.apache.commons.vfs2.FileListener
import org.apache.commons.vfs2.FileObject
import org.jbake.app.Oven
import org.jbake.app.configuration.JBakeConfiguration
import org.slf4j.Logger
import org.jbake.util.Logging.logger
import java.io.File

class CustomFileSystemChangeListener(private val config: JBakeConfiguration) : FileListener {

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
        oven.bakeSingleFile(File(file.name.path))
    }

    private val log: Logger by logger()
}
