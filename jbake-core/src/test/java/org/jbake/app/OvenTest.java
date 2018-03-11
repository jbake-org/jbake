package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.model.DocumentTypes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class OvenTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private CompositeConfiguration config;
    private File rootPath;
	private File outputPath;

    @Before
    public void setup() throws Exception {
    	// reset values to known state otherwise previous test case runs can affect the success of this test case
    	DocumentTypes.resetDocumentTypes();
		
    	URL sourceUrl = this.getClass().getResource("/fixture");
		rootPath = new File(sourceUrl.getFile());
        if (!rootPath.exists()) {
            throw new Exception("Cannot find base path for test!");
        }
        outputPath = folder.newFolder("destination");
		config = ConfigUtil.load(rootPath);
		config.setProperty(Keys.TEMPLATE_FOLDER, "freemarkerTemplates");
	}

    @Test
    public void bakeWithRelativePaths() throws IOException, ConfigurationException {
        final Oven oven = new Oven(rootPath, outputPath, config, true);
        oven.setupPaths();
        oven.bake();

        assertThat("There shouldn't be any errors: " + oven.getErrors(), oven.getErrors().isEmpty());
    }

    @Test
    public void bakeWithAbsolutePaths() throws IOException, ConfigurationException {
        makeAbsolute(config, rootPath, Keys.TEMPLATE_FOLDER);
        makeAbsolute(config, rootPath, Keys.CONTENT_FOLDER);
        makeAbsolute(config, rootPath, Keys.ASSET_FOLDER);

        final Oven oven = new Oven(rootPath, outputPath, config, true);
        oven.setupPaths();
        oven.bake();

        assertThat("There shouldn't be any errors: " + oven.getErrors(), oven.getErrors().isEmpty());
    }

    @Test
    public void localeConfiguration() throws Exception {
        String language = config.getString(ConfigUtil.Keys.JVM_LOCALE);

        final Oven oven = new Oven(rootPath, outputPath, config, true);
        oven.setupPaths();
        oven.bake();

        assertThat(Locale.getDefault(), is(new Locale(language)));
    }

    @Test
    public void noLocaleConfiguration() throws Exception {
        config.clearProperty(ConfigUtil.Keys.JVM_LOCALE);

        String language = Locale.getDefault().getLanguage();
        final Oven oven = new Oven(rootPath, outputPath, config, true);
        oven.setupPaths();
        oven.bake();

        assertThat(Locale.getDefault(), is(new Locale(language)));
    }

    private void makeAbsolute(Configuration configuration, File source, String key) {
        final File folder = new File(source, configuration.getString(key));
        configuration.setProperty(key, folder.getAbsolutePath());
    }

}
