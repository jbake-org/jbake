package org.jbake.maven

import org.apache.commons.lang3.StringUtils
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.jbake.maven.util.DirWatcher
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Runs jbake on a directory while watching for changes
 */
@Mojo(name = "watch", requiresDirectInvocation = true, requiresProject = false)
open class WatchMojo : GenerateMojo() {
    @Throws(MojoExecutionException::class)
    public override fun executeInternal() {
        reRender()

        var lastProcessed = System.currentTimeMillis()

        log.info("Watching for file changes in directory: ${inputDirectory!!.path}")
        initServer()

        var dirWatcher: DirWatcher? = null

        try {
            dirWatcher = DirWatcher(inputDirectory!!)
            val done = AtomicBoolean(false)
            val reader = BufferedReader(InputStreamReader(System.`in`))

            (object : Thread() {
                override fun run() {
                    try {
                        log.info("Watch mode active. Press Enter to stop watching, or type any text and press Enter to force re-render.")

                        while (true) {
                            val line = reader.readLine()
                            if (StringUtils.isBlank(line)) break
                            log.info("Manual re-render triggered by user input")
                            reRender()
                        }
                    }
                    catch (exc: Exception) {
                        log.error("Error in input handler thread: ${exc.message}", exc)
                    }
                    finally {
                        done.set(true)
                    }
                }
            }).start()

            dirWatcher.start()

            do {
                val result = dirWatcher.processEvents()

                if (null != result && result >= lastProcessed) {
                    log.info("File system change detected, re-rendering site from: ${inputDirectory!!.path}")
                    super.reRender()
                    lastProcessed = System.currentTimeMillis()
                }
            } while (!done.get())
        }
        catch (exc: Exception) {
            log.error("Watch mode failed: ${exc.message}", exc)
            throw MojoExecutionException("Failed to watch directory: ${inputDirectory!!.path}", exc)
        }
        finally {
            log.info("Shutting down watch mode for directory: ${inputDirectory!!.path}")
            dirWatcher?.stop()
            stopServer()
        }
    }

    @Throws(MojoExecutionException::class)
    protected open fun stopServer() {}

    @Throws(MojoExecutionException::class)
    protected open fun initServer() {}
}
