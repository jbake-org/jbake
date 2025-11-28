package org.jbake.render

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.template.RenderingException

class SitemapRendererTest : StringSpec({
        fun returnsZeroWhenConfigDoesNotRenderSitemaps() {
        val renderer = SitemapRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderSiteMap } returns false

        val contentStore = mockk<ContentStore>()

        val mockRenderer = mockk<Renderer>()
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderSitemaps" {
        val renderer = SitemapRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderSiteMap } returns false

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 0) { mockRenderer.renderSitemap(any()) }
    }

        fun returnsOneWhenConfigRendersSitemaps() {
        val renderer = SitemapRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderSiteMap } returns true

        val contentStore = mockk<ContentStore>()

        val mockRenderer = mockk<Renderer>(relaxed = true)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 1
    }

    "doesRenderWhenConfigDoesRenderSitemaps" {
        val renderer = SitemapRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderSiteMap } returns true
        every { configuration.siteMapFileName } returns "mocksitemap.html"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 1) { mockRenderer.renderSitemap(any()) }
    }

    fun propogatesRenderingException() {
        val renderer = SitemapRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderSiteMap } returns true
        every { configuration.siteMapFileName } returns "mocksitemap.html"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        every { mockRenderer.renderSitemap(any()) } throws RuntimeException()

        shouldThrow<RenderingException> {
            renderer.render(mockRenderer, contentStore, configuration)
        }

        verify(exactly = 0) { mockRenderer.renderSitemap(any()) }
    }
})
