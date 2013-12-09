package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
		assertTrue(cssFile.exists());
		File imgFile = new File(folder.getRoot().getPath() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png");
		assertTrue(imgFile.exists());
		File jsFile = new File(folder.getRoot().getPath() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js");
		assertTrue(jsFile.exists());
	}

    @Test
    public void fileIsAsset() throws IOException {
        URL assetsUrl = this.getClass().getResource("/assets");
        File assets = new File(assetsUrl.getFile());
        Asset asset = new Asset(assets.getParentFile(), folder.getRoot());
        asset.copy(assets);

        Path cssFile = Paths.get(folder.getRoot().getPath(), "assets", "css", "bootstrap.min.css");
        assertTrue(asset.isAsset(cssFile));

        Path jsFile = Paths.get(folder.getRoot().getPath(), "assets", "js", "html5shiv.js");
        assertTrue(asset.isAsset(jsFile));

        Path htmlFile = Paths.get(folder.getRoot().getPath(), "content", "about.html");
        assertFalse(asset.isAsset(htmlFile));

        Path templateFile = Paths.get(folder.getRoot().getPath(), "template", "header.ftl");
        assertFalse(asset.isAsset(templateFile));
    }

}
