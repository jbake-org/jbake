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

class TagsRendererTest : StringSpec({

    "returnsZeroWhenConfigDoesNotRenderTags" {
        val renderer = TagsRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderTags } returns false

        val contentStore = mockk<ContentStore>()

        val mockRenderer = mockk<Renderer>()
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderTags" {
        val renderer = TagsRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderTags } returns false

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>()

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 0) { mockRenderer.renderTags(any()) }
    }

    "returnsOneWhenConfigRendersIndices" {
        val renderer = TagsRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderTags } returns true
        every { configuration.tagPathName } returns "mocktagpath"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>()

        val tags: MutableSet<String> = HashSet(mutableListOf("tag1", "tags2"))
        every { contentStore.tags } returns tags

        every { mockRenderer.renderTags(any()) } returns 1

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 1
    }

    "doesRenderWhenConfigDoesRenderIndices" {
        val renderer = TagsRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderTags } returns true

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>()

        val tags: MutableSet<String> = HashSet(mutableListOf("tag1", "tags2"))
        every { contentStore.tags } returns tags
        every { configuration.tagPathName } returns "mockTagfile.html"

        every { mockRenderer.renderTags(any()) } returns 1

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 1) { mockRenderer.renderTags(any()) }
    }

    "propogatesRenderingException" {
        val renderer = TagsRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderTags } returns true
        every { configuration.tagPathName } returns "mocktagpath/tag"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>()

        every { mockRenderer.renderTags(any()) } throws RuntimeException()

        shouldThrow<RenderingException> {
            renderer.render(mockRenderer, contentStore, configuration)
        }
    }
})

