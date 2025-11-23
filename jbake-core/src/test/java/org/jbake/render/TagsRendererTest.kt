package org.jbake.render

import org.assertj.core.api.Assertions.assertThat
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class TagsRendererTest {
    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderTags() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderTags).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)

        val mockRenderer = Mockito.mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    fun doesNotRenderWhenConfigDoesNotRenderTags() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderTags).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderTags(ArgumentMatchers.anyString())
    }

    @Test
    fun returnsOneWhenConfigRendersIndices() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderTags).thenReturn(true)
        Mockito.`when`(configuration.tagPathName).thenReturn("mocktagpath")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        val tags: MutableSet<String> = HashSet(mutableListOf("tag1", "tags2"))
        Mockito.`when`(contentStore.tags).thenReturn(tags)

        Mockito.`when`(mockRenderer.renderTags(ArgumentMatchers.anyString())).thenReturn(1)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        assertThat(renderResponse).isEqualTo(1)
    }

    @Test
    fun doesRenderWhenConfigDoesRenderIndices() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderTags).thenReturn(true)

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        val tags: MutableSet<String> = HashSet(mutableListOf("tag1", "tags2"))
        Mockito.`when`(contentStore.tags).thenReturn(tags)
        Mockito.`when`(configuration.tagPathName).thenReturn("mockTagfile.html")

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.times(1)).renderTags(ArgumentMatchers.anyString())
    }

    @Test(expected = RenderingException::class)
    fun propogatesRenderingException() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderTags).thenReturn(true)
        Mockito.`when`(configuration.tagPathName).thenReturn("mocktagpath/tag")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        Mockito.doThrow(RuntimeException()).`when`(mockRenderer).renderTags(ArgumentMatchers.anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderTags(ArgumentMatchers.anyString())
    }
}


