package org.jbake.render

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.DelegatingTemplateEngine
import org.jbake.template.RenderingException

class Error404RendererTest : StringSpec({

    "returnsZeroWhenConfigDoesNotRenderError404" {
        val tool = Error404RenderingTool()

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.renderError404 } returns false
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderError404" {
        val tool = Error404RenderingTool()

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.renderError404 } returns false
        every { configuration.error404FileName } returns "404.html"
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<DelegatingTemplateEngine>(relaxed = true)
        val mockRenderer = Renderer(contentStore, configuration, renderingEngine)

        tool.render(mockRenderer, contentStore, configuration)

        // No verification needed - we just check it doesn't throw
    }

    "returnsOneWhenConfigRendersError404" {
        val tool = Error404RenderingTool()

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.renderError404 } returns true
        every { configuration.error404FileName } returns "mock404file.html"
        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>()
        every { mockRenderer.renderError404() } just runs

        val renderResponse = tool.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 1
        verify(exactly = 1) { mockRenderer.renderError404() }
    }

    "doesRenderWhenConfigDoesRenderError404" {
        val tool = Error404RenderingTool()
        val error404file = "mock404file.html"

        val configuration = mockk<JBakeConfiguration>(relaxed = true)
        every { configuration.renderError404 } returns true
        every { configuration.error404FileName } returns error404file
        every { configuration.destinationDir } returns mockk(relaxed = true)
        every { configuration.renderEncoding } returns "UTF-8"
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<DelegatingTemplateEngine>(relaxed = true)
        val renderer = Renderer(contentStore, configuration, renderingEngine)

        val result = tool.render(renderer, contentStore, configuration)

        result shouldBe 1
        verify(exactly = 1) { renderingEngine.renderDocument(any(), any(), any()) }
    }

    "propogatesRenderingException" {
        val tool = Error404RenderingTool()
        val error404file = "mock404file.html"

        val configuration = mockk<JBakeConfiguration>(relaxed = true)
        every { configuration.renderError404 } returns true
        every { configuration.error404FileName } returns error404file
        every { configuration.destinationDir } returns mockk(relaxed = true)
        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<DelegatingTemplateEngine>(relaxed = true)
        every { renderingEngine.renderDocument(any(), any(), any()) } throws RuntimeException("Test exception")

        val renderer = Renderer(contentStore, configuration, renderingEngine)

        shouldThrow<RenderingException> {
            tool.render(renderer, contentStore, configuration)
        }
    }
})

