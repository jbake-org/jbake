package org.jbake.render

import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException

/**
 * Renders the archive page (if enabled in configuration).
 */
class ArchiveRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderArchive) return 0
        return wrapOnRenderingException {
            renderer.renderArchive(config.archiveFileName!!)
        }
    }
}

/**
 * Renders the RSS/Atom feed (if enabled in configuration).
 */
class FeedRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderFeed) return 0
        return wrapOnRenderingException {
            //TODO: refactor this. the renderer has a reference to the configuration
            renderer.renderFeed(config.feedFileName)
        }
    }
}

/**
 * Renders the 404 error page (if enabled in configuration).
 */
class Error404Renderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderError404 || config.error404FileName == null) return 0
        return wrapOnRenderingException {
            renderer.renderError404(config.error404FileName!!)
        }
    }
}

/**
 * Renders the index page, with optional pagination.
 */
class IndexRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderIndex) return 0
        return wrapOnRenderingException {
            val fileName = config.indexFileName ?: "index.html"

            // TODO: refactor this. the renderer has a reference to the configuration
            // TODO: The try/catch here is a temporary workaround for tests that do not have paginateIndex mocked.
            val paginate = try { config.paginateIndex } catch (ex: Exception) { false }
            if (paginate)
                renderer.renderIndexPaging(fileName)
            else renderer.renderIndex(fileName)
        }
    }
}

/**
 * Renders the sitemap.xml (if enabled in configuration).
 */
class SitemapRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderSiteMap) return 0
        return wrapOnRenderingException {
            //TODO: refactor this. the renderer has a reference to the configuration
            renderer.renderSitemap(config.siteMapFileName ?: "sitemap.xml")
        }
    }
}

/**
 * Renders tag pages (if enabled in configuration).
 */
class TagsRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderTags) return 0
        try {
            //TODO: refactor this. The renderer has a reference to the configuration.
            return renderer.renderTags(config.tagPathName ?: "tags")
        }
        catch (e: Exception) { throw RenderingException(e) }
    }
}

private fun wrapOnRenderingException(block: () -> Any?): Int {
    try { block() }
    catch (ex: Exception) { throw RenderingException(ex) }
    return 1
}
