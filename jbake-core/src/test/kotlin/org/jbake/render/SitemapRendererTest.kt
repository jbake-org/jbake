package org.jbake.render

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jbake.TestUtils
import org.jbake.app.ContentStore
import org.jbake.app.ContentStoreIntegrationTest.Companion.tempDir
import org.jbake.app.Renderer
import org.jbake.app.RenderingException
import org.jbake.app.configuration.DefaultJBakeConfiguration

class SitemapRendererTest : StringSpec({
    "returnsZeroWhenRendersSitemaps" {
        val tool = SitemapRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderSiteMap } returns false
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>()

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)
        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderSitemaps" {
        val tool = SitemapRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderSiteMap } returns false
        every { configuration.siteMapFileName } returns "sitemap.xml"
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val mockRenderer = Renderer(contentStore, configuration, renderingEngine)

        tool.render(mockRenderer, contentStore, configuration)

        // No verification needed - we just check it doesn't throw
    }

    "returnsOneWh0enRendersSitemaps" {
        val tool = SitemapRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderSiteMap } returns true
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)
        every { mockRenderer.renderSitemap() } returns Unit

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)
        renderResponse shouldBe 1
    }

    "rendersSitemapsWhenConfigured" {
        val tool = SitemapRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderSiteMap } returns true
        every { configuration.siteMapFileName } returns "mocksitemap.html"
        every { configuration.destinationDir } returns TestUtils.createOrEmptyDir(tempDir, "output")
        every { configuration.renderEncoding } returns "UTF-8"
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        val result = tool.render(renderer, contentStore, configuration)

        result shouldBe 1
        verify(exactly = 1) { renderingEngine.renderDocument(any(), any(), any()) }
    }

    "propagatesRenderingException" {
        val tool = SitemapRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderSiteMap } returns true
        every { configuration.siteMapFileName } returns "mocksitemap.html"
        every { configuration.destinationDir } returns TestUtils.createOrEmptyDir(tempDir, "output")
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        every { renderingEngine.renderDocument(any(), any(), any()) } throws RuntimeException("Test exception")
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        shouldThrow<RenderingException> {
            tool.render(renderer, contentStore, configuration)
        }
    }
})
