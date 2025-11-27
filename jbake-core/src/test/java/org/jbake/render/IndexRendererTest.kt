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
        fun returnsZeroWhenConfigDoesNotRenderIndices() {
        val renderer = IndexRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderIndex } returns false

        val contentStore = mockk<ContentStore>()

        val mockRenderer = mockk<Renderer>(relaxed = true)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderIndices" {
        val renderer = IndexRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderIndex } returns false

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 0) { mockRenderer.renderIndex(any()) }
    }

        fun returnsOneWhenConfigRendersIndices() {
        val renderer = IndexRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderIndex } returns true

        val contentStore = mockk<ContentStore>()

        val mockRenderer = mockk<Renderer>(relaxed = true)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 1
    }


    fun propagatesRenderingException() {
        val renderer = IndexRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderIndex } returns true
        every { configuration.indexFileName } returns "mockindex.html"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        every { mockRenderer.renderIndex(any()) } throws RuntimeException()

        shouldThrow<RenderingException> {
            renderer.render(mockRenderer, contentStore, configuration)
        }

        verify(exactly = 0) { mockRenderer.renderIndex(any()) }
    }


    /**
     * @see [Issue 332](https://github.com/jbake-org/jbake/issues/332)
     */
    "shouldFallbackToStandardIndexRenderingIfPropertyIsMissing" {
        val renderer = IndexRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderIndex } returns true
        every { configuration.indexFileName } returns "mockindex.html"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 1) { mockRenderer.renderIndex(any()) }
    }

    "shouldRenderPaginatedIndex" {
        val renderer = IndexRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderIndex } returns true
        every { configuration.paginateIndex } returns true
        every { configuration.indexFileName } returns "mockindex.html"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 1) { mockRenderer.renderIndexPaging(any()) }
    }
})
