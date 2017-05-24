package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.ContentStore;
import org.jbake.app.Renderer;
import org.jbake.render.support.MockCompositeConfiguration;
import org.jbake.template.RenderingException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TagsRendererTest {
	
	@Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Test
    public void returnsZeroWhenConfigDoesNotRenderTags() throws RenderingException {
        TagsRenderer renderer = new TagsRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);

        Renderer mockRenderer = mock(Renderer.class);
        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    public void doesNotRenderWhenConfigDoesNotRenderTags() throws Exception {
        TagsRenderer renderer = new TagsRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(false);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderTags(anyString());
    }

    @Test
    public void returnsOneWhenConfigRendersIndices() throws Exception {
        TagsRenderer renderer = new TagsRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        Set<String> tags = new HashSet<String>(Arrays.asList("tag1", "tags2"));
        when(contentStore.getTags()).thenReturn(tags);

        when(mockRenderer.renderTags("random string")).thenReturn(1);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        assertThat(renderResponse).isEqualTo(1);
    }

    @Test
    public void doesRenderWhenConfigDoesNotRenderIndices() throws Exception {
        TagsRenderer renderer = new TagsRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        Set<String> tags = new HashSet<String>(Arrays.asList("tag1", "tags2"));
        when(contentStore.getTags()).thenReturn(tags);

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, times(1)).renderTags("random string");
    }

    @Test(expected = RenderingException.class)
    public void propogatesRenderingException() throws Exception {
        TagsRenderer renderer = new TagsRenderer();

        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        ContentStore contentStore = mock(ContentStore.class);
        Renderer mockRenderer = mock(Renderer.class);

        doThrow(new Exception()).when(mockRenderer).renderTags(anyString());

        int renderResponse = renderer.render(mockRenderer, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);

        verify(mockRenderer, never()).renderTags(anyString());
    }
    
    @Test
    public void shouldGenerateTagsHome() throws Exception {
        TagsRenderer renderer = new TagsRenderer();
       
        CompositeConfiguration compositeConfiguration = new MockCompositeConfiguration().withDefaultBoolean(true);
        compositeConfiguration.setProperty(Keys.TAG_PATH, "tags");
        compositeConfiguration.setProperty(Keys.OUTPUT_EXTENSION, ".html");
        compositeConfiguration.setProperty(Keys.RENDER_ENCODING, "UTF-8");
        
        ContentStore contentStore = mock(ContentStore.class);
        
        File destination = folder.newFolder();
        
        //Let it create the files in temporary folder
        Renderer rendere = new Renderer(contentStore, destination, new File("."), compositeConfiguration);
      
        Set<String> tags = new HashSet<String>(Arrays.asList("tag1", "tags2"));
        when(contentStore.getAllTags()).thenReturn(tags);
        
        int renderResponse = renderer.render(rendere, contentStore,
                new File("fake"), new File("fake"), compositeConfiguration);
        
        assertThat(renderResponse).isEqualTo(3);
        
        //Verify that index.html is created as tags home.
        assertThat(new File(destination, "tags/index.html").exists()).isEqualTo(true);
    }

}


