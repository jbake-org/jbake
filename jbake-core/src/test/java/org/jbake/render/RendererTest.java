package org.jbake.render;

import org.jbake.TestUtils;
import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.template.DelegatingTemplateEngine;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RendererTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private DefaultJBakeConfiguration config;
    private File outputPath;

    @Mock
    private ContentStore db;

    @Mock
    private DelegatingTemplateEngine renderingEngine;

    @Before
    public void setup() throws Exception {
        URL sourceUrl = this.getClass().getResource("/fixture");
        File sourcePath = new File(sourceUrl.getFile());
        if (!sourcePath.exists()) {
            throw new Exception("Cannot find base path for test!");
        }
        outputPath = folder.newFolder("output");
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourcePath);
        config.setDestinationFolder(outputPath);
    }

    /**
     * See issue #300
     *
     * @throws Exception
     */
    @Test
    public void testRenderFileWorksWhenPathHasDotInButFileDoesNot() throws Exception {

        Assume.assumeFalse("Ignore running on Windows", TestUtils.isWindows());
        String FOLDER = "real.path";

        final String FILENAME = "about";
        config.setOutputExtension("");
        config.setTemplateFolder(folder.newFolder("templates"));
        Renderer renderer = new Renderer(db, config, renderingEngine);

        Map<String, Object> content = new HashMap<>();
        content.put(Crawler.Attributes.TYPE, "page");
        content.put(Crawler.Attributes.URI, "/" + FOLDER + "/" + FILENAME);
        content.put(Crawler.Attributes.STATUS, "published");

        renderer.render(content);

        File outputFile = new File(outputPath.getAbsolutePath() + File.separatorChar + FOLDER + File.separatorChar + FILENAME);
        assertThat(outputFile).isFile();
    }
}
