package org.jbake.app.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by frank on 12.02.17.
 */
public class JBakeConfigurationFactoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_return_a_default_configuration() throws Exception {
        File sourceFolder = folder.getRoot();
        File destinationFolder = folder.newFolder("output");

        JBakeConfiguration configuration = JBakeConfigurationFactory.createDefaultJbakeConfiguration(sourceFolder,destinationFolder, true);

        assertThat(configuration.getSourceFolder()).isEqualTo(sourceFolder);
        assertThat(configuration.getDestinationFolder()).isEqualTo(destinationFolder);
        assertThat(configuration.getClearCache()).isEqualTo(true);
    }

}