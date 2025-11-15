package org.jbake.maven

import org.apache.commons.lang3.StringUtils
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.jbake.maven.util.DirWatcher
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

/*
* Copyright 2013 ingenieux Labs
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * Runs jbake on a folder while watching for changes
 */
@Mojo(name = "watch", requiresDirectInvocation = true, requiresProject = false)
open class WatchMojo : GenerateMojo() {
    @Throws(MojoExecutionException::class)
    public override fun executeInternal() {
        reRender()

        var lastProcessed = System.currentTimeMillis()

        getLog().info(
            "Now listening for changes on path " + inputDirectory.getPath()
        )

        initServer()

        var dirWatcher: DirWatcher? = null

        try {
            dirWatcher = DirWatcher(inputDirectory)
            val done = AtomicBoolean(false)
            val reader = BufferedReader(
                InputStreamReader(System.`in`)
            )

            (object : Thread() {
                override fun run() {
                    try {
                        getLog()
                            .info("Running. Enter a blank line to finish. Anything else forces re-rendering.")

                        while (true) {
                            val line = reader.readLine()

                            if (StringUtils.isBlank(line)) {
                                break
                            }

                            reRender()
                        }
                    } catch (exc: Exception) {
                        getLog().info("Ooops", exc)
                    } finally {
                        done.set(true)
                    }
                }
            }).start()

            dirWatcher.start()

            do {
                val result = dirWatcher.processEvents()

                if (null == result) {
                    // do nothing on purpose
                } else if (result >= lastProcessed) {
                    getLog().info("Refreshing")

                    super.reRender()

                    lastProcessed = System.currentTimeMillis()
                }
            } while (!done.get())
        } catch (exc: Exception) {
            getLog().info("Oops", exc)

            throw MojoExecutionException("Oops", exc)
        } finally {
            getLog().info("Finishing")

            if (null != dirWatcher) dirWatcher.stop()

            stopServer()
        }
    }

    @Throws(MojoExecutionException::class)
    protected open fun stopServer() {
    }

    @Throws(MojoExecutionException::class)
    protected open fun initServer() {
    }
}
