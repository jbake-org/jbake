package org.jbake.render;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.vfs2.util.Os;
import org.jbake.app.ConfigUtil;
import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.Renderer;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.template.DelegatingTemplateEngine;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith( MockitoJUnitRunner.class )
public class RendererTest {

	@Rule
    public TemporaryFolder folder = new TemporaryFolder();
	private CompositeConfiguration config;
	private File rootPath;
	private File outputPath;
	
	@Mock private ContentStore db;
	@Mock private DelegatingTemplateEngine renderingEngine;
	
	@Before
    public void setup() throws Exception {
		URL sourceUrl = this.getClass().getResource("/");
		rootPath = new File(sourceUrl.getFile());
        if (!rootPath.exists()) {
            throw new Exception("Cannot find base path for test!");
        }
        outputPath = folder.newFolder("output");
		config = ConfigUtil.load(rootPath);		
	}
	
	/**
	 * See issue #300
	 * 
	 * @throws Exception
	 */
	@Test
    public void testRenderFileWorksWhenPathHasDotInButFileDoesNot() throws Exception {
		String FOLDER = "real.path";

		final String FILENAME = "about";
		config.setProperty(Keys.OUTPUT_EXTENSION, "");
		Renderer renderer = new Renderer(db, outputPath, folder.newFolder("templates"), config, renderingEngine);
		
		Map<String, Object> content = new HashMap<String, Object>();
		content.put(Crawler.Attributes.TYPE, "page");
		content.put(Crawler.Attributes.URI, "/" + FOLDER + "/" + FILENAME);
		content.put(Crawler.Attributes.STATUS, "published");
		
		renderer.render(content);
		
		File outputFile = new File(outputPath.getAbsolutePath() + File.separatorChar + FOLDER + File.separatorChar + FILENAME);
		assertThat(outputFile).isFile();
	}
}
