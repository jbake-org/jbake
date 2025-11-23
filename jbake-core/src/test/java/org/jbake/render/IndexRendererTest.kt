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

class IndexRendererTest {
    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderIndices() {
        val renderer = IndexRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderIndex).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)

        val mockRenderer = Mockito.mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    fun doesNotRenderWhenConfigDoesNotRenderIndices() {
        val renderer = IndexRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderIndex).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderIndex(ArgumentMatchers.anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersIndices() {
        val renderer = IndexRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderIndex).thenReturn(true)

        val contentStore = Mockito.mock(ContentStore::class.java)

        val mockRenderer = Mockito.mock(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(1)
    }


    @Test(expected = RenderingException::class)
    fun propagatesRenderingException() {
        val renderer = IndexRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderIndex).thenReturn(true)
        Mockito.`when`(configuration.indexFileName).thenReturn("mockindex.html")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        Mockito.doThrow(Exception()).`when`(mockRenderer).renderIndex(ArgumentMatchers.anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderIndex(ArgumentMatchers.anyString())
    }


    /**
     * @see [Issue 332](https://github.com/jbake-org/jbake/issues/332)
     */
    @Test
    fun shouldFallbackToStandardIndexRenderingIfPropertyIsMissing() {
        val renderer = IndexRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderIndex).thenReturn(true)
        Mockito.`when`(configuration.indexFileName).thenReturn("mockindex.html")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.times(1)).renderIndex(ArgumentMatchers.anyString())
    }

    @Test
    fun shouldRenderPaginatedIndex() {
        val renderer = IndexRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderIndex).thenReturn(true)
        Mockito.`when`(configuration.paginateIndex).thenReturn(true)
        Mockito.`when`(configuration.indexFileName).thenReturn("mockindex.html")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.times(1)).renderIndexPaging(ArgumentMatchers.anyString())
    }
}


