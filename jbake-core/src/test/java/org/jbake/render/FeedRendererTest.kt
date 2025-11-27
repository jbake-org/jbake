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

class FeedRendererTest : StringSpec({

        fun returnsZeroWhenConfigDoesNotRenderFeeds() {
        val renderer = FeedRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderFeed } returns false

        val contentStore = mockk<ContentStore>()

        val mockRenderer = mockk<Renderer>(relaxed = true)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderFeeds" {
        val renderer = FeedRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderFeed } returns false

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 0) { mockRenderer.renderFeed(any()) }
    }

        fun returnsOneWhenConfigRendersFeeds() {
        val renderer = FeedRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderFeed } returns true

        val contentStore = mockk<ContentStore>()

        val mockRenderer = mockk<Renderer>(relaxed = true)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 1
    }

    "doesRenderWhenConfigDoesRenderFeeds" {
        val renderer = FeedRenderer()
        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderFeed } returns true
        every { configuration.feedFileName } returns "mockfeedfile.xml"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 1) { mockRenderer.renderFeed(any()) }
    }

    fun propogatesRenderingException() {
        val renderer = FeedRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderFeed } returns true
        every { configuration.feedFileName } returns "mockfeedfile.xml"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        every { mockRenderer.renderFeed(any()) } throws RuntimeException()

        shouldThrow<RenderingException> {
            renderer.render(mockRenderer, contentStore, configuration)
        }

        verify(exactly = 0) { mockRenderer.renderFeed("random string") }
    }
})
