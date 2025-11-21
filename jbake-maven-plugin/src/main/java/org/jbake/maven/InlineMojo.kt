package org.jbake.maven

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.jbake.app.JBakeException
import spark.Spark

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
 * Runs jbake on a folder while watching and serving a folder with it
 */
@Mojo(name = "inline", requiresDirectInvocation = true, requiresProject = false)
class InlineMojo : WatchMojo() {
    /**
     * Listen Port
     */
    @Parameter(property = "jbake.listenAddress", defaultValue = "127.0.0.1")
    private val listenAddress: String? = null

    /**
     * Index File
     */
    @Parameter(property = "jbake.indexFile", defaultValue = "index.html")
    private val indexFile: String? = null

    /**
     * Listen Port
     */
    @Parameter(property = "jbake.port")
    private val port: Int? = null
        get() {
            if (field == null) {
                try {
                    return createConfiguration().serverPort
                } catch (e: JBakeException) {
                    // ignore since default will be returned
                }
            } else {
                return field
            }
            return 8820
        }

    @Throws(MojoExecutionException::class)
    override fun stopServer() {
        Spark.stop()
    }

    @Throws(MojoExecutionException::class)
    override fun initServer() {
        Spark.externalStaticFileLocation(outputDirectory!!.path)

        Spark.ipAddress(listenAddress)
        Spark.port(this.port!!)

        Spark.init()

        Spark.awaitInitialization()
    }
}
