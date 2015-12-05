package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.template.RenderingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.io.File;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;

public class ArchiveRendererTest {



    @Test
    public void returnsZeroWhenConfigDoesNotRenderArchives() throws RenderingException {
        ArchiveRenderer renderer = new ArchiveRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);
        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    public void doesNotRenderWhenConfigDoesNotRenderArchives() throws Exception {
        ArchiveRenderer renderer = new ArchiveRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderArchive(anyString());
    }

    @Test
    public void returnsOneWhenConfigRendersArchives() throws RenderingException {
        ArchiveRenderer renderer = new ArchiveRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(1);
    }

    @Test
    public void doesRenderWhenConfigDoesNotRenderArchives() throws Exception {
        ArchiveRenderer renderer = new ArchiveRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, times(1)).renderArchive("random string");
    }

    @Test(expected = RenderingException.class)
    public void propogatesRenderingException() throws Exception {
        ArchiveRenderer renderer = new ArchiveRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withInnerBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        doThrow(new Exception()).when(mockRenderer).renderArchive(anyString());

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderArchive("random string");
    }

}


class MockCompositeConfiguration extends CompositeConfiguration {

    private boolean _bool = false;
    private String _string = "random string";

    public MockCompositeConfiguration withInnerBoolean(boolean bool) {
        _bool = bool;
        return this;
    }

    public MockCompositeConfiguration withInnerString(String string) {
        _string = string;
        return this;
    }

    @Override
    public boolean getBoolean(String key) {
        return _bool;
    }

    @Override
    public String getString(String string) {
        return _string;
    }
}