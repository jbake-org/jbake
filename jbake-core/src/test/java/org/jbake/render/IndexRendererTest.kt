package org.jbake.render;

import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.RenderingException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IndexRendererTest {

    @Test
    public void returnsZeroWhenConfigDoesNotRenderIndices() throws RenderingException {
        IndexRenderer renderer = new IndexRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderIndex()).thenReturn(false);

        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);
        int renderResponse = renderer.render(mockRenderer, contentStore, configuration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    public void doesNotRenderWhenConfigDoesNotRenderIndices() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderIndex()).thenReturn(false);

        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        renderer.render(mockRenderer, contentStore, configuration);

        verify(mockRenderer, never()).renderIndex(anyString());
    }

    @Test
    public void returnsOneWhenConfigRendersIndices() throws RenderingException {
        IndexRenderer renderer = new IndexRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderIndex()).thenReturn(true);

        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore, configuration);

        assertThat(renderResponse).isEqualTo(1);
    }


    @Test(expected = RenderingException.class)
    public void propagatesRenderingException() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderIndex()).thenReturn(true);
        when(configuration.getIndexFileName()).thenReturn("mockindex.html");

        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        doThrow(new Exception()).when(mockRenderer).renderIndex(anyString());

        renderer.render(mockRenderer, contentStore, configuration);

        verify(mockRenderer, never()).renderIndex(anyString());
    }


    /**
     * @see <a href="https://github.com/jbake-org/jbake/issues/332">Issue 332</a>
     */
    @Test
    public void shouldFallbackToStandardIndexRenderingIfPropertyIsMissing() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderIndex()).thenReturn(true);
        when(configuration.getIndexFileName()).thenReturn("mockindex.html");

        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        renderer.render(mockRenderer, contentStore, configuration);

        verify(mockRenderer, times(1)).renderIndex(anyString());
    }

    @Test
    public void shouldRenderPaginatedIndex() throws Exception {

        IndexRenderer renderer = new IndexRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderIndex()).thenReturn(true);
        when(configuration.getPaginateIndex()).thenReturn(true);
        when(configuration.getIndexFileName()).thenReturn("mockindex.html");

        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        renderer.render(mockRenderer, contentStore, configuration);

        verify(mockRenderer, times(1)).renderIndexPaging(anyString());

    }
}


