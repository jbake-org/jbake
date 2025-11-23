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

class SitemapRendererTest {
    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderSitemaps() {
        val renderer = SitemapRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderSiteMap).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)

        val mockRenderer = Mockito.mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    fun doesNotRenderWhenConfigDoesNotRenderSitemaps() {
        val renderer = SitemapRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderSiteMap).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderSitemap(ArgumentMatchers.anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersSitemaps() {
        val renderer = SitemapRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderSiteMap).thenReturn(true)

        val contentStore = Mockito.mock(ContentStore::class.java)

        val mockRenderer = Mockito.mock(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(1)
    }

    @Test
    fun doesRenderWhenConfigDoesRenderSitemaps() {
        val renderer = SitemapRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderSiteMap).thenReturn(true)
        Mockito.`when`(configuration.siteMapFileName).thenReturn("mocksitemap.html")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.times(1)).renderSitemap(ArgumentMatchers.anyString())
    }

    @Test(expected = RenderingException::class)
    fun propogatesRenderingException() {
        val renderer = SitemapRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderSiteMap).thenReturn(true)
        Mockito.`when`(configuration.siteMapFileName).thenReturn("mocksitemap.html")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        Mockito.doThrow(RuntimeException()).`when`(mockRenderer).renderSitemap(ArgumentMatchers.anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderSitemap(ArgumentMatchers.anyString())
    }
}


