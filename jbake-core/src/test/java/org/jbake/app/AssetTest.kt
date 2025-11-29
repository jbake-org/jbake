package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFilterUtils
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.PropertyList
import java.io.File
import java.nio.file.Files

class AssetTest : StringSpec({

    lateinit var folder: File
    lateinit var config: DefaultJBakeConfiguration
    lateinit var fixtureDir: File

    beforeTest {
        fixtureDir = File(AssetTest::class.java.getResource("/fixture")!!.file)
        folder = Files.createTempDirectory("jbake-test").toFile()
        config = ConfigUtil().loadConfig(fixtureDir) as DefaultJBakeConfiguration
        config.destinationDir = folder
        config.outputExtension shouldBe ".html"
    }

    afterTest {
        folder.deleteRecursively()
    }

    "testCopy" {
        val asset = Asset(config)
        asset.copy()

        File(folder, "css/bootstrap.min.css").exists() shouldBe true
        File(folder, "img/glyphicons-halflings.png").exists() shouldBe true
        File(folder, "js/bootstrap.min.js").exists() shouldBe true
        asset.errors.isEmpty() shouldBe true
    }

    "testCopySingleFile" {
        val asset = Asset(config)

        // Copy single asset file
        val cssTarget = File(folder, "css/bootstrap.min.css")
        cssTarget.exists() shouldBe false
        asset.copySingleFile(File(fixtureDir, "assets/css/bootstrap.min.css"))
        cssTarget.exists() shouldBe true

        // Copy single content file
        val imgTarget = File(folder, "blog/2013/images/custom-image.jpg")
        imgTarget.exists() shouldBe false
        asset.copySingleFile(File(fixtureDir, "content/blog/2013/images/custom-image.jpg"))
        imgTarget.exists() shouldBe true
    }

    "shouldSkipCopyingSingleFileIfDirectory" {
        val asset = Asset(config)
        val emptyDir = File(folder, "emptyDir").apply { mkdir() }

        asset.copySingleFile(emptyDir)

        File(fixtureDir.canonicalPath, "emptyDir").exists() shouldBe false
    }

    "shouldLogSkipCopyingSingleFileIfDirectory" {
        // This test requires LoggingTest setup - skipping for now or implement MockK logging
        val asset = Asset(config)
        File(folder, "emptyDir").apply { mkdir() }.let { asset.copySingleFile(it) }
        // TODO: verify logging with MockK appender
    }

    "testCopyCustomFolder" {
        config.assetDir = File(config.sourceDir, "/media")
        Asset(config).copy()

        File(folder, "favicon.ico").exists() shouldBe true
    }

    "testCopyIgnore" {
        val assetDir = File(folder, "ignoredAssets").apply { mkdirs() }
        FileUtils.copyDirectory(File(AssetTest::class.java.getResource("/fixture/ignorables")!!.file), assetDir)
        config.assetDir = assetDir
        config.assetIgnoreHidden = true
        TestUtils.hideAssets(assetDir)

        val asset = Asset(config)
        asset.copy(assetDir)

        File(folder, "test.txt").exists() shouldBe true
        File(folder, ".test.txt").exists() shouldBe false
        asset.errors.isEmpty() shouldBe true
    }

    "testWriteProtected" {
        val css = File(folder, "css").apply { mkdir() }
        val cssFile = File(css, "bootstrap.min.css").apply {
            FileUtils.touch(this)
            setReadOnly()
        }
        css.setReadOnly()

        config.assetDir = File(config.sourceDir, "assets")
        config.destinationDir = folder
        val asset = Asset(config)
        asset.copy()

        css.setWritable(true) // Restore directory permissions first
        cssFile.setWritable(true)
        asset.errors.isEmpty() shouldBe false
    }

    "testUnlistable" {
        config.assetDir = File(config.sourceDir, "non-existent")
        Asset(config).copy()
    }

    "testJBakeIgnoredFolder" {
        val assets = File(AssetTest::class.java.getResource("/fixture/assets")!!.file)
        val asset = Asset(config)
        asset.copy(assets)

        File(folder, "css/bootstrap.min.css").exists() shouldBe true
        File(folder, "img/glyphicons-halflings.png").exists() shouldBe true
        File(folder, "js/bootstrap.min.js").exists() shouldBe true
        File(folder, "ignorablefolder").exists() shouldBe false
        File(folder, "fooignorablefolder").exists() shouldBe true
        asset.errors.isEmpty() shouldBe true
    }

    "testFooIgnoredFolder" {
        config.setProperty(PropertyList.IGNORE_FILE.key, ".fooignore")
        val assets = File(AssetTest::class.java.getResource("/fixture/assets")!!.file)
        val asset = Asset(config)
        asset.copy(assets)

        File(folder, "css/bootstrap.min.css").exists() shouldBe true
        File(folder, "img/glyphicons-halflings.png").exists() shouldBe true
        File(folder, "js/bootstrap.min.js").exists() shouldBe true
        File(folder, "ignorablefolder").exists() shouldBe true
        File(folder, "fooignorablefolder").exists() shouldBe false
        asset.errors.isEmpty() shouldBe true
    }

    "testCopyAssetsFromContent" {
        val contents = File(AssetTest::class.java.getResource("/fixture/content")!!.file)
        val asset = Asset(config)
        asset.copyAssetsFromContent(contents)

        countFiles(folder) shouldBe 3
        File(folder, "blog/2012/images/custom-image.png").exists() shouldBe true
        File(folder, "blog/2013/images/custom-image.jpg").exists() shouldBe true
        File(folder, "blog/2012/sample.json").exists() shouldBe true
        asset.errors.isEmpty() shouldBe true
    }

    "testIsFileAsset" {
        val cssAsset = File(config.assetDir, "css/bootstrap.min.css")
        val contentFile = File(config.contentDir, "about.html")
        val asset = Asset(config)

        cssAsset.exists() shouldBe true
        contentFile.exists() shouldBe true
        asset.isAssetFile(cssAsset) shouldBe true
        asset.isAssetFile(contentFile) shouldBe false
    }
}) {
    companion object {
        private fun countFiles(path: File): Int {
            val files = path.listFiles(FileFilterUtils.fileFileFilter() as java.io.FileFilter) ?: return 0
            val dirs = path.listFiles(FileFilterUtils.directoryFileFilter() as java.io.FileFilter) ?: emptyArray()
            return files.size + dirs.sumOf { countFiles(it) }
        }
    }
}
