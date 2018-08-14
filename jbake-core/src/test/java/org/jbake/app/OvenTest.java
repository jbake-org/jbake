package org.jbake.app;

import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.model.DocumentTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OvenTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private DefaultJBakeConfiguration configuration;
    private File sourceFolder;
    private ContentStore contentStore;

    @Before
    public void setUp() throws Exception {
        // reset values to known state otherwise previous test case runs can affect the success of this test case
        DocumentTypes.resetDocumentTypes();
        sourceFolder = new File(this.getClass().getResource("/fixture").getPath());
        configuration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourceFolder);
        configuration.setDestinationFolder(folder.newFolder("output"));
        configuration.setTemplateFolder(new File(sourceFolder,"freemarkerTemplates"));
    }

    @After
    public void tearDown() {
        if (contentStore!=null && contentStore.isActive()){
            contentStore.close();
            contentStore.shutdown();
        }
    }

    @Test
    public void bakeWithAbsolutePaths() {
        configuration.setTemplateFolder( new File(sourceFolder, "freemarkerTemplates") );
        configuration.setContentFolder( new File(sourceFolder, "content") );
        configuration.setAssetFolder( new File(sourceFolder, "assets") );

        final Oven oven = new Oven(configuration);
        oven.bake();

        assertThat(oven.getErrors()).isEmpty();
    }

    @Test(expected = JBakeException.class)
    public void shouldThrowExceptionIfSourceFolderDoesNotExist() {
        configuration.setSourceFolder(new File(folder.getRoot(),"none"));
        new Oven(configuration);
    }

    @Test
    public void shouldInstantiateNeededUtensils() throws Exception {

        configuration.setTemplateFolder( folder.newFolder("template") );
        configuration.setContentFolder( folder.newFolder("content") );
        configuration.setAssetFolder( folder.newFolder("assets") );

        Oven oven = new Oven(configuration);

        assertThat(oven.getUtensils().getContentStore()).isNotNull();
        assertThat(oven.getUtensils().getCrawler()).isNotNull();
        assertThat(oven.getUtensils().getRenderer()).isNotNull();
        assertThat(oven.getUtensils().getAsset()).isNotNull();
        assertThat(oven.getUtensils().getConfiguration()).isEqualTo(configuration);

    }

    @Test(expected = JBakeException.class)
    public void shouldInspectConfigurationDuringInstantiationFromUtils() {
        configuration.setSourceFolder(new File(folder.getRoot(),"none"));

        Utensils utensils = new Utensils();
        utensils.setConfiguration(configuration);

        new Oven(utensils);
    }

    @Test
    public void shouldCrawlRenderAndCopyAssets() throws Exception {
        configuration.setTemplateFolder( folder.newFolder("template") );
        configuration.setContentFolder( folder.newFolder("content") );
        configuration.setAssetFolder( folder.newFolder("assets") );

        contentStore = spy(new ContentStore("memory", "documents"+ System.currentTimeMillis()));

        Crawler crawler = mock(Crawler.class);
        Renderer renderer = mock(Renderer.class);
        Asset asset = mock(Asset.class);

        Utensils utensils = new Utensils();
        utensils.setConfiguration(configuration);
        utensils.setContentStore(contentStore);
        utensils.setRenderer(renderer);
        utensils.setCrawler(crawler);
        utensils.setAsset(asset);

        Oven oven = new Oven(utensils);

        oven.bake();

        verify(contentStore, times(1)).startup();
        verify(renderer,atLeastOnce()).renderIndex(anyString());
        verify(crawler,times(1)).crawl();
        verify(asset,times(1)).copy();
    }
}
