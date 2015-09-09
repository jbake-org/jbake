package org.jbake.app;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.model.DocumentTypes;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.orientechnologies.orient.core.Orient;

public class OvenTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void reset() {
        DocumentTypes.resetDocumentTypes();
    }

    @Before
    public void startup() {
        Orient.instance().startup();
    }

    @Test
    public void bakeWithRelativePaths() throws IOException, ConfigurationException {
        final File source = new File(OvenTest.class.getResource("/").getFile());
        final File destination = folder.newFolder("destination");
        final CompositeConfiguration configuration = ConfigUtil.load(source);
        configuration.setProperty(Keys.DESTINATION_FOLDER, destination.getAbsolutePath());

        final Oven oven = new Oven(source, destination, configuration, true);
        oven.setupPaths();
        oven.bake();

        assertThat("There shouldn't be any errors: " + oven.getErrors(), oven.getErrors().isEmpty());
    }

    @Test
    public void bakeWithAbsolutePaths() throws IOException, ConfigurationException {
        final File source = new File(OvenTest.class.getResource("/").getFile());
        final File destination = folder.newFolder("destination");
        final CompositeConfiguration configuration = ConfigUtil.load(source);
        makeAbsolute(configuration, source, Keys.TEMPLATE_FOLDER);
        makeAbsolute(configuration, source, Keys.CONTENT_FOLDER);
        makeAbsolute(configuration, source, Keys.ASSET_FOLDER);
        configuration.setProperty(Keys.DESTINATION_FOLDER, destination.getAbsolutePath());

        final Oven oven = new Oven(source, destination, configuration, true);
        oven.setupPaths();
        oven.bake();

        assertThat("There shouldn't be any errors: " + oven.getErrors(), oven.getErrors().isEmpty());
    }

    private void makeAbsolute(Configuration configuration, File source, String key) {
        final File folder = new File(source, configuration.getString(key));
        configuration.setProperty(key, folder.getAbsolutePath());
    }

}
