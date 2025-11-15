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
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderSiteMap).thenReturn(false)

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)

        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    @Throws(Exception::class)
    fun doesNotRenderWhenConfigDoesNotRenderSitemaps() {
        val renderer = SitemapRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderSiteMap).thenReturn(false)

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify<Renderer?>(mockRenderer, Mockito.never()).renderSitemap(ArgumentMatchers.anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersSitemaps() {
        val renderer = SitemapRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderSiteMap).thenReturn(true)

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)

        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(1)
    }

    @Test
    @Throws(Exception::class)
    fun doesRenderWhenConfigDoesRenderSitemaps() {
        val renderer = SitemapRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderSiteMap).thenReturn(true)
        Mockito.`when`<Any?>(configuration.siteMapFileName).thenReturn("mocksitemap.html")

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify<Renderer?>(mockRenderer, Mockito.times(1)).renderSitemap(ArgumentMatchers.anyString())
    }

    @Test(expected = RenderingException::class)
    @Throws(Exception::class)
    fun propogatesRenderingException() {
        val renderer = SitemapRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderSiteMap).thenReturn(true)
        Mockito.`when`<Any?>(configuration.siteMapFileName).thenReturn("mocksitemap.html")

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        Mockito.doThrow(Exception()).`when`<Renderer?>(mockRenderer).renderSitemap(ArgumentMatchers.anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify<Renderer?>(mockRenderer, Mockito.never()).renderSitemap(ArgumentMatchers.anyString())
    }
}


