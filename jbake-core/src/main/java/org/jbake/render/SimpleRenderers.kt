package org.jbake.render

import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException

/**
 * Renders the archive page (if enabled in configuration).
 * The renderer uses its internal config reference to determine the archive file name.
 */
class ArchiveRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderArchive) return 0
        return wrapOnRenderingException {
            renderer.renderArchive()
        }
    }
}

/**
 * Renders the RSS/Atom feed (if enabled in configuration).
 * The renderer uses its internal config reference to determine the feed file name.
 */
class FeedRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderFeed) return 0
        return wrapOnRenderingException {
            renderer.renderFeed()
        }
    }
}

/**
 * Renders the 404 error page (if enabled in configuration).
 * The renderer uses its internal config reference to determine the error page file name.
 */
class Error404Renderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderError404 || config.error404FileName == null) return 0
        return wrapOnRenderingException {
            renderer.renderError404()
        }
    }
}

/**
 * Renders the index page, with optional pagination.
 * The renderer uses its internal config reference to determine pagination and file name.
 */
class IndexRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderIndex) return 0
        return wrapOnRenderingException {
            // The try/catch here is a temporary workaround for tests that do not have paginateIndex mocked.
            val paginate = try { config.paginateIndex } catch (ex: Exception) { false }
            if (paginate)
                renderer.renderIndexPaging()
            else
                renderer.renderIndex()
        }
    }
}

/**
 * Renders the sitemap.xml (if enabled in configuration).
 * The renderer uses its internal config reference to determine the sitemap file name.
 */
class SitemapRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderSiteMap) return 0
        return wrapOnRenderingException {
            renderer.renderSitemap()
        }
    }
}

/**
 * Renders tag pages (if enabled in configuration).
 * The renderer uses its internal config reference to determine the tag path.
 */
class TagsRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        if (!config.renderTags) return 0
        return wrapOnRenderingException {
            renderer.renderTags()
        }
    }
}

private fun wrapOnRenderingException(block: () -> Any?): Int {
    try { block() }
    catch (ex: Exception) { throw RenderingException(ex) }
    return 1
}
