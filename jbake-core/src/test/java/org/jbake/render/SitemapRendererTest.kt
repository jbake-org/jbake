package org.jbake.render

import org.assertj.core.api.Assertions
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*

class SitemapRendererTest {
    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderSitemaps() {
        val renderer = SitemapRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderSiteMap).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)

        val mockRenderer = mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    fun doesNotRenderWhenConfigDoesNotRenderSitemaps() {
        val renderer = SitemapRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderSiteMap).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderSitemap(anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersSitemaps() {
        val renderer = SitemapRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderSiteMap).thenReturn(true)

        val contentStore = mock(ContentStore::class.java)

        val mockRenderer = mock(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(1)
    }

    @Test
    fun doesRenderWhenConfigDoesRenderSitemaps() {
        val renderer = SitemapRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderSiteMap).thenReturn(true)
        `when`(configuration.siteMapFileName).thenReturn("mocksitemap.html")

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, times(1)).renderSitemap(anyString())
    }

    @Test(expected = RenderingException::class)
    fun propogatesRenderingException() {
        val renderer = SitemapRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderSiteMap).thenReturn(true)
        `when`(configuration.siteMapFileName).thenReturn("mocksitemap.html")

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        doThrow(RuntimeException()).`when`(mockRenderer).renderSitemap(anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderSitemap(anyString())
    }
}


