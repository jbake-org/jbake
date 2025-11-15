package org.jbake.render

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException
import java.io.File

interface RenderingTool {
    @Throws(RenderingException::class)
    fun render(renderer: Renderer?, db: ContentStore?, config: JBakeConfiguration?): Int

    @Deprecated("")
    @Throws(RenderingException::class)
    fun render(
        renderer: Renderer?,
        db: ContentStore?,
        destination: File?,
        templatesPath: File?,
        config: CompositeConfiguration?
    ): Int
}
