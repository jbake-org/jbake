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
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderFeed).thenReturn(false)

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)

        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    @Throws(Exception::class)
    fun doesNotRenderWhenConfigDoesNotRenderFeeds() {
        val renderer = FeedRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderFeed).thenReturn(false)

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify<Renderer?>(mockRenderer, Mockito.never()).renderFeed(ArgumentMatchers.anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersFeeds() {
        val renderer = FeedRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderFeed).thenReturn(true)

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)

        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(1)
    }

    @Test
    @Throws(Exception::class)
    fun doesRenderWhenConfigDoesRenderFeeds() {
        val renderer = FeedRenderer()
        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderFeed).thenReturn(true)
        Mockito.`when`<Any?>(configuration.feedFileName).thenReturn("mockfeedfile.xml")

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify<Renderer?>(mockRenderer, Mockito.times(1)).renderFeed(ArgumentMatchers.anyString())
    }

    @Test(expected = RenderingException::class)
    @Throws(Exception::class)
    fun propogatesRenderingException() {
        val renderer = FeedRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderFeed).thenReturn(true)
        Mockito.`when`<Any?>(configuration.feedFileName).thenReturn("mockfeedfile.xml")

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        Mockito.doThrow(Exception()).`when`<Renderer?>(mockRenderer).renderFeed(ArgumentMatchers.anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify<Renderer?>(mockRenderer, Mockito.never()).renderFeed("random string")
    }
}


