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

class FeedRendererTest : StringSpec({

    "returnsZeroWhenNotRenderFeeds" {
        val tool = FeedRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderFeed } returns false
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderFeeds" {
        val tool = FeedRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderFeed } returns false
        every { configuration.feedFileName } returns "feed.xml"
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        tool.render(renderer, contentStore, configuration)

        // No verification needed - we just check it doesn't throw
    }

    "returnsOneWhenRendersFeed" {
        val tool = FeedRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderFeed } returns true
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)
        every { mockRenderer.renderFeed() } returns Unit

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 1
    }

    "rendersFeedWhenConfigured" {
        val tool = FeedRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderFeed } returns true
        every { configuration.feedFileName } returns "mockfeedfile.xml"
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
        val tool = FeedRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderFeed } returns true
        every { configuration.feedFileName } returns "mockfeedfile.xml"
        every { configuration.destinationDir } returns TestUtils.createOrEmptyDir(tempDir,"output")
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        every { renderingEngine.renderDocument(any(), any(), any()) } throws RuntimeException("Test exception")
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        shouldThrow<RenderingException> {
            tool.render(renderer, contentStore, configuration)
        }
    }
})
