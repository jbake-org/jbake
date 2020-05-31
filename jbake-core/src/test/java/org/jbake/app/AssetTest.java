package org.jbake.app;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(TempDirectory.class)
public class AssetTest extends LoggingTest {

    public Path folder;
    private DefaultJBakeConfiguration config;
    private File fixtureDir;


    @BeforeEach
    public void setup(@TempDir Path folder) throws Exception {
        fixtureDir = new File(this.getClass().getResource("/fixture").getFile());
        this.folder = folder;
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(fixtureDir);
        config.setDestinationFolder(folder.toFile());
        Assertions.assertEquals(".html", config.getOutputExtension());
    }


    @Test
    public void testCopy() throws Exception {
        Asset asset = new Asset(config);
        asset.copy();
        File cssFile = new File(folder.toString() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css");
        Assertions.assertTrue(cssFile.exists(), () -> "File " + cssFile.getAbsolutePath() + " does not exist");
        File imgFile = new File(folder.toString() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png");
        Assertions.assertTrue(imgFile.exists(), () -> "File " + imgFile.getAbsolutePath() + " does not exist");
        File jsFile = new File(folder.toString() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js");
        Assertions.assertTrue(jsFile.exists(), () -> "File " + jsFile.getAbsolutePath() + " does not exist");

        Assertions.assertTrue(asset.getErrors().isEmpty(), "Errors during asset copying");
    }

    @Test
    public void testCopySingleFile() throws Exception {
        Asset asset = new Asset(config);
        String cssSubPath = File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css";
        String contentImgPath = File.separatorChar + "blog" + File.separatorChar + "2013" + File.separatorChar
            + "images" + File.separatorChar + "custom-image.jpg";

        // Copy single Asset File
        File expected = new File(folder.toString() + cssSubPath);
        Assertions.assertFalse(expected.exists(), "cssFile should not exist before running the test; avoids false positives");
        File cssFile = new File(fixtureDir.getPath() + File.separatorChar + "assets" + cssSubPath);
        asset.copySingleFile(cssFile);
        Assertions.assertTrue(expected.exists(), "Css asset file did not copy");

        // Copy single Content file
        expected = new File(folder.toString() + contentImgPath);
        Assertions.assertFalse(expected.exists(), "content image file should not exist before running the test");
        File imgFile = new File(fixtureDir.getPath() + File.separatorChar + "content" + contentImgPath);
        asset.copySingleFile(imgFile);
        Assertions.assertTrue(expected.exists(), "Content img file did not copy");
    }

    @Test
    public void shouldSkipCopyingSingleFileIfDirectory() throws IOException {

        Asset asset = new Asset(config);

        File emptyDir = new File(folder.toFile(),"emptyDir");
        emptyDir.mkdir();
        File expectedDir = new File(fixtureDir.getCanonicalPath(), "emptyDir");

        asset.copySingleFile(emptyDir);

        Assertions.assertFalse(expectedDir.exists(), "Directory should be skipped");
    }

    @Test
    public void shouldLogSkipCopyingSingleFileIfDirectory() throws IOException {

        Asset asset = new Asset(config);
        File emptyDir = new File(folder.toFile(),"emptyDir");
        emptyDir.mkdir();

        asset.copySingleFile(emptyDir);

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());

        LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        assertThat(loggingEvent.getMessage()).isEqualTo("Skip copying single asset file [{}]. Is a directory.");

    }

    @Test
    public void testCopyCustomFolder() throws Exception {
        config.setAssetFolder(new File(config.getSourceFolder(), "/media"));
        Asset asset = new Asset(config);
        asset.copy();

        File favFile = new File(folder.toString() + File.separatorChar + "favicon.ico");
        Assertions.assertTrue(favFile.exists(), () -> "File " + favFile.getAbsolutePath() + " does not exist");

        Assertions.assertTrue(asset.getErrors().isEmpty(), "Errors during asset copying");
    }

    @Test
    public void testCopyIgnore() throws Exception {
        File assetFolder = new File(folder.toFile(), "ignoredAssets");
        assetFolder.mkdirs();
        FileUtils.copyDirectory(new File(this.getClass().getResource("/fixture/ignorables").getFile()), assetFolder);
        config.setAssetFolder(assetFolder);
        config.setAssetIgnoreHidden(true);
        TestUtils.hideAssets(assetFolder);
        Asset asset = new Asset(config);
        asset.copy(assetFolder);

        File testFile = new File(folder.toFile(), "test.txt");
        Assertions.assertTrue(testFile.exists(), () -> "File " + testFile.getAbsolutePath() + " does not exist");
        File testIgnoreFile = new File(folder.toFile(), ".test.txt");
        Assertions.assertFalse(testIgnoreFile.exists(), () -> "File " + testIgnoreFile.getAbsolutePath() + " does exist");

        Assertions.assertTrue(asset.getErrors().isEmpty(), "Errors during asset copying");
    }


    /**
     * Primary intention is to extend test cases to increase coverage.
     *
     * @throws Exception
     */
    @Test
    public void testWriteProtected() throws Exception {
        File assets = new File(config.getSourceFolder(), "assets");
        File css = new File(folder.toFile(),"css");
        css.mkdir();
        final File cssFile = new File(css, "bootstrap.min.css");
        FileUtils.touch(cssFile);
        cssFile.setReadOnly();

        config.setAssetFolder(assets);
        config.setDestinationFolder(folder.toFile());
        Asset asset = new Asset(config);
        asset.copy();

        cssFile.setWritable(true);
        Assertions.assertFalse(asset.getErrors().isEmpty(), "At least one error during copy expected");
    }

    /**
     * Primary intention is to extend test cases to increase coverage.
     *
     * @throws Exception
     */
    @Test
    public void testUnlistable() throws Exception {
        config.setAssetFolder(new File(config.getSourceFolder(), "non-exsitent"));
        Asset asset = new Asset(config);
        asset.copy();
    }

    @Test
    public void testJBakeIgnoredFolder() {
        URL assetsUrl = this.getClass().getResource("/fixture/assets");
        File assets = new File(assetsUrl.getFile());
        Asset asset = new Asset(config);
        asset.copy(assets);

        File cssFile = new File(folder.toString() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css");
        Assertions.assertTrue(cssFile.exists(), () -> "File " + cssFile.getAbsolutePath() + " does not exist");
        File imgFile = new File(folder.toString() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png");
        Assertions.assertTrue(imgFile.exists(), () -> "File " + imgFile.getAbsolutePath() + " does not exist");
        File jsFile = new File(folder.toString() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js");
        Assertions.assertTrue(jsFile.exists(), () -> "File " + jsFile.getAbsolutePath() + " does not exist");
        File ignorableFolder = new File(folder.toString() + File.separatorChar + "ignorablefolder");
        File fooIgnorableFolder = new File(folder.toString() + File.separatorChar + "fooignorablefolder");
        Assertions.assertFalse(ignorableFolder.exists(), () -> "Folder " + ignorableFolder.getAbsolutePath() + " must not exist");
        Assertions.assertTrue(fooIgnorableFolder.exists(), () -> "Folder " + fooIgnorableFolder.getAbsolutePath() + " must exist");

        Assertions.assertTrue(asset.getErrors().isEmpty(), "Errors during asset copying");
    }

    @Test
    public void testFooIgnoredFolder() {
        config.setProperty(JBakeProperty.IGNORE_FILE, ".fooignore");

        URL assetsUrl = this.getClass().getResource("/fixture/assets");
        File assets = new File(assetsUrl.getFile());
        Asset asset = new Asset(config);
        asset.copy(assets);

        File cssFile = new File(folder.toString() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css");
        Assertions.assertTrue(cssFile.exists(), () -> "File " + cssFile.getAbsolutePath() + " does not exist");
        File imgFile = new File(folder.toString() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png");
        Assertions.assertTrue(imgFile.exists(), () -> "File " + imgFile.getAbsolutePath() + " does not exist");
        File jsFile = new File(folder.toString() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js");
        Assertions.assertTrue(jsFile.exists(), () -> "File " + jsFile.getAbsolutePath() + " does not exist");
        File ignorableFolder = new File(folder.toString() + File.separatorChar + "ignorablefolder");
        File fooIgnorableFolder = new File(folder.toString() + File.separatorChar + "fooignorablefolder");
        Assertions.assertTrue(ignorableFolder.exists(), () -> "Folder " + ignorableFolder.getAbsolutePath() + " must exist");
        Assertions.assertFalse(fooIgnorableFolder.exists(), () -> "Folder " + fooIgnorableFolder.getAbsolutePath() + " must not exist");

        Assertions.assertTrue(asset.getErrors().isEmpty(), "Errors during asset copying");
    }

    @Test
    public void testCopyAssetsFromContent() {
        URL contentUrl = this.getClass().getResource("/fixture/content");
        File contents = new File(contentUrl.getFile());
        Asset asset = new Asset(config);
        asset.copyAssetsFromContent(contents);

        int totalFiles = countFiles(folder.toFile());
        int expected = 3;

        Assertions.assertTrue(totalFiles == expected, () -> String.format("Number of files copied must be %d but are %d", expected, totalFiles));

        File pngFile = new File(folder.toString() + File.separatorChar + "blog" + File.separatorChar + "2012/images/custom-image.png");
        Assertions.assertTrue(pngFile.exists(), () -> "File " + pngFile.getAbsolutePath() + " does not exist");

        File jpgFile = new File(folder.toString() + File.separatorChar + "blog" + File.separatorChar + "2013/images/custom-image.jpg");
        Assertions.assertTrue(jpgFile.exists(), () -> "File " + jpgFile.getAbsolutePath() + " does not exist");

        File jsonFile = new File(folder.toString() + File.separatorChar + "blog" + File.separatorChar + "2012/sample.json");
        Assertions.assertTrue(jsonFile.exists(), () -> "File " + jsonFile.getAbsolutePath() + " does not exist");

        Assertions.assertTrue(asset.getErrors().isEmpty(), "Errors during asset copying");
    }

    @Test
    public void testIsFileAsset() {
        File cssAsset = new File(config.getAssetFolder().getAbsolutePath() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css");
        Assertions.assertTrue(cssAsset.exists());
        File contentFile = new File(config.getContentFolder().getAbsolutePath() + File.separatorChar + "about.html");
        Assertions.assertTrue(contentFile.exists());
        Asset asset = new Asset(config);

        Assertions.assertTrue(asset.isAssetFile(cssAsset));
        Assertions.assertFalse(asset.isAssetFile(contentFile));
    }


    private Integer countFiles(File path) {
        int total = 0;
        FileFilter filesOnly = FileFilterUtils.fileFileFilter();
        FileFilter dirsOnly = FileFilterUtils.directoryFileFilter();
        File[] files = path.listFiles(filesOnly);
        System.out.println(files);
        total += files.length;
        for (File file : path.listFiles(dirsOnly)) {
            total += countFiles(file);
        }
        return total;
    }
}
