package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.render.support.MockCompositeConfiguration;
import org.jbake.template.RenderingException;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbake.app.ConfigUtil.Keys.PAGINATE_INDEX;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class IndexRendererTest {

    @Test
    public void returnsZeroWhenConfigDoesNotRenderIndices() throws RenderingException {
        IndexRenderer renderer = new IndexRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);
        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    public void doesNotRenderWhenConfigDoesNotRenderIndices() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderIndex(anyString());
    }

    @Test
    public void returnsOneWhenConfigRendersIndices() throws RenderingException {
        IndexRenderer renderer = new IndexRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(1);
    }

    @Test
    public void doesRenderWhenConfigDoesNotRenderIndices() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        MockCompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        compositeConfiguration.setProperty(PAGINATE_INDEX, false);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, times(1)).renderIndex("random string");
    }

    @Test(expected = RenderingException.class)
    public void propagatesRenderingException() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        compositeConfiguration.setProperty(PAGINATE_INDEX, false);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        doThrow(new Exception()).when(mockRenderer).renderIndex(anyString());

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderIndex("random string");
    }


    /**
     * @see <a href="https://github.com/jbake-org/jbake/issues/332">Issue 332</a>
     */
    @Test
    public void shouldFallbackToStandardIndexRenderingIfPropertyIsMissing() throws Exception {
        IndexRenderer renderer = new IndexRenderer();

        MockCompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, times(1)).renderIndex("random string");
    }

}


