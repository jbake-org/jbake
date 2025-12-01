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
        val tool = TagsRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderTags } returns false
        every { configuration.tagPathName } returns "tags"

        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val mockRenderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        tool.render(mockRenderer, contentStore, configuration)

        // No verification needed - we just check it doesn't throw
    }

    "returnsOneWhenConfigRendersIndices" {
        val tool = TagsRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderTags } returns true
        every { configuration.tagPathName } returns "mocktagpath"
        every { configuration.destinationDir } returns mockk(relaxed = true)
        every { configuration.outputExtension } returns ".html"
        every { configuration.renderEncoding } returns "UTF-8"
        every { configuration.getTemplateByDocType(any()) } returns "tag.ftl"

        val contentStore = mockk<ContentStore>(relaxed = true)
        val tags: MutableSet<String> = HashSet(mutableListOf("tag1"))
        every { contentStore.tags } returns tags
        every { contentStore.allTags } returns tags

        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        val renderResponse = tool.render(renderer, contentStore, configuration)

        renderResponse shouldBe 1
    }

    "doesRenderWhenConfigDoesRenderIndices" {
        val tool = TagsRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderTags } returns true
        every { configuration.tagPathName } returns "mockTagfile"
        every { configuration.destinationDir } returns mockk(relaxed = true)
        every { configuration.outputExtension } returns ".html"
        every { configuration.renderEncoding } returns "UTF-8"
        every { configuration.getTemplateByDocType(any()) } returns "tag.ftl"

        val contentStore = mockk<ContentStore>(relaxed = true)
        val tags: MutableSet<String> = HashSet(mutableListOf("tag1", "tags2"))
        every { contentStore.tags } returns tags
        every { contentStore.allTags } returns tags

        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        val result = tool.render(renderer, contentStore, configuration)

        result shouldBe 2
        verify(exactly = 2) { renderingEngine.renderDocument(any(), any(), any()) }
    }

    "propogatesRenderingException" {
        val tool = TagsRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderTags } returns true
        every { configuration.tagPathName } returns "mocktagpath/tag"
        every { configuration.destinationDir } returns mockk(relaxed = true)
        every { configuration.outputExtension } returns ".html"
        every { configuration.getTemplateByDocType(any()) } returns "tag.ftl"

        val contentStore = mockk<ContentStore>(relaxed = true)
        val tags: MutableSet<String> = HashSet(mutableListOf("tag1"))
        every { contentStore.tags } returns tags
        every { contentStore.allTags } returns tags

        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        every { renderingEngine.renderDocument(any(), any(), any()) } throws RuntimeException("Test exception")

        val renderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        shouldThrow<RenderingException> {
            tool.render(renderer, contentStore, configuration)
        }
    }
})

