package org.jbake.render

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.template.RenderingException
import java.io.File

class Error404Renderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {

        if (!config.renderError404 || config.error404FileName == null)
            return 0

        try {
            renderer.renderError404(config.error404FileName!!)
            return 1
        } catch (e: Exception) {
            throw RenderingException(e)
        }
    }

    @Throws(RenderingException::class)
    override fun render(
        renderer: Renderer,
        db: ContentStore,
        destination: File,
        templatesPath: File,
        config: CompositeConfiguration,
    ): Int {
        val configuration: JBakeConfiguration =
            JBakeConfigurationFactory().createDefaultJbakeConfiguration(templatesPath.getParentFile(), config)
        return render(renderer, db, configuration)
    }
}
