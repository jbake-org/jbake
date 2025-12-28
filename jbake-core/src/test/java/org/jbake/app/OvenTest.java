package org.jbake.app;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.PropertyList;
import org.jbake.model.DocumentTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OvenTest {

    @TempDir
    private Path root;

    private DefaultJBakeConfiguration configuration;
    private File sourceFolder;
    private ContentStore contentStore;

    @BeforeEach
    void setUp() throws Exception {
        // reset values to known state otherwise previous test case runs can affect the success of this test case
        DocumentTypes.resetDocumentTypes();
        File output = root.resolve("output").toFile();
        sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        configuration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourceFolder);
        configuration.setDestinationFolder(output);
        configuration.setTemplateFolder(new File(sourceFolder, "groovyMarkupTemplates"));
        configuration.setProperty("template.paper.file", "paper.tpl");
    }

    @AfterEach
    void tearDown() {
        if (contentStore != null && contentStore.isActive()) {
            contentStore.close();
            contentStore.shutdown();
        }
    }

    @Test
    void bakeWithAbsolutePaths() {
        configuration.setTemplateFolder(new File(sourceFolder, "groovyMarkupTemplates"));
        configuration.setContentFolder(new File(sourceFolder, "content"));
        configuration.setAssetFolder(new File(sourceFolder, "assets"));

        final Oven oven = new Oven(configuration);
        oven.bake();

        assertThat(oven.getErrors()).isEmpty();
    }

    @Test
    void shouldBakeWithRelativeCustomPaths() throws Exception {
        sourceFolder = TestUtils.getTestResourcesAsSourceFolder("/fixture-custom-relative");
        configuration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourceFolder);
        File assetFolder = new File(configuration.getDestinationFolder(), "css");
        File aboutFile = new File(configuration.getDestinationFolder(), "about.html");
        File blogSubFolder = new File(configuration.getDestinationFolder(), "blog");


        final Oven oven = new Oven(configuration);
        oven.bake();

        assertThat(oven.getErrors()).isEmpty();
        assertThat(configuration.getDestinationFolder()).isNotEmptyDirectory();
        assertThat(assetFolder).isNotEmptyDirectory();
        assertThat(aboutFile).isFile();
        assertThat(aboutFile).isNotEmpty();
        assertThat(blogSubFolder).isNotEmptyDirectory();
    }

    @Test
    void shouldBakeWithAbsoluteCustomPaths() throws Exception {

        // given
        Path source = root.resolve("source");
        Path theme = root.resolve("theme");
        Path destination = root.resolve("destination");

        File originalSource = TestUtils.getTestResourcesAsSourceFolder();
        FileUtils.copyDirectory(originalSource, source.toFile());
        File originalTheme = TestUtils.getTestResourcesAsSourceFolder("/fixture-theme");
        FileUtils.copyDirectory(originalTheme, theme.toFile());

        Path expectedTemplateFolder = theme.resolve("templates");
        Path expectedAssetFolder = theme.resolve("assets");
        Path expectedDestination = destination.resolve("output");

        Path properties = source.resolve("jbake.properties");


        BufferedWriter fw = Files.newBufferedWriter(properties);

        fw.write(PropertyList.ASSET_FOLDER.getKey() + "=" + TestUtils.getOsPath(expectedAssetFolder));
        fw.newLine();
        fw.write(PropertyList.TEMPLATE_FOLDER.getKey() + "=" + TestUtils.getOsPath(expectedTemplateFolder));
        fw.newLine();
        fw.write(PropertyList.DESTINATION_FOLDER.getKey() + "=" + TestUtils.getOsPath(expectedDestination));
        fw.close();

        configuration = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(source.toFile());
        File assetFolder = new File(configuration.getDestinationFolder(), "css");
        File aboutFile = new File(configuration.getDestinationFolder(), "about.html");
        File blogSubFolder = new File(configuration.getDestinationFolder(), "blog");


        final Oven oven = new Oven(configuration);
        oven.bake();

        assertThat(oven.getErrors()).isEmpty();
        assertThat(configuration.getDestinationFolder()).isNotEmptyDirectory();
        assertThat(assetFolder).isNotEmptyDirectory();
        assertThat(aboutFile).isFile();
        assertThat(aboutFile).isNotEmpty();
        assertThat(blogSubFolder).isNotEmptyDirectory();
    }


    @Test
    void shouldThrowExceptionIfSourceFolderDoesNotExist() {
        configuration.setSourceFolder(root.resolve("none").toFile());

        assertThrows(JBakeException.class, () -> new Oven(configuration));
    }

    @Test
    void shouldInstantiateNeededUtensils() throws Exception {

        File template = TestUtils.newFolder(root.toFile(), "template");
        File content = TestUtils.newFolder(root.toFile(), "content");
        File assets = TestUtils.newFolder(root.toFile(), "assets");

        configuration.setTemplateFolder(template);
        configuration.setContentFolder(content);
        configuration.setAssetFolder(assets);

        Oven oven = new Oven(configuration);

        assertThat(oven.getUtensils().getContentStore()).isNotNull();
        assertThat(oven.getUtensils().getCrawler()).isNotNull();
        assertThat(oven.getUtensils().getRenderer()).isNotNull();
        assertThat(oven.getUtensils().getAsset()).isNotNull();
        assertThat(oven.getUtensils().getConfiguration()).isEqualTo(configuration);
    }

    @Test()
    void shouldInspectConfigurationDuringInstantiationFromUtils() {
        configuration.setSourceFolder(root.resolve("none").toFile());

        Utensils utensils = new Utensils();
        utensils.setConfiguration(configuration);

        assertThrows(JBakeException.class, () -> new Oven(utensils));
    }

    @Test
    void shouldCrawlRenderAndCopyAssets() throws Exception {
        File template = TestUtils.newFolder(root.toFile(), "template");
        File content = TestUtils.newFolder(root.toFile(), "content");
        File assets = TestUtils.newFolder(root.toFile(), "assets");

        configuration.setTemplateFolder(template);
        configuration.setContentFolder(content);
        configuration.setAssetFolder(assets);

        contentStore = spy(new ContentStore("memory", "documents" + System.currentTimeMillis()));

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
        verify(renderer, atLeastOnce()).renderIndex(anyString());
        verify(crawler, times(1)).crawl();
        verify(asset, times(1)).copy();
    }

    @Test
    void localeConfiguration() throws Exception {
        String language = configuration.getJvmLocale();

        final Oven oven = new Oven(configuration);
        oven.bake();

        assertThat(Locale.getDefault()).isEqualTo(new Locale(language));
    }

    @Test
    void noLocaleConfiguration() throws Exception {
        configuration.setProperty(PropertyList.JVM_LOCALE.getKey(), null);

        String language = Locale.getDefault().getLanguage();
        final Oven oven = new Oven(configuration);
        oven.bake();

        assertThat(Locale.getDefault()).isEqualTo(new Locale(language));
    }
}
