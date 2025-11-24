package org.jbake.render

import org.assertj.core.api.Assertions.assertThat
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.template.RenderingException
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*

class ArchiveRendererTest {

    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderArchives() {
        val renderer = ArchiveRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderArchive).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    fun doesNotRenderWhenConfigDoesNotRenderArchives() {
        val renderer = ArchiveRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderArchive).thenReturn(false)

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderArchive(anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersArchives() {
        val renderer = ArchiveRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderArchive).thenReturn(true)
        `when`(configuration.archiveFileName).thenReturn("archive.html")

        val contentStore = mock(ContentStore::class.java)

        val mockRenderer = mock(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        assertThat(renderResponse).isEqualTo(1)
    }

    @Test
    fun doesRenderWhenConfigDoesRenderArchives() {
        val renderer = ArchiveRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderArchive).thenReturn(true)
        `when`(configuration.archiveFileName).thenReturn("mockarchive.html")

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, times(1)).renderArchive(anyString())
    }

    @Test(expected = RenderingException::class)
    fun propogatesRenderingException() {
        val renderer = ArchiveRenderer()

        val configuration = mock(DefaultJBakeConfiguration::class.java)
        `when`(configuration.renderArchive).thenReturn(true)
        `when`(configuration.archiveFileName).thenReturn("mockarchive.html")

        val contentStore = mock(ContentStore::class.java)
        val mockRenderer = mock(Renderer::class.java)

        doThrow(RuntimeException()).`when`(mockRenderer).renderArchive(anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        verify(mockRenderer, never()).renderArchive("random string")
    }
}


