package org.jbake.maven.util

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.apache.commons.io.monitor.FileAlterationObserver
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Watches a directory for file changes and queues modification timestamps.
 */
class DirWatcher(dir: File) {

    private val changeQueue = ArrayBlockingQueue<Long>(1)

    private val monitor = FileAlterationMonitor(1000, FileAlterationObserver(dir).apply {
        addListener(object : FileAlterationListenerAdaptor() {
            override fun onFileCreate(file: File) = recordChange()
            override fun onFileChange(file: File) = recordChange()
        })
    })

    fun start() = monitor.start()

    fun stop() = runCatching { monitor.stop() }

    private fun recordChange() {
        try {
            changeQueue.put(System.currentTimeMillis())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    /** Polls for file change events, returning the timestamp of the last change or null if no changes. */
    fun processEvents(): Long? = changeQueue.poll(1, TimeUnit.SECONDS)
}
