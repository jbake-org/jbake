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

public class SitemapRendererTest {

    @Test
    public void returnsZeroWhenConfigDoesNotRenderSitemaps() throws RenderingException {
        SitemapRenderer renderer = new SitemapRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);
        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    public void doesNotRenderWhenConfigDoesNotRenderSitemaps() throws Exception {
        SitemapRenderer renderer = new SitemapRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderSitemap(anyString());
    }

    @Test
    public void returnsOneWhenConfigRendersSitemaps() throws RenderingException {
        SitemapRenderer renderer = new SitemapRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(1);
    }

    @Test
    public void doesRenderWhenConfigDoesNotRenderSitemaps() throws Exception {
        SitemapRenderer renderer = new SitemapRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, times(1)).renderSitemap("random string");
    }

    @Test(expected = RenderingException.class)
    public void propogatesRenderingException() throws Exception {
        SitemapRenderer renderer = new SitemapRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        doThrow(new Exception()).when(mockRenderer).renderSitemap(anyString());

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderSitemap("random string");
    }

}


