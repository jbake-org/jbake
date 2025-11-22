package org.jbake.maven.util

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.apache.commons.io.monitor.FileAlterationObserver
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Example to watch a directory (or tree) for changes to files.
 */
class DirWatcher(dir: File) {
    private val observer: FileAlterationObserver = FileAlterationObserver(dir)

    private val monitor: FileAlterationMonitor

    private val changeQueue: BlockingQueue<Long> = ArrayBlockingQueue<Long>(1)

    /**
     * Creates a WatchService and registers the given directory
     */
    init {
        this.monitor = FileAlterationMonitor(1000, observer)

        observer.addListener(object : FileAlterationListenerAdaptor() {
            override fun onFileCreate(file: File?) {
                onUpdated()
            }

            override fun onFileChange(file: File?) {
                onUpdated()
            }
        })
    }

    fun start() {
        monitor.start()
    }

    fun stop() {
        try {
            monitor.stop()
        } catch (exc: Exception) {
        }
    }

    private fun onUpdated() {
        try {
            changeQueue.put(System.currentTimeMillis())
        } catch (iex: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    /**
     * Process all events for keys queued to the watcher
     */
    @Throws(InterruptedException::class)
    fun processEvents(): Long? {
        return changeQueue.poll(1, TimeUnit.SECONDS)
    }
}
