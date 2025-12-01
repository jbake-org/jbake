package org.jbake.render

import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException

interface RenderingTool {

    @Throws(RenderingException::class)
    fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int
}
