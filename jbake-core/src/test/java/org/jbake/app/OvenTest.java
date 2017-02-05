package org.jbake.app;

import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OvenTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private DefaultJBakeConfiguration configuration;
    private File sourceFolder;
    private ContentStore contentStore;

    @Before
    public void setUp() throws Exception {
        sourceFolder = new File(this.getClass().getResource("/fixture").getPath());
        configuration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourceFolder);
        configuration.setDestinationFolder(folder.newFolder("output"));
        configuration.setTemplateFolder(new File(sourceFolder,"freemarkerTemplates"));
    }

    @After
    public void tearDown() throws Exception {
        if (contentStore!=null){
            contentStore.close();
            contentStore.shutdown();
        }
    }

    @Test
    public void bakeWithAbsolutePaths() {
        final Oven oven = new Oven(configuration);
        oven.bake();

        assertThat(oven.getErrors()).isEmpty();
    }

    @Test(expected = JBakeException.class)
    public void should_throw_exception_if_source_folder_does_not_exist() throws Exception {
        configuration.setSourceFolder(new File(folder.getRoot(),"none"));
        Oven oven = new Oven(configuration);
    }

    @Test
    public void should_instantiate_needed_Utensils() throws Exception {

        configuration.setTemplateFolder( folder.newFolder("template") );
        configuration.setTemplateFolder( folder.newFolder("content") );
        configuration.setTemplateFolder( folder.newFolder("assets") );

        Oven oven = new Oven(configuration);

        assertThat(oven.getUtensils().getContentStore()).isNotNull();
        assertThat(oven.getUtensils().getCrawler()).isNotNull();
        assertThat(oven.getUtensils().getRenderer()).isNotNull();
        assertThat(oven.getUtensils().getAsset()).isNotNull();
        assertThat(oven.getUtensils().getConfiguration()).isEqualTo(configuration);

    }

    @Test(expected = JBakeException.class)
    public void should_inspect_configuration_during_instantiation_from_utils() throws Exception {
        configuration.setSourceFolder(new File(folder.getRoot(),"none"));

        Utensils utensils = new Utensils();
        utensils.setConfiguration(configuration);

        Oven oven = new Oven(utensils);
    }

    @Test
    public void should_crawl_render_and_copy_assets() throws Exception {
        configuration.setTemplateFolder( folder.newFolder("template") );
        configuration.setTemplateFolder( folder.newFolder("content") );
        configuration.setTemplateFolder( folder.newFolder("assets") );

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
