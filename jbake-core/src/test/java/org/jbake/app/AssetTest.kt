package org.jbake.app

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFilterUtils
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.PropertyList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.nio.file.Path
import java.util.function.Supplier

class AssetTest : LoggingTest() {
    var folder: Path? = null
    private lateinit var config: DefaultJBakeConfiguration
    private var fixtureDir: File? = null


    @BeforeEach
    fun setup(@TempDir folder: Path) {
        fixtureDir = File(this.javaClass.getResource("/fixture").file)
        this.folder = folder
        config = ConfigUtil().loadConfig(fixtureDir!!) as DefaultJBakeConfiguration
        config.destinationFolder = folder.toFile()
        Assertions.assertEquals(".html", config.outputExtension)
    }


    @Test
    fun testCopy() {
        val asset = Asset(config)
        asset.copy()
        val cssFile = File(folder.toString() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css")
        Assertions.assertTrue(cssFile.exists()) { "File " + cssFile.absolutePath + " does not exist" }
        val imgFile =
            File(folder.toString() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png")
        Assertions.assertTrue(imgFile.exists()) { "File " + imgFile.absolutePath + " does not exist" }
        val jsFile = File(folder.toString() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js")
        Assertions.assertTrue(jsFile.exists()) { "File " + jsFile.absolutePath + " does not exist" }

        Assertions.assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testCopySingleFile() {
        val asset = Asset(config)
        val cssSubPath = File.separatorChar.toString() + "css" + File.separatorChar + "bootstrap.min.css"
        val contentImgPath = (File.separatorChar.toString() + "blog" + File.separatorChar + "2013" + File.separatorChar
                + "images" + File.separatorChar + "custom-image.jpg")

        // Copy single Asset File
        var expected = File(folder.toString() + cssSubPath)
        Assertions.assertFalse(
            expected.exists(),
            "cssFile should not exist before running the test; avoids false positives"
        )
        val cssFile = File(fixtureDir!!.path + File.separatorChar + "assets" + cssSubPath)
        asset.copySingleFile(cssFile)
        Assertions.assertTrue(expected.exists(), "Css asset file did not copy")

        // Copy single Content file
        expected = File(folder.toString() + contentImgPath)
        Assertions.assertFalse(expected.exists(), "content image file should not exist before running the test")
        val imgFile = File(fixtureDir!!.path + File.separatorChar + "content" + contentImgPath)
        asset.copySingleFile(imgFile)
        Assertions.assertTrue(expected.exists(), "Content img file did not copy")
    }

    @Test
    @Throws(IOException::class)
    fun shouldSkipCopyingSingleFileIfDirectory() {
        val asset = Asset(config)

        val emptyDir = File(folder!!.toFile(), "emptyDir")
        emptyDir.mkdir()
        val expectedDir = File(fixtureDir!!.getCanonicalPath(), "emptyDir")

        asset.copySingleFile(emptyDir)

        Assertions.assertFalse(expectedDir.exists(), "Directory should be skipped")
    }

    @Test
    @Throws(IOException::class)
    fun shouldLogSkipCopyingSingleFileIfDirectory() {
        val asset = Asset(config)
        val emptyDir = File(folder!!.toFile(), "emptyDir")
        emptyDir.mkdir()

        asset.copySingleFile(emptyDir)

        Mockito.verify(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent!!.capture())

        val loggingEvent = captorLoggingEvent!!.getValue()
        org.assertj.core.api.Assertions.assertThat(loggingEvent.message)
            .isEqualTo("Skip copying single asset file [{}]. Is a directory.")
    }

    @Test
    fun testCopyCustomFolder() {
        config.assetFolder = (File(config.sourceFolder, "/media"))
        val asset = Asset(config)
        asset.copy()

        val favFile = File(folder.toString() + File.separatorChar + "favicon.ico")
        Assertions.assertTrue(favFile.exists()) { "File " + favFile.absolutePath + " does not exist" }

        Assertions.assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testCopyIgnore() {
        val assetFolder = File(folder!!.toFile(), "ignoredAssets")
        assetFolder.mkdirs()
        FileUtils.copyDirectory(File(this.javaClass.getResource("/fixture/ignorables").file), assetFolder)
        config.assetFolder = (assetFolder)
        config.assetIgnoreHidden = true
        TestUtils.hideAssets(assetFolder)
        val asset = Asset(config)
        asset.copy(assetFolder)

        val testFile = File(folder!!.toFile(), "test.txt")
        Assertions.assertTrue(testFile.exists()) { "File " + testFile.absolutePath + " does not exist" }
        val testIgnoreFile = File(folder!!.toFile(), ".test.txt")
        Assertions.assertFalse(
            testIgnoreFile.exists()
        ) { "File " + testIgnoreFile.absolutePath + " does exist" }

        Assertions.assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }


    /**
     * Primary intention is to extend test cases to increase coverage.
     *
     * @throws Exception
     */
    @Test
    fun testWriteProtected() {
        val assets = File(config.sourceFolder, "assets")
        val css = File(folder!!.toFile(), "css")
        css.mkdir()
        val cssFile = File(css, "bootstrap.min.css")
        FileUtils.touch(cssFile)
        cssFile.setReadOnly()

        config.assetFolder = (assets)
        config.destinationFolder = (folder!!.toFile())
        val asset = Asset(config)
        asset.copy()

        cssFile.setWritable(true)
        Assertions.assertFalse(asset.errors.isEmpty(), "At least one error during copy expected")
    }

    /**
     * Primary intention is to extend test cases to increase coverage.
     *
     * @throws Exception
     */
    @Test
    fun testUnlistable() {
        config.assetFolder = (File(config.sourceFolder, "non-exsitent"))
        val asset = Asset(config)
        asset.copy()
    }

    @Test
    fun testJBakeIgnoredFolder() {
        val assetsUrl = this.javaClass.getResource("/fixture/assets")
        val assets = File(assetsUrl!!.file)
        val asset = Asset(config)
        asset.copy(assets)

        val cssFile = File(folder.toString() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css")
        Assertions.assertTrue(cssFile.exists()) { "File " + cssFile.absolutePath + " does not exist" }
        val imgFile =
            File(folder.toString() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png")
        Assertions.assertTrue(imgFile.exists()) { "File " + imgFile.absolutePath + " does not exist" }
        val jsFile = File(folder.toString() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js")
        Assertions.assertTrue(jsFile.exists()) { "File " + jsFile.absolutePath + " does not exist" }
        val ignorableFolder = File(folder.toString() + File.separatorChar + "ignorablefolder")
        val fooIgnorableFolder = File(folder.toString() + File.separatorChar + "fooignorablefolder")
        Assertions.assertFalse(
            ignorableFolder.exists()
        ) { "Folder " + ignorableFolder.absolutePath + " must not exist" }
        Assertions.assertTrue(
            fooIgnorableFolder.exists()
        ) { "Folder " + fooIgnorableFolder.absolutePath + " must exist" }

        Assertions.assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testFooIgnoredFolder() {
        config.setProperty(PropertyList.IGNORE_FILE.key, ".fooignore")

        val assetsUrl = this.javaClass.getResource("/fixture/assets")
        val assets = File(assetsUrl!!.file)
        val asset = Asset(config)
        asset.copy(assets)

        val cssFile = File(folder.toString() + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css")
        Assertions.assertTrue(cssFile.exists()) { "File " + cssFile.absolutePath + " does not exist" }
        val imgFile =
            File(folder.toString() + File.separatorChar + "img" + File.separatorChar + "glyphicons-halflings.png")
        Assertions.assertTrue(imgFile.exists()) { "File " + imgFile.absolutePath + " does not exist" }
        val jsFile = File(folder.toString() + File.separatorChar + "js" + File.separatorChar + "bootstrap.min.js")
        Assertions.assertTrue(jsFile.exists()) { "File " + jsFile.absolutePath + " does not exist" }
        val ignorableFolder = File(folder.toString() + File.separatorChar + "ignorablefolder")
        val fooIgnorableFolder = File(folder.toString() + File.separatorChar + "fooignorablefolder")
        Assertions.assertTrue(
            ignorableFolder.exists()
        ) { "Folder " + ignorableFolder.absolutePath + " must exist" }
        Assertions.assertFalse(
            fooIgnorableFolder.exists()
        ) { "Folder " + fooIgnorableFolder.absolutePath + " must not exist" }

        Assertions.assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testCopyAssetsFromContent() {
        val contentUrl = this.javaClass.getResource("/fixture/content")
        val contents = File(contentUrl!!.file)
        val asset = Asset(config)
        asset.copyAssetsFromContent(contents)

        val totalFiles = countFiles(folder!!.toFile())
        val expected = 3

        Assertions.assertTrue(
            totalFiles == expected
        ) { String.format("Number of files copied must be %d but are %d", expected, totalFiles) }

        val pngFile =
            File(folder.toString() + File.separatorChar + "blog" + File.separatorChar + "2012/images/custom-image.png")
        Assertions.assertTrue(pngFile.exists()) { "File " + pngFile.absolutePath + " does not exist" }

        val jpgFile =
            File(folder.toString() + File.separatorChar + "blog" + File.separatorChar + "2013/images/custom-image.jpg")
        Assertions.assertTrue(jpgFile.exists()) { "File " + jpgFile.absolutePath + " does not exist" }

        val jsonFile = File(folder.toString() + File.separatorChar + "blog" + File.separatorChar + "2012/sample.json")
        Assertions.assertTrue(jsonFile.exists()) { "File " + jsonFile.absolutePath + " does not exist" }

        Assertions.assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testIsFileAsset() {
        val cssAsset = File(config.assetFolder.absolutePath + File.separatorChar + "css" + File.separatorChar + "bootstrap.min.css")
        Assertions.assertTrue(cssAsset.exists())
        val contentFile = File(config.contentFolder.absolutePath + File.separatorChar + "about.html")
        Assertions.assertTrue(contentFile.exists())
        val asset = Asset(config)

        Assertions.assertTrue(asset.isAssetFile(cssAsset))
        Assertions.assertFalse(asset.isAssetFile(contentFile))
    }


    private fun countFiles(path: File): Int {
        var total = 0
        val filesOnly: FileFilter? = FileFilterUtils.fileFileFilter()
        val dirsOnly: FileFilter? = FileFilterUtils.directoryFileFilter()
        val files = path.listFiles(filesOnly)
        println(files)
        total += files!!.size
        for (file in path.listFiles(dirsOnly)) {
            total += countFiles(file)
        }
        return total
    }
}
