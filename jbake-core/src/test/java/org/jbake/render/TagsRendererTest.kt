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

class TagsRendererTest {
    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderTags() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderTags).thenReturn(false)

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)

        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    @Throws(Exception::class)
    fun doesNotRenderWhenConfigDoesNotRenderTags() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderTags).thenReturn(false)

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify<Renderer?>(mockRenderer, Mockito.never()).renderTags(ArgumentMatchers.anyString())
    }

    @Test
    @Throws(Exception::class)
    fun returnsOneWhenConfigRendersIndices() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderTags).thenReturn(true)
        Mockito.`when`<Any?>(configuration.tagPathName).thenReturn("mocktagpath")

        val contentStore = Mockito.mock<ContentStore>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        val tags: MutableSet<String> = HashSet<String>(mutableListOf<String>("tag1", "tags2"))
        Mockito.`when`<MutableSet<String>>(contentStore.tags).thenReturn(tags)

        Mockito.`when`<Int?>(mockRenderer.renderTags(ArgumentMatchers.anyString())).thenReturn(1)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(1)
    }

    @Test
    @Throws(Exception::class)
    fun doesRenderWhenConfigDoesRenderIndices() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderTags).thenReturn(true)

        val contentStore = Mockito.mock<ContentStore>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        val tags: MutableSet<String> = HashSet<String>(mutableListOf<String>("tag1", "tags2"))
        Mockito.`when`<MutableSet<String?>>(contentStore.tags).thenReturn(tags)
        Mockito.`when`<Any?>(configuration.tagPathName).thenReturn("mockTagfile.html")

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify<Renderer?>(mockRenderer, Mockito.times(1)).renderTags(ArgumentMatchers.anyString())
    }

    @Test(expected = RenderingException::class)
    @Throws(Exception::class)
    fun propogatesRenderingException() {
        val renderer = TagsRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock<DefaultJBakeConfiguration>(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Any?>(configuration.renderTags).thenReturn(true)
        Mockito.`when`<Any?>(configuration.tagPathName).thenReturn("mocktagpath/tag")

        val contentStore = Mockito.mock<ContentStore?>(ContentStore::class.java)
        val mockRenderer = Mockito.mock<Renderer>(Renderer::class.java)

        Mockito.doThrow(Exception()).`when`<Renderer?>(mockRenderer).renderTags(ArgumentMatchers.anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify<Renderer?>(mockRenderer, Mockito.never()).renderTags(ArgumentMatchers.anyString())
    }
}


