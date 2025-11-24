package org.jbake.app

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFilterUtils
import org.assertj.core.api.Assertions.assertThat
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.PropertyList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.nio.file.Path

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
        assertEquals(".html", config.outputExtension)
    }


    @Test
    fun testCopy() {
        val asset = Asset(config)
        asset.copy()
        val cssFile = File(folder.toString() + fSC + "css" + fSC + "bootstrap.min.css")
        assertTrue(cssFile.exists()) { "File " + cssFile.absolutePath + " does not exist" }
        val imgFile = File(folder.toString() + fSC + "img" + fSC + "glyphicons-halflings.png")
        assertTrue(imgFile.exists()) { "File " + imgFile.absolutePath + " does not exist" }
        val jsFile = File(folder.toString() + fSC + "js" + fSC + "bootstrap.min.js")
        assertTrue(jsFile.exists()) { "File " + jsFile.absolutePath + " does not exist" }
        assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testCopySingleFile() {
        val asset = Asset(config)
        val cssSubPath = fSC.toString() + "css" + fSC + "bootstrap.min.css"
        val contentImgPath = (fSC.toString() + "blog" + fSC + "2013" + fSC + "images" + fSC + "custom-image.jpg")

        // Copy single Asset File
        var expected = File(folder.toString() + cssSubPath)
        assertFalse(expected.exists(), "cssFile should not exist before running the test; avoids false positives")
        val cssFile = File(fixtureDir!!.path + fSC + "assets" + cssSubPath)
        asset.copySingleFile(cssFile)
        assertTrue(expected.exists(), "Css asset file did not copy")

        // Copy single Content file
        expected = File(folder.toString() + contentImgPath)
        assertFalse(expected.exists(), "content image file should not exist before running the test")
        val imgFile = File(fixtureDir!!.path + fSC + "content" + contentImgPath)
        asset.copySingleFile(imgFile)
        assertTrue(expected.exists(), "Content img file did not copy")
    }

    @Test
    @Throws(IOException::class)
    fun shouldSkipCopyingSingleFileIfDirectory() {
        val asset = Asset(config)

        val emptyDir = File(folder!!.toFile(), "emptyDir")
        emptyDir.mkdir()
        val expectedDir = File(fixtureDir!!.getCanonicalPath(), "emptyDir")

        asset.copySingleFile(emptyDir)
        assertFalse(expectedDir.exists(), "Directory should be skipped")
    }

    @Test
    @Throws(IOException::class)
    fun shouldLogSkipCopyingSingleFileIfDirectory() {
        val asset = Asset(config)
        val emptyDir = File(folder!!.toFile(), "emptyDir")
        emptyDir.mkdir()

        asset.copySingleFile(emptyDir)

        Mockito.verify(mockAppender, Mockito.times(1)).doAppend(captorLoggingEvent.capture())

        val loggingEvent = captorLoggingEvent.getValue()
        assertThat(loggingEvent.message).isEqualTo("Skip copying single asset file [{}]. Is a directory.")
    }

    @Test
    fun testCopyCustomFolder() {
        config.assetFolder = (File(config.sourceFolder, "/media"))
        val asset = Asset(config)
        asset.copy()

        val favFile = File(folder.toString() + fSC + "favicon.ico")
        assertTrue(favFile.exists()) { "File " + favFile.absolutePath + " does not exist" }
        assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
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
        assertTrue(testFile.exists()) { "File " + testFile.absolutePath + " does not exist" }
        val testIgnoreFile = File(folder!!.toFile(), ".test.txt")
        assertFalse(testIgnoreFile.exists()) { "File " + testIgnoreFile.absolutePath + " does exist" }
        assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testWriteProtected() {
        val assets = File(config.sourceFolder, "assets")
        val css = File(folder!!.toFile(), "css")
        css.mkdir()
        val cssFile = File(css, "bootstrap.min.css")
        FileUtils.touch(cssFile)
        cssFile.setReadOnly()
        css.setReadOnly() // Make directory read-only as well

        config.assetFolder = (assets)
        config.destinationFolder = (folder!!.toFile())
        val asset = Asset(config)
        asset.copy()

        css.setWritable(true) // Restore directory permissions first
        cssFile.setWritable(true)
        assertFalse(asset.errors.isEmpty(), "At least one error during copy expected")
    }

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

        val cssFile = File(folder.toString() + fSC + "css" + fSC + "bootstrap.min.css")
        assertTrue(cssFile.exists()) { "File " + cssFile.absolutePath + " does not exist" }
        val imgFile = File(folder.toString() + fSC + "img" + fSC + "glyphicons-halflings.png")
        assertTrue(imgFile.exists()) { "File " + imgFile.absolutePath + " does not exist" }
        val jsFile = File(folder.toString() + fSC + "js" + fSC + "bootstrap.min.js")
        assertTrue(jsFile.exists()) { "File " + jsFile.absolutePath + " does not exist" }
        val ignorableFolder = File(folder.toString() + fSC + "ignorablefolder")
        val fooIgnorableFolder = File(folder.toString() + fSC + "fooignorablefolder")
        assertFalse(ignorableFolder.exists()) { "Folder " + ignorableFolder.absolutePath + " must not exist" }
        assertTrue(fooIgnorableFolder.exists()) { "Folder " + fooIgnorableFolder.absolutePath + " must exist" }
        assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testFooIgnoredFolder() {
        config.setProperty(PropertyList.IGNORE_FILE.key, ".fooignore")

        val assetsUrl = this.javaClass.getResource("/fixture/assets")
        val assets = File(assetsUrl!!.file)
        val asset = Asset(config)
        asset.copy(assets)

        val cssFile = File(folder.toString() + fSC + "css" + fSC + "bootstrap.min.css")
        assertTrue(cssFile.exists()) { "File " + cssFile.absolutePath + " does not exist" }
        val imgFile = File(folder.toString() + fSC + "img" + fSC + "glyphicons-halflings.png")
        assertTrue(imgFile.exists()) { "File " + imgFile.absolutePath + " does not exist" }
        val jsFile = File(folder.toString() + fSC + "js" + fSC + "bootstrap.min.js")
        assertTrue(jsFile.exists()) { "File " + jsFile.absolutePath + " does not exist" }
        val ignorableFolder = File(folder.toString() + fSC + "ignorablefolder")
        val fooIgnorableFolder = File(folder.toString() + fSC + "fooignorablefolder")
        assertTrue(ignorableFolder.exists()) { "Folder " + ignorableFolder.absolutePath + " must exist" }
        assertFalse(fooIgnorableFolder.exists()) { "Folder " + fooIgnorableFolder.absolutePath + " must not exist" }
        assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testCopyAssetsFromContent() {
        val contentUrl = this.javaClass.getResource("/fixture/content")
        val contents = File(contentUrl!!.file)
        val asset = Asset(config)
        asset.copyAssetsFromContent(contents)

        val totalFiles = countFiles(folder!!.toFile())
        val expected = 3

        assertTrue(totalFiles == expected) { String.format("Number of files copied must be %d but are %d", expected, totalFiles) }

        val pngFile = File(folder.toString() + fSC + "blog" + fSC + "2012/images/custom-image.png")
        assertTrue(pngFile.exists()) { "File " + pngFile.absolutePath + " does not exist" }

        val jpgFile = File(folder.toString() + fSC + "blog" + fSC + "2013/images/custom-image.jpg")
        assertTrue(jpgFile.exists()) { "File " + jpgFile.absolutePath + " does not exist" }

        val jsonFile = File(folder.toString() + fSC + "blog" + fSC + "2012/sample.json")
        assertTrue(jsonFile.exists()) { "File " + jsonFile.absolutePath + " does not exist" }

        assertTrue(asset.errors.isEmpty(), "Errors during asset copying")
    }

    @Test
    fun testIsFileAsset() {
        val cssAsset = File(config.assetFolder.absolutePath + fSC + "css" + fSC + "bootstrap.min.css")
        assertTrue(cssAsset.exists())
        val contentFile = File(config.contentFolder.absolutePath + fSC + "about.html")
        assertTrue(contentFile.exists())
        val asset = Asset(config)

        assertTrue(asset.isAssetFile(cssAsset))
        assertFalse(asset.isAssetFile(contentFile))
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

internal val fSC = File.separatorChar
