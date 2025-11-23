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

class ArchiveRendererTest {
    @Test
    @Throws(RenderingException::class)
    fun returnsZeroWhenConfigDoesNotRenderArchives() {
        val renderer = ArchiveRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderArchive).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)

        val mockRenderer = Mockito.mock(Renderer::class.java)
        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    fun doesNotRenderWhenConfigDoesNotRenderArchives() {
        val renderer = ArchiveRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderArchive).thenReturn(false)

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderArchive(ArgumentMatchers.anyString())
    }

    @Test
    @Throws(RenderingException::class)
    fun returnsOneWhenConfigRendersArchives() {
        val renderer = ArchiveRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`<Boolean>(configuration.renderArchive).thenReturn(true)
        Mockito.`when`<String>(configuration.archiveFileName).thenReturn("archive.html")

        val contentStore = Mockito.mock(ContentStore::class.java)

        val mockRenderer = Mockito.mock(Renderer::class.java)

        val renderResponse = renderer.render(mockRenderer, contentStore, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(1)
    }

    @Test
    fun doesRenderWhenConfigDoesRenderArchives() {
        val renderer = ArchiveRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderArchive).thenReturn(true)
        Mockito.`when`(configuration.archiveFileName).thenReturn("mockarchive.html")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.times(1)).renderArchive(ArgumentMatchers.anyString())
    }

    @Test(expected = RenderingException::class)
    fun propogatesRenderingException() {
        val renderer = ArchiveRenderer()

        val configuration: JBakeConfiguration =
            Mockito.mock(DefaultJBakeConfiguration::class.java)
        Mockito.`when`(configuration.renderArchive).thenReturn(true)
        Mockito.`when`(configuration.archiveFileName).thenReturn("mockarchive.html")

        val contentStore = Mockito.mock(ContentStore::class.java)
        val mockRenderer = Mockito.mock(Renderer::class.java)

        Mockito.doThrow(Exception()).`when`(mockRenderer).renderArchive(ArgumentMatchers.anyString())

        renderer.render(mockRenderer, contentStore, configuration)

        Mockito.verify(mockRenderer, Mockito.never()).renderArchive("random string")
    }
}


