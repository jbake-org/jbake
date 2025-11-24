package org.jbake.render;

import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.RenderingException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedRendererTest {

    @Test
    void returnsZeroWhenConfigDoesNotRenderFeeds() throws RenderingException {
        FeedRenderer renderer = new FeedRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderFeed()).thenReturn(false);

        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);
        int renderResponse = renderer.render(mockRenderer, contentStore, configuration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    void doesNotRenderWhenConfigDoesNotRenderFeeds() throws Exception {
        FeedRenderer renderer = new FeedRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderFeed()).thenReturn(false);

        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        renderer.render(mockRenderer, contentStore, configuration);

        verify(mockRenderer, never()).renderFeed(anyString());
    }

    @Test
    void returnsOneWhenConfigRendersFeeds() throws RenderingException {
        FeedRenderer renderer = new FeedRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderFeed()).thenReturn(true);

        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore, configuration);

        assertThat(renderResponse).isEqualTo(1);
    }

    @Test
    void doesRenderWhenConfigDoesRenderFeeds() throws Exception {
        FeedRenderer renderer = new FeedRenderer();
        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderFeed()).thenReturn(true);
        when(configuration.getFeedFileName()).thenReturn("mockfeedfile.xml");

        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        renderer.render(mockRenderer, contentStore, configuration);

        verify(mockRenderer, times(1)).renderFeed(anyString());
    }

    @Test
    void propagatesRenderingException() throws Exception {
        FeedRenderer renderer = new FeedRenderer();

        JBakeConfiguration configuration = mock(DefaultJBakeConfiguration.class);
        when(configuration.getRenderFeed()).thenReturn(true);
        when(configuration.getFeedFileName()).thenReturn("mockfeedfile.xml");

        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        doThrow(new Exception()).when(mockRenderer).renderFeed(anyString());

        assertThatThrownBy(() -> renderer.render(mockRenderer, contentStore, configuration))
            .isInstanceOf(RenderingException.class);

        verify(mockRenderer, never()).renderFeed("random string");
    }

}


