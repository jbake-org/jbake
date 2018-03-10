package org.jbake.app.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class JBakeConfigurationFactoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldReturnDefaultConfigurationWithDefaultFolders() throws Exception {
        File sourceFolder = folder.getRoot();
        File destinationFolder = folder.newFolder("output");
        File templateFolder = new File(folder.getRoot(), "templates");
        File assetFolder = new File(folder.getRoot(), "assets");

        JBakeConfiguration configuration = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true);

        assertThat(configuration.getSourceFolder()).isEqualTo(sourceFolder);
        assertThat(configuration.getDestinationFolder()).isEqualTo(destinationFolder);
        assertThat(configuration.getTemplateFolder()).isEqualTo(templateFolder);
        assertThat(configuration.getAssetFolder()).isEqualTo(assetFolder);
        assertThat(configuration.getClearCache()).isEqualTo(true);
    }

    @Test
    public void shouldReturnADefaultConfigurationWithSitehost() throws Exception {
        File sourceFolder = folder.getRoot();
        File destinationFolder = folder.newFolder("output");
        String siteHost = "http://www.jbake.org";

        JBakeConfiguration configuration = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true);

        assertThat(configuration.getSiteHost()).isEqualTo(siteHost);
    }

    @Test
    public void shouldReturnAJettyConfiguration() throws Exception {
        File sourceFolder = folder.getRoot();
        File destinationFolder = folder.newFolder("output");
        String siteHost = "http://localhost:8820";

        JBakeConfiguration configuration = new JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, destinationFolder, true);

        assertThat(configuration.getSiteHost()).isEqualTo(siteHost);
    }

}