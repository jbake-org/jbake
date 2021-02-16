package org.jbake.app.configuration;


import org.jbake.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class JBakeConfigurationFactoryTest {

    @TempDir File root;

    @Test
    public void shouldReturnDefaultConfigurationWithDefaultFolders() throws Exception {
        File sourceFolder = root;
        File destinationFolder = TestUtils.newFolder(root, "output");
        File templateFolder = TestUtils.newFolder(root, "templates");
        File assetFolder = TestUtils.newFolder(root, "assets");

        JBakeConfiguration configuration = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true);

        assertThat(configuration.getSourceFolder()).isEqualTo(sourceFolder);
        assertThat(configuration.getDestinationFolder()).isEqualTo(destinationFolder);
        assertThat(configuration.getTemplateFolder()).isEqualTo(templateFolder);
        assertThat(configuration.getAssetFolder()).isEqualTo(assetFolder);
        assertThat(configuration.getClearCache()).isEqualTo(true);
    }

    @Test
    public void shouldReturnDefaultConfigurationWithCustomFolders() throws Exception {
        File sourceFolder = root;
        File destinationFolder = TestUtils.newFolder(root,"output/custom");
        File templateFolder = TestUtils.newFolder(root, "templates/custom");
        File assetFolder = TestUtils.newFolder(root, "assets/custom");
        File contentFolder = TestUtils.newFolder(root, "content/custom");


        File properties = new File(sourceFolder, "jbake.properties");

        FileWriter pw = new FileWriter(properties);
        pw.write("template.folder=templates/custom\n");
        pw.write("asset.folder=assets/custom\n");
        pw.write("content.folder=content/custom\n");
        pw.close();

        JBakeConfiguration configuration = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true);

        assertThat(configuration.getTemplateFolderName()).isEqualTo("templates/custom");
        assertThat(configuration.getAssetFolderName()).isEqualTo("assets/custom");
        assertThat(configuration.getContentFolderName()).isEqualTo("content/custom");

        assertThat(configuration.getSourceFolder()).isEqualTo(sourceFolder);
        assertThat(configuration.getDestinationFolder()).isEqualTo(destinationFolder);
        assertThat(configuration.getTemplateFolder()).isEqualTo(templateFolder);
        assertThat(configuration.getAssetFolder()).isEqualTo(assetFolder);
        assertThat(configuration.getContentFolder()).isEqualTo(contentFolder);

        assertThat(configuration.getClearCache()).isEqualTo(true);
    }




    @Test
    public void shouldReturnADefaultConfigurationWithSitehost() throws Exception {
        File sourceFolder = root;
        File destinationFolder = TestUtils.newFolder(root,"output");
        String siteHost = "http://www.jbake.org";

        JBakeConfiguration configuration = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true);

        assertThat(configuration.getSiteHost()).isEqualTo(siteHost);
    }

    @Test
    public void shouldReturnAJettyConfiguration() throws Exception {
        File sourceFolder = root;
        File destinationFolder = TestUtils.newFolder(root,"output");
        String siteHost = "http://localhost:8820";

        JBakeConfiguration configuration = new JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, destinationFolder, true);

        assertThat(configuration.getSiteHost()).isEqualTo(siteHost);
    }

}
