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

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 0) { mockRenderer.renderError404(any()) }
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
        val renderer = Error404Renderer()
        val error404file = "mock404file.html"

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.renderError404 } returns true
        every { configuration.error404FileName } returns error404file

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(exactly = 1) { mockRenderer.renderError404(error404file) }
    }

    "propogatesRenderingException" {
        val renderer = Error404Renderer()
        val error404file = "mock404file.html"

        val configuration = mockk<JBakeConfiguration>()
        every { configuration.renderError404 } returns true
        every { configuration.error404FileName } returns error404file

        val contentStore = mockk<ContentStore>()
        val mockRenderer = mockk<Renderer>(relaxed = true)

        every { mockRenderer.renderError404(any()) } throws RuntimeException()

        shouldThrow<RuntimeException> {
            renderer.render(mockRenderer, contentStore, configuration)
        }
    }
})

