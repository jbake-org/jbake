package org.jbake.render

import org.assertj.core.api.Assertions
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*

class Error404RendererTest {
    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderError404() {
        val renderer = Error404Renderer()

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.renderError404).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)

        val mockRenderer = mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test fun doesNotRenderWhenConfigDoesNotRenderError404() {
        val renderer = Error404Renderer()

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.renderError404).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderError404(anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersError404() {
        val renderer = Error404Renderer()

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.renderError404).thenReturn(true)
        // ensure a non-null error404 filename so renderer will attempt to render
        `when`(configuration.error404FileName).thenReturn("mock404file.html")

        // Debug assertions to ensure stubbing worked
        Assertions.assertThat(configuration.renderError404).isTrue()
        Assertions.assertThat(configuration.error404FileName).isNotNull()

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(1)
    }

    @Test fun doesRenderWhenConfigDoesNotRenderError404() {
        val renderer = Error404Renderer()
        val error404file = "mock404file.html"

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.renderError404).thenReturn(true)
        `when`(configuration.error404FileName).thenReturn(error404file)

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, times(1)).renderError404(error404file)
    }

    @Test(expected = RenderingException::class)
    fun propogatesRenderingException() {
        val renderer = Error404Renderer()
        val error404file = "mock404file.html"

        val configuration = mock(JBakeConfiguration::class.java)
        `when`(configuration.renderError404).thenReturn(true)
        `when`(configuration.error404FileName).thenReturn(error404file)

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        /// Use doThrow for void method stubbing
        doThrow(RuntimeException()).`when`(mockRenderer).renderError404(anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderError404(error404file)
    }
}
