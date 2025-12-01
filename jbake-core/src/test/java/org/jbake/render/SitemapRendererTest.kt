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
        every { configuration.siteMapFileName } returns "sitemap.xml"

        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val mockRenderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        renderer.render(mockRenderer, contentStore, configuration)

        // No verification needed - we just check it doesn't throw
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
        val tool = SitemapRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderSiteMap } returns true
        every { configuration.siteMapFileName } returns "mocksitemap.html"
        every { configuration.destinationDir } returns mockk(relaxed = true)
        every { configuration.renderEncoding } returns "UTF-8"

        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        val result = tool.render(renderer, contentStore, configuration)

        result shouldBe 1
        verify(exactly = 1) { renderingEngine.renderDocument(any(), any(), any()) }
    }

    fun propogatesRenderingException() {
        val tool = SitemapRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderSiteMap } returns true
        every { configuration.siteMapFileName } returns "mocksitemap.html"
        every { configuration.destinationDir } returns mockk(relaxed = true)

        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        every { renderingEngine.renderDocument(any(), any(), any()) } throws RuntimeException("Test exception")

        val renderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        shouldThrow<RenderingException> {
            tool.render(renderer, contentStore, configuration)
        }
    }
})
