package org.jbake.render

import org.assertj.core.api.Assertions
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class FeedRendererTest {
    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderFeeds() {
        val renderer = FeedRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderFeed).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)

        val mockRenderer = Mockito.mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    fun doesNotRenderWhenConfigDoesNotRenderFeeds() {
        val renderer = FeedRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderFeed).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderFeed(ArgumentMatchers.anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersFeeds() {
        val renderer = FeedRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderFeed).thenReturn(true)

        val contentStore = Mockito.mock(ContentStore::class.java)

        val mockRenderer = Mockito.mock(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(1)
    }

    @Test
    fun doesRenderWhenConfigDoesRenderFeeds() {
        val renderer = FeedRenderer()
        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderFeed).thenReturn(true)
        Mockito.`when`(configuration.feedFileName).thenReturn("mockfeedfile.xml")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.times(1)).renderFeed(ArgumentMatchers.anyString())
    }

    @Test(expected = RenderingException::class)
    fun propogatesRenderingException() {
        val renderer = FeedRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderFeed).thenReturn(true)
        Mockito.`when`(configuration.feedFileName).thenReturn("mockfeedfile.xml")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        Mockito.doThrow(Exception()).`when`(mockRenderer).renderFeed(ArgumentMatchers.anyString())
        //Mockito.`when`(mockRenderer.renderFeed(ArgumentMatchers.anyString())).thenThrow(Exception())

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderFeed("random string")
    }
}


