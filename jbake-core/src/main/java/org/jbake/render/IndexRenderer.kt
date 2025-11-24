package org.jbake.render

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.template.RenderingException
import java.io.File

class IndexRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderIndex) return 0

        try {
            val fileName = config.indexFileName ?: "index.html"

            //TODO: refactor this. the renderer has a reference to the configuration
            if (config.paginateIndex)
                renderer.renderIndexPaging(fileName)
            else renderer.renderIndex(fileName)
            return 1
        }
        catch (e: Exception) { throw RenderingException(e) }
    }

    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, destination: File,templatesPath: File, config: CompositeConfiguration): Int {

        val configuration = JBakeConfigurationFactory().createDefaultJbakeConfiguration(templatesPath.getParentFile(), config)
        return render(renderer, db, configuration)
    }
}
