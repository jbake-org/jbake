package org.jbake.render

import org.assertj.core.api.Assertions.assertThat
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.template.RenderingException
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*

class FeedRendererTest {

    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderFeeds() {
        val renderer = FeedRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderFeed).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)

        val mockRenderer = mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        assertThat(renderResponse).isEqualTo(0)
    }

    @Test fun doesNotRenderWhenConfigDoesNotRenderFeeds() {
        val renderer = FeedRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderFeed).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderFeed(anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersFeeds() {
        val renderer = FeedRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderFeed).thenReturn(true)

        val contentStore = mock(ContentStore::class.java)

        val mockRenderer = mock(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        assertThat(renderResponse).isEqualTo(1)
    }

    @Test fun doesRenderWhenConfigDoesRenderFeeds() {
        val renderer = FeedRenderer()
        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderFeed).thenReturn(true)
        `when`(configuration.feedFileName).thenReturn("mockfeedfile.xml")

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, times(1)).renderFeed(anyString())
    }

    @Test(expected = RenderingException::class)
    fun propogatesRenderingException() {
        val renderer = FeedRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderFeed).thenReturn(true)
        `when`(configuration.feedFileName).thenReturn("mockfeedfile.xml")

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        doThrow(RuntimeException()).`when`(mockRenderer).renderFeed(ArgumentMatchers.anyString())
        //`when`(mockRenderer.renderFeed(ArgumentMatchers.anyString())).thenThrow(Exception())

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderFeed("random string")
    }
}


