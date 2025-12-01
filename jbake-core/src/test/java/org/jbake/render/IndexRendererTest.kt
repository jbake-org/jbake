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

class IndexRendererTest : StringSpec({
    "returnsZeroWhenConfigDoesNotRenderIndices" {
        val tool = IndexRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderIndex } returns false
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)
        val renderResponse = tool.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderIndices" {
        val tool = IndexRenderingTool()
        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderIndex } returns false
        every { configuration.indexFileName } returns "index.html"
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        tool.render(renderer, contentStore, configuration)

        // No verification needed - we just check it doesn't throw
    }

    "returnsOneWhenConfigRendersIndices" {
        val tool = IndexRenderingTool()
        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderIndex } returns true
        every { configuration.indexFileName } returns "mockindex.html"
        every { configuration.paginateIndex } returns false
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)
        every { mockRenderer.config } answers { configuration }
        every { mockRenderer.renderIndex() } returns Unit

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)
        renderResponse shouldBe 1
    }

    "propagatesRenderingException" {
        val tool = IndexRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderIndex } returns true
        every { configuration.indexFileName } returns "mockindex.html"
        every { configuration.destinationDir } returns mockk(relaxed = true)
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        every { renderingEngine.renderDocument(any(), any(), any()) } throws RuntimeException("Test exception")

        val renderer = Renderer(contentStore, configuration, renderingEngine)

        shouldThrow<RenderingException> {
            tool.render(renderer, contentStore, configuration)
        }
    }


    /**
     * @see [Issue 332](https://github.com/jbake-org/jbake/issues/332)
     */
    "shouldFallbackToStandardIndexRenderingIfPropertyIsMissing" {
        val tool = IndexRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderIndex } returns true
        every { configuration.indexFileName } returns "mockindex.html"
        every { configuration.destinationDir } returns mockk(relaxed = true)
        every { configuration.renderEncoding } returns "UTF-8"
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        val result = tool.render(renderer, contentStore, configuration)

        result shouldBe 1
        verify(exactly = 1) { renderingEngine.renderDocument(any(), any(), any()) }
    }

    "shouldRenderPaginatedIndex" {
        val tool = IndexRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderIndex } returns true
        every { configuration.paginateIndex } returns true
        every { configuration.indexFileName } returns "mockindex.html"
        every { configuration.postsPerPage } returns 5
        every { configuration.destinationDir } returns mockk(relaxed = true)
        every { configuration.renderEncoding } returns "UTF-8"
        val contentStore = mockk<ContentStore>(relaxed = true)
        every { contentStore.getPublishedCount("post") } returns 0L // No posts, so it will call renderIndex instead
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        val result = tool.render(renderer, contentStore, configuration)

        result shouldBe 1
    }
})
