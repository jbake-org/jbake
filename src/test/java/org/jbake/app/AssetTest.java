package org.jbake.app;

import java.io.File;
import java.net.URL;


import org.apache.commons.io.FileUtils;
import org.junit.Assert;
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
		Asset asset = new Asset(assets, folder.getRoot());
		asset.copy(assets);
		
		File cssFile = new File(folder.getRoot().getPath() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css");
		Assert.assertTrue(cssFile.exists());
		File imgFile = new File(folder.getRoot().getPath() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png");
		Assert.assertTrue(imgFile.exists());
		File jsFile = new File(folder.getRoot().getPath() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js");
		Assert.assertTrue(jsFile.exists());
	}

	/**
	 * Test the copy of assets if the configuration of directory assets is nor named <b>assets</b>
	 * With Jbake config that's possible to change the property named : 'asset.folder'
	 */
	@Test
	public void copyFilesFromConfigurableFolder() throws Exception{

		File assetsTest = new File(this.getClass().getResource("/assets").getFile());

		File notNamedAssets = folder.newFolder("notNamedAssets");
		//Clone the assets directory in a temporary folder NOT named 'assets'
		FileUtils.copyDirectory(assetsTest,notNamedAssets);
		File finalCopyAssets = folder.newFolder("finalCopyAssets");


		//Simulate the copy like in the oven class
		Asset asset = new Asset(notNamedAssets, finalCopyAssets);
		asset.copySourceFile();

		File cssFile = new File(finalCopyAssets.getPath() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css");
		Assert.assertTrue(cssFile.exists());
		File imgFile = new File(finalCopyAssets.getPath() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png");
		Assert.assertTrue(imgFile.exists());
		File jsFile = new File(finalCopyAssets.getPath() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js");
		Assert.assertTrue(jsFile.exists());
	}

}
