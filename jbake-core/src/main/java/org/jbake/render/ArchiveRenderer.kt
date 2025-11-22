package org.jbake.render

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.template.RenderingException
import java.io.File

class ArchiveRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (config.renderArchive) {
            try {
                renderer.renderArchive(config.archiveFileName!!)
                return 1
            } catch (e: Exception) {
                throw RenderingException(e)
            }
        }
        return 0
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
