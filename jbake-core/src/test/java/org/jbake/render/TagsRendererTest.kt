package org.jbake.render

import org.assertj.core.api.Assertions.assertThat
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.RenderingException
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*

class TagsRendererTest {
    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderTags() {
        val renderer = TagsRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderTags).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)

        val mockRenderer = mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        assertThat(renderResponse).isEqualTo(0)
    }

    @Test fun doesNotRenderWhenConfigDoesNotRenderTags() {
        val renderer = TagsRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderTags).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderTags(anyString())
    }

    @Test fun returnsOneWhenConfigRendersIndices() {
        val renderer = TagsRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderTags).thenReturn(true)
        `when`(configuration.tagPathName).thenReturn("mocktagpath")

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        val tags: MutableSet<String> = HashSet(mutableListOf("tag1", "tags2"))
        `when`(contentStore.tags).thenReturn(tags)

        `when`(mockRenderer.renderTags(anyString())).thenReturn(1)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        assertThat(renderResponse).isEqualTo(1)
    }

    @Test fun doesRenderWhenConfigDoesRenderIndices() {
        val renderer = TagsRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderTags).thenReturn(true)

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        val tags: MutableSet<String> = HashSet(mutableListOf("tag1", "tags2"))
        `when`(contentStore.tags).thenReturn(tags)
        `when`(configuration.tagPathName).thenReturn("mockTagfile.html")

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, times(1)).renderTags(anyString())
    }

    @Test(expected = RenderingException::class)
    fun propogatesRenderingException() {
        val renderer = TagsRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderTags).thenReturn(true)
        `when`(configuration.tagPathName).thenReturn("mocktagpath/tag")

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        doThrow(RuntimeException()).`when`(mockRenderer).renderTags(anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderTags(anyString())
    }
}


