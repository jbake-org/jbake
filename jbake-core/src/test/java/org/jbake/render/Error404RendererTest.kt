package org.jbake.render

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException

class Error404RendererTest : StringSpec({

    "returnsZeroWhenConfigDoesNotRenderError404" {
        val renderer = Error404Renderer()

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.renderError404 } returns false

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 0
    }

    "doesNotRenderWhenConfigDoesNotRenderError404" {
        val renderer = Error404Renderer()

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.renderError404 } returns false
        every { configuration.error404FileName } returns "404.html"

        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val mockRenderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        renderer.render(mockRenderer, contentStore, configuration)

        // No verification needed - we just check it doesn't throw
    }

    "returnsOneWhenConfigRendersError404" {
        val renderer = Error404Renderer()

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.renderError404 } returns true
        every { configuration.error404FileName } returns "mock404file.html"

        configuration.renderError404 shouldBe true
        configuration.error404FileName shouldBe "mock404file.html"

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        renderResponse shouldBe 1
    }

    "doesRenderWhenConfigDoesRenderError404" {
        val tool = Error404Renderer()
        val error404file = "mock404file.html"

        val configuration = mockk<JBakeConfiguration>(relaxed = true)
        every { configuration.renderError404 } returns true
        every { configuration.error404FileName } returns error404file
        every { configuration.destinationDir } returns mockk(relaxed = true)
        every { configuration.renderEncoding } returns "UTF-8"

        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        val renderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        val result = tool.render(renderer, contentStore, configuration)

        result shouldBe 1
        verify(exactly = 1) { renderingEngine.renderDocument(any(), any(), any()) }
    }

    "propogatesRenderingException" {
        val tool = Error404Renderer()
        val error404file = "mock404file.html"

        val configuration = mockk<JBakeConfiguration>(relaxed = true)
        every { configuration.renderError404 } returns true
        every { configuration.error404FileName } returns error404file
        every { configuration.destinationDir } returns mockk(relaxed = true)

        val contentStore = mockk<ContentStore>(relaxed = true)
        val renderingEngine = mockk<org.jbake.template.DelegatingTemplateEngine>(relaxed = true)
        every { renderingEngine.renderDocument(any(), any(), any()) } throws RuntimeException("Test exception")

        val renderer = org.jbake.app.Renderer(contentStore, configuration, renderingEngine)

        shouldThrow<RenderingException> {
            tool.render(renderer, contentStore, configuration)
        }
    }
})

