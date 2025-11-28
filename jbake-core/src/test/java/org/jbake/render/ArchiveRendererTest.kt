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

        fun returnsZeroWhenConfigDoesNotRenderArchives() {
        val renderer = ArchiveRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderArchive } returns false

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderArchives" {
        val renderer = ArchiveRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderArchive } returns false

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 0) { mockRenderer.renderArchive(any()) }
    }

        fun returnsOneWhenConfigRendersArchives() {
        val renderer = ArchiveRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderArchive } returns true
        every { configuration.archiveFileName } returns "archive.html"

        val contentStore = mockk<ContentStore>()

        val mockRenderer = mockk<Renderer>(relaxed = true)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 1
    }

    "doesRenderWhenConfigDoesRenderArchives" {
        val renderer = ArchiveRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderArchive } returns true
        every { configuration.archiveFileName } returns "mockarchive.html"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 1) { mockRenderer.renderArchive(any()) }
    }

    fun propogatesRenderingException() {
        val renderer = ArchiveRenderer()

        val configuration = mockk<DefaultJBakeConfiguration>()
        every { configuration.renderArchive } returns true
        every { configuration.archiveFileName } returns "mockarchive.html"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        every { mockRenderer.renderArchive(any()) } throws RuntimeException()

        shouldThrow<RenderingException> {
            renderer.render(mockRenderer, contentStore, configuration)
        }

        verify(exactly = 0) { mockRenderer.renderArchive("random string") }
    }
})
