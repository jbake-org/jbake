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
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.io.File
import java.nio.file.Path

class AssetTest : LoggingTest() {
    private lateinit var folder: File
    private lateinit var config: DefaultJBakeConfiguration
    private lateinit var fixtureDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        fixtureDir = File(javaClass.getResource("/fixture")!!.file)
        folder = tempDir.toFile()
        config = ConfigUtil().loadConfig(fixtureDir) as DefaultJBakeConfiguration
        config.destinationFolder = folder
        assertEquals(".html", config.outputExtension)
    }

    @Test fun testCopy() {
        val asset = Asset(config)
        asset.copy()

        assertTrue(File(folder, "css/bootstrap.min.css").exists())
        assertTrue(File(folder, "img/glyphicons-halflings.png").exists())
        assertTrue(File(folder, "js/bootstrap.min.js").exists())
        assertTrue(asset.errors.isEmpty())
    }

    @Test fun testCopySingleFile() {
        val asset = Asset(config)

        // Copy single asset file
        val cssTarget = File(folder, "css/bootstrap.min.css")
        assertFalse(cssTarget.exists())
        asset.copySingleFile(File(fixtureDir, "assets/css/bootstrap.min.css"))
        assertTrue(cssTarget.exists())

        // Copy single content file
        val imgTarget = File(folder, "blog/2013/images/custom-image.jpg")
        assertFalse(imgTarget.exists())
        asset.copySingleFile(File(fixtureDir, "content/blog/2013/images/custom-image.jpg"))
        assertTrue(imgTarget.exists())
    }

    @Test fun shouldSkipCopyingSingleFileIfDirectory() {
        val asset = Asset(config)
        val emptyDir = File(folder, "emptyDir").apply { mkdir() }

        asset.copySingleFile(emptyDir)

        assertFalse(File(fixtureDir.canonicalPath, "emptyDir").exists())
    }

    @Test fun shouldLogSkipCopyingSingleFileIfDirectory() {
        val asset = Asset(config)
        File(folder, "emptyDir").apply { mkdir() }.let { asset.copySingleFile(it) }

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture())
        assertThat(captorLoggingEvent.value.message).isEqualTo("Skip copying single asset file [{}]. Is a directory.")
    }

    @Test fun testCopyCustomFolder() {
        config.assetFolder = File(config.sourceFolder, "/media")
        Asset(config).copy()

        assertTrue(File(folder, "favicon.ico").exists())
    }

    @Test fun testCopyIgnore() {
        val assetFolder = File(folder, "ignoredAssets").apply { mkdirs() }
        FileUtils.copyDirectory(File(javaClass.getResource("/fixture/ignorables")!!.file), assetFolder)
        config.assetFolder = assetFolder
        config.assetIgnoreHidden = true
        TestUtils.hideAssets(assetFolder)

        val asset = Asset(config)
        asset.copy(assetFolder)

        assertTrue(File(folder, "test.txt").exists())
        assertFalse(File(folder, ".test.txt").exists())
        assertTrue(asset.errors.isEmpty())
    }

    @Test fun testWriteProtected() {
        val css = File(folder, "css").apply { mkdir() }
        val cssFile = File(css, "bootstrap.min.css").apply {
            FileUtils.touch(this)
            setReadOnly()
        }
        css.setReadOnly()

        config.assetFolder = File(config.sourceFolder, "assets")
        config.destinationFolder = folder
        val asset = Asset(config)
        asset.copy()

        css.setWritable(true) // Restore directory permissions first
        cssFile.setWritable(true)
        assertFalse(asset.errors.isEmpty(), "At least one error during copy expected")
    }

    @Test fun testUnlistable() {
        config.assetFolder = File(config.sourceFolder, "non-existent")
        Asset(config).copy()
    }

    @Test fun testJBakeIgnoredFolder() {
        val assets = File(javaClass.getResource("/fixture/assets")!!.file)
        val asset = Asset(config)
        asset.copy(assets)

        assertTrue(File(folder, "css/bootstrap.min.css").exists())
        assertTrue(File(folder, "img/glyphicons-halflings.png").exists())
        assertTrue(File(folder, "js/bootstrap.min.js").exists())
        assertFalse(File(folder, "ignorablefolder").exists())
        assertTrue(File(folder, "fooignorablefolder").exists())
        assertTrue(asset.errors.isEmpty())
    }

    @Test fun testFooIgnoredFolder() {
        config.setProperty(PropertyList.IGNORE_FILE.key, ".fooignore")
        val assets = File(javaClass.getResource("/fixture/assets")!!.file)
        val asset = Asset(config)
        asset.copy(assets)

        assertTrue(File(folder, "css/bootstrap.min.css").exists())
        assertTrue(File(folder, "img/glyphicons-halflings.png").exists())
        assertTrue(File(folder, "js/bootstrap.min.js").exists())
        assertTrue(File(folder, "ignorablefolder").exists())
        assertFalse(File(folder, "fooignorablefolder").exists())
        assertTrue(asset.errors.isEmpty())
    }

    @Test fun testCopyAssetsFromContent() {
        val contents = File(javaClass.getResource("/fixture/content")!!.file)
        val asset = Asset(config)
        asset.copyAssetsFromContent(contents)

        assertEquals(3, countFiles(folder))
        assertTrue(File(folder, "blog/2012/images/custom-image.png").exists())
        assertTrue(File(folder, "blog/2013/images/custom-image.jpg").exists())
        assertTrue(File(folder, "blog/2012/sample.json").exists())
        assertTrue(asset.errors.isEmpty())
    }

    @Test fun testIsFileAsset() {
        val cssAsset = File(config.assetFolder, "css/bootstrap.min.css")
        val contentFile = File(config.contentFolder, "about.html")
        val asset = Asset(config)

        assertTrue(cssAsset.exists())
        assertTrue(contentFile.exists())
        assertTrue(asset.isAssetFile(cssAsset))
        assertFalse(asset.isAssetFile(contentFile))
    }

    private fun countFiles(path: File): Int {
        val files = path.listFiles(FileFilterUtils.fileFileFilter() as java.io.FileFilter) ?: return 0
        val dirs = path.listFiles(FileFilterUtils.directoryFileFilter() as java.io.FileFilter) ?: emptyArray()
        return files.size + dirs.sumOf { countFiles(it) }
    }
}
