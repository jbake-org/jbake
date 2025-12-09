package org.jbake.app

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.apache.commons.io.FileUtils
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.PropertyList
import org.jbake.model.DocumentTypeRegistry.resetDocumentTypes
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class OvenTest : StringSpec({

    lateinit var root: Path
    lateinit var conf: DefaultJBakeConfiguration
    lateinit var sourceDir: File
    var contentStore: ContentStore? = null

    beforeTest {
        root = Files.createTempDirectory("jbake-test")
        // reset values to known state otherwise previous test case runs can affect the success of this test case
        resetDocumentTypes()
        val output = root.resolve("output").toFile()
        sourceDir = TestUtils.testResourcesAsSourceDir
        conf = ConfigUtil().loadConfig(sourceDir) as DefaultJBakeConfiguration
        conf.destinationDir = (output)
        conf.templateDir = (File(sourceDir, "groovyMarkupTemplates"))
        conf.setProperty("template.paper.file", "paper.tpl")
    }

    afterTest {
        if (contentStore != null && contentStore!!.isActive) {
            contentStore!!.close()
            contentStore!!.shutdown()
        }
    }

    "bakeWithAbsolutePaths" {
        conf.templateDir = (File(sourceDir, "groovyMarkupTemplates"))
        conf.contentDir = (File(sourceDir, "content"))
        conf.assetDir = (File(sourceDir, "assets"))

        val oven = Oven(conf)
        oven.bakeEverything()

        if (oven.errors.isNotEmpty())
            println("Oven errors:\n" + oven.errors.map { " * $it" }.joinToString())
        oven.errors.shouldBeEmpty()
    }

    "shouldBakeWithRelativeCustomPaths" {
        sourceDir = TestUtils.getTestResourcesAsSourceDir("/fixture-custom-relative")
        conf = ConfigUtil().loadConfig(sourceDir) as DefaultJBakeConfiguration
        val assetDir = conf.destinationDir.resolve("css")
        val aboutFile = conf.destinationDir.resolve("about.html")
        val blogSubDir = conf.destinationDir.resolve("blog")


        val oven = Oven(conf)
        oven.bakeEverything()

        if (oven.errors.isNotEmpty())
            println("Oven errors:\n" + oven.errors.map { " * $it" }.joinToString())
        oven.errors.shouldBeEmpty()

        conf.destinationDir.exists() shouldBe true
        conf.destinationDir.list()?.isNotEmpty() shouldBe true
        assetDir.exists() shouldBe true
        assetDir.list()?.isNotEmpty() shouldBe true
        aboutFile.shouldBeAFile()
        aboutFile.length() shouldBeGreaterThan 0
        blogSubDir.exists() shouldBe true
        blogSubDir.list()?.isNotEmpty() shouldBe true
    }

    "shouldBakeWithAbsoluteCustomPaths" {
        // given

        val source = root.resolve("source")
        val theme = root.resolve("theme")
        val destination = root.resolve("destination")

        val originalSource = TestUtils.testResourcesAsSourceDir
        FileUtils.copyDirectory(originalSource, source.toFile())
        val originalTheme = TestUtils.getTestResourcesAsSourceDir("/fixture-theme")
        FileUtils.copyDirectory(originalTheme, theme.toFile())

        val expectedTemplateDir = theme.resolve("templates")
        val expectedAssetDir = theme.resolve("assets")
        val expectedDestination = destination.resolve("output")

        val properties = source.resolve("jbake.properties")


        val fw = Files.newBufferedWriter(properties)

        fw.write(PropertyList.ASSET_FOLDER.key + "=" + TestUtils.escapeBackSlashes(expectedAssetDir))
        fw.newLine()
        fw.write(PropertyList.TEMPLATE_FOLDER.key + "=" + TestUtils.escapeBackSlashes(expectedTemplateDir))
        fw.newLine()
        fw.write(PropertyList.DESTINATION_FOLDER.key + "=" + TestUtils.escapeBackSlashes(expectedDestination))
        fw.close()

        conf = ConfigUtil().loadConfig(source.toFile()) as DefaultJBakeConfiguration
        val assetDir = conf.destinationDir.resolve("css")
        val aboutFile = conf.destinationDir.resolve("about.html")
        val blogSubDir = conf.destinationDir.resolve("blog")


        val oven = Oven(conf)
        oven.bakeEverything()

        if (oven.errors.isNotEmpty())
            println("Oven errors:\n" + oven.errors.map { " * $it" }.joinToString())
        oven.errors.shouldBeEmpty()

        conf.destinationDir.exists() shouldBe true
        conf.destinationDir.list()?.isNotEmpty() shouldBe true
        assetDir.exists() shouldBe true
        assetDir.list()?.isNotEmpty() shouldBe true
        aboutFile.shouldBeAFile()
        aboutFile.length() shouldBeGreaterThan 0
        blogSubDir.exists() shouldBe true
        blogSubDir.list()?.isNotEmpty() shouldBe true
    }


    "shouldThrowExceptionIfSourceDirDoesNotExist" {
        conf.setSourceDir(root.resolve("none").toFile())

        shouldThrow<JBakeExitException> { Oven(conf) }
    }

    "shouldInstantiateNeededUtensils" {
        val template = TestUtils.createOrEmptyDir(root.toFile(), "template")
        val content = TestUtils.createOrEmptyDir(root.toFile(), "content")
        val assets = TestUtils.createOrEmptyDir(root.toFile(), "assets")

        conf.templateDir = (template)
        conf.contentDir = (content)
        conf.assetDir = (assets)

        val oven = Oven(conf)

        oven.utensils.contentStore.shouldNotBeNull()
        oven.utensils.crawler.shouldNotBeNull()
        oven.utensils.renderer.shouldNotBeNull()
        oven.utensils.asset.shouldNotBeNull()
        oven.utensils.configuration shouldBe conf
    }

    "shouldInspectConfigurationDuringInstantiationFromUtils" {
        conf.setSourceDir(root.resolve("none").toFile())

        val contentStore = mockk<ContentStore>()
        val crawler = mockk<Crawler>()
        val renderer = mockk<Renderer>()
        val asset = mockk<Asset>()

        val utensils = Utensils(
            configuration = conf,
            contentStore = contentStore,
            crawler = crawler,
            renderer = renderer,
            asset = asset
        )

        shouldThrow<JBakeExitException> { Oven(utensils) }
    }

    "shouldCrawlRenderAndCopyAssets" {

        conf.templateDir = TestUtils.createOrEmptyDir(root.toFile(), "template")
        conf.contentDir = TestUtils.createOrEmptyDir(root.toFile(), "content")
        conf.assetDir = TestUtils.createOrEmptyDir(root.toFile(), "assets")
        conf.setProperty(PropertyList.RENDER_TAGS.key, "false")

        contentStore = spyk(ContentStore("memory", "documents" + System.currentTimeMillis()))

        val mockCrawler = mockk<Crawler>(relaxed = true)
        val mockRenderer = mockk<Renderer>(relaxed = true)
        val mockAsset = mockk<Asset>(relaxed = true)

        // Mock the config property so render methods can access it
        every { mockRenderer.config } answers { conf }

        // Mock all render methods that might be called by rendering tools
        // These methods are called without parameters (using default parameters)
        every { mockRenderer.renderIndex() } returns Unit
        every { mockRenderer.renderIndexPaging() } returns Unit
        every { mockRenderer.renderArchive() } returns Unit
        every { mockRenderer.renderFeed() } returns Unit
        every { mockRenderer.renderError404() } returns Unit
        every { mockRenderer.renderSitemap() } returns Unit
        every { mockRenderer.renderTags() } returns 0

        val utensils = Utensils(conf, contentStore, mockCrawler, mockRenderer, mockAsset)

        val oven = Oven(utensils)

        oven.bakeEverything()

        verify(exactly = 1) { contentStore.startup() }
        verify(atLeast = 1) { mockRenderer.renderIndex() }
        verify(exactly = 1) { mockCrawler.crawlContentDirectory() }
        verify(exactly = 1) { mockAsset.copy() }
    }

    "localeConfiguration" {
        val language = conf.jvmLocale

        val oven = Oven(conf)
        oven.bakeEverything()

        Locale.getDefault() shouldBe Locale(language)
    }

    "noLocaleConfiguration" {
        conf.setProperty(PropertyList.JVM_LOCALE.key, null)

        val language = Locale.getDefault().language
        val oven = Oven(conf)
        oven.bakeEverything()

        Locale.getDefault().language shouldBe language
    }
})
