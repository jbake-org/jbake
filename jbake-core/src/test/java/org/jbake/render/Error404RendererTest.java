package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.render.support.MockCompositeConfiguration;
import org.jbake.template.RenderingException;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

public class Error404RendererTest {
    @Test
    public void returnsZeroWhenConfigDoesNotRenderError404() throws RenderingException {
        Error404Renderer renderer = new Error404Renderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);
        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    public void doesNotRenderWhenConfigDoesNotRenderError404() throws Exception {
        Error404Renderer renderer = new Error404Renderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderError404(anyString());
    }

    @Test
    public void returnsOneWhenConfigRendersError404() throws RenderingException {
        Error404Renderer renderer = new Error404Renderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(1);
    }

    @Test
    public void doesRenderWhenConfigDoesNotRenderError404() throws Exception {
        Error404Renderer renderer = new Error404Renderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, times(1)).renderError404("random string");
    }

    @Test(expected = RenderingException.class)
    public void propogatesRenderingException() throws Exception {
        Error404Renderer renderer = new Error404Renderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        doThrow(new Exception()).when(mockRenderer).renderError404(anyString());

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderError404("random string");
    }
}
