package org.jbake.maven

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.jbake.app.JBakeException
import org.jbake.app.Oven
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import java.io.File

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
 * Runs jbake on a folder
 */
@Mojo(name = "generate", requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
open class GenerateMojo : AbstractMojo() {
    @Parameter(defaultValue = "\${project}")
    protected var project: MavenProject? = null

    /**
     * Location of the Output Directory.
     */
    @Parameter(
        property = "jbake.outputDirectory",
        defaultValue = "\${project.build.directory}/\${project.build.finalName}",
        required = true
    )
    protected var outputDirectory: File? = null

    /**
     * Location of the Output Directory.
     */
    @Parameter(property = "jbake.inputDirectory", defaultValue = "\${project.basedir}/src/main/jbake", required = true)
    protected var inputDirectory: File? = null

    /**
     * Breaks the build when `true` and errors occur during baking in JBake oven.
     */
    @Parameter(property = "jbake.failOnError", defaultValue = "true")
    protected var failOnError: Boolean = false

    /**
     * Set if cache is present or clear
     */
    @Parameter(property = "jbake.isClearCache", defaultValue = "false", required = true)
    protected var isClearCache: Boolean = false

    @Throws(MojoExecutionException::class)
    override fun execute() {
        executeInternal()
    }

    @Throws(MojoExecutionException::class)
    protected open fun executeInternal() {
        reRender()
    }

    @Throws(MojoExecutionException::class)
    protected fun reRender() {
        try {
            // TODO: At some point, reuse Oven
            val oven = Oven(createConfiguration())
            oven.bake()
            if (failOnError && !oven.getErrors().isEmpty()) {
                throw MojoFailureException("Baked with " + oven.getErrors().size + " errors. Check output above for details!")
            }
        } catch (e: Exception) {
            getLog().info("Oops", e)

            throw MojoExecutionException("Failure when running: ", e)
        }
    }

    @Throws(JBakeException::class)
    protected fun createConfiguration(): JBakeConfiguration {
        val jBakeConfiguration =
            JBakeConfigurationFactory().createDefaultJbakeConfiguration(inputDirectory!!, outputDirectory, isClearCache)
        jBakeConfiguration.addConfiguration(this.project!!.properties)
        return jBakeConfiguration
    }
}
