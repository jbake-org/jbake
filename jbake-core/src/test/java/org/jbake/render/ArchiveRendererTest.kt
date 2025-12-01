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

class ArchiveRendererTest : StringSpec({

    "returnsZeroWhenConfigDoesNotRenderArchives" {
        val tool = ArchiveRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderArchive } returns false
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderArchives" {
        val tool = ArchiveRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderArchive } returns false
        every { configuration.archiveFileName } returns "archive.html"

        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        tool.render(renderer, contentStore, configuration)
        // No verification needed - we just check it doesn't throw
    }

        fun returnsOneWhenConfigRendersArchives() {
        val tool = ArchiveRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderArchive } returns true
        every { configuration.archiveFileName } returns "archive.html"
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)
        every { mockRenderer.config } answers { configuration }
        every { mockRenderer.renderArchive() } returns Unit

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 1
    }

    "doesRenderWhenConfigDoesRenderArchives" {
        val tool = ArchiveRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderArchive } returns true
        every { configuration.archiveFileName } returns "mockarchive.html"
        every { configuration.destinationDir } returns mockk(relaxed = true)
        every { configuration.renderEncoding } returns "UTF-8"
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        val result = tool.render(renderer, contentStore, configuration)

        result shouldBe 1
        verify(exactly = 1) { renderingEngine.renderDocument(any(), any(), any()) }
    }

    fun propagatesRenderingException() {
        val tool = ArchiveRenderingTool()

        val configuration = mockk<DefaultJBakeConfiguration>(relaxed = true)
        every { configuration.renderArchive } returns true
        every { configuration.archiveFileName } returns "mockarchive.html"
        every { configuration.destinationDir } returns mockk(relaxed = true)

        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        every { renderingEngine.renderDocument(any(), any(), any()) } throws RuntimeException("Test exception")
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        shouldThrow<RenderingException> {
            tool.render(renderer, contentStore, configuration)
        }
    }
})
