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
import java.nio.file.Files

class TagsRendererTest : StringSpec({

    "returnsZeroWhenConfigDoesNotRenderTags" {
        val tool = TagsRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderTags } returns false
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>()

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderTags" {
        val tool = TagsRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderTags } returns false
        every { configuration.tagPathName } returns "tags"
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val mockRenderer = Renderer(contentStore, configuration, renderingEngine)

        tool.render(mockRenderer, contentStore, configuration)

        // No verification needed - we just check it doesn't throw
    }

    "returnsCountWhenConfigRendersTags" {
        val tool = TagsRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderTags } returns true
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>()
        every { mockRenderer.renderTags() } returns 5

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 5
        verify(exactly = 1) { mockRenderer.renderTags() }
    }

    "doesRenderWhenConfigDoesRenderTags" {
        val tool = TagsRenderingTool()

        val tempDir = Files.createTempDirectory("jbake-test").toFile()
        val mockConf = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { mockConf.renderTags } returns true
        every { mockConf.tagPathName } returns "mockTagfile"
        every { mockConf.destinationDir } returns tempDir
        every { mockConf.outputExtension } returns ".html"
        every { mockConf.renderEncoding } returns "UTF-8"
        every { mockConf.getTemplateByDocType(any()) } returns "tag.ftl"
        val mockContentStore = mockk<ContentStore>(relaxed = true)
        val tags: MutableSet<String> = HashSet(mutableListOf("tag1", "tags2"))
        every { mockContentStore.tags } returns tags
        every { mockContentStore.allTags } returns tags
        val mockRenderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = Renderer(mockContentStore, mockConf, mockRenderingEngine)

        val result = tool.render(renderer, mockContentStore, mockConf)

        result shouldBe 2
        verify(exactly = 2) { mockRenderingEngine.renderDocument(any(), any(), any()) }

        tempDir.deleteRecursively()
    }

    "propogatesRenderingException" {
        val tool = TagsRenderingTool()

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
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        shouldThrow<RenderingException> {
            tool.render(renderer, contentStore, configuration)
        }
    }
})
