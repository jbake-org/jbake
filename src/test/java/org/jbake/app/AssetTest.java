package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AssetTest {

	private CompositeConfiguration config;
	
	@Before
    public void setup() throws Exception, IOException, URISyntaxException {
		config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
        Assert.assertEquals(".html", config.getString(Keys.OUTPUT_EXTENSION));
	}
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void copy() throws Exception {
		URL assetsUrl = this.getClass().getResource("/assets");
		File assets = new File(assetsUrl.getFile());
		Asset asset = new Asset(assets.getParentFile(), folder.getRoot(), config);
		asset.copy(assets);
		
		File cssFile = new File(folder.getRoot().getPath() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css");
		Assert.assertTrue(cssFile.exists());
		File imgFile = new File(folder.getRoot().getPath() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png");
		Assert.assertTrue(imgFile.exists());
		File jsFile = new File(folder.getRoot().getPath() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js");
		Assert.assertTrue(jsFile.exists());
	}
	
	@Test
	public void copyCustomFolder() throws Exception {
		config.setProperty(ConfigUtil.Keys.ASSET_FOLDER, "media");
		URL assetsUrl = this.getClass().getResource("/media");
		File assets = new File(assetsUrl.getFile());
		Asset asset = new Asset(assets.getParentFile(), folder.getRoot(), config);
		asset.copy(assets);
		
		File favFile = new File(folder.getRoot().getPath() + File.separatorChar + "favicon.ico");
		Assert.assertTrue(favFile.exists());
	}
}
