package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AssetTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void copy() throws Exception {
		URL assetsUrl = this.getClass().getResource("/assets");
		File assets = new File(assetsUrl.getFile());
		Asset asset = new Asset(assets.getParentFile(), folder.getRoot());
		asset.copy(assets);
		
		File cssFile = new File(folder.getRoot().getPath() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css");
		Assert.assertTrue(cssFile.exists());
		File imgFile = new File(folder.getRoot().getPath() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png");
		Assert.assertTrue(imgFile.exists());
		File jsFile = new File(folder.getRoot().getPath() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js");
		Assert.assertTrue(jsFile.exists());
	}
	
}
