package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.render.support.MockCompositeConfiguration;
import org.jbake.template.RenderingException;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class IndexRendererTest {

    @Test
    public void returnsZeroWhenConfigDoesNotRenderIndices() throws RenderingException {
        IndexRenderer renderer = new IndexRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);
        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    public void doesNotRenderWhenConfigDoesNotRenderIndices() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderIndex(anyString());
    }

    @Test
    public void returnsOneWhenConfigRendersIndices() throws RenderingException {
        IndexRenderer renderer = new IndexRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(1);
    }

    @Test
    public void doesRenderWhenConfigDoesNotRenderIndices() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, times(1)).renderIndex("random string");
    }

    @Test(expected = RenderingException.class)
    public void propogatesRenderingException() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        doThrow(new Exception()).when(mockRenderer).renderIndex(anyString());

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderIndex("random string");
    }

}


