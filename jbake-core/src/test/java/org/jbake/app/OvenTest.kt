package org.jbake.app

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.apache.commons.io.FileUtils
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.PropertyList
import org.jbake.model.DocumentTypes.resetDocumentTypes
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class OvenTest : StringSpec({

    lateinit var root: Path
    lateinit var configuration: DefaultJBakeConfiguration
    lateinit var sourceDir: File
    var contentStore: ContentStore? = null

    beforeTest {
        root = Files.createTempDirectory("jbake-test")
        // reset values to known state otherwise previous test case runs can affect the success of this test case
        resetDocumentTypes()
        val output = root.resolve("output").toFile()
        sourceDir = TestUtils.testResourcesAsSourceDir
        configuration = ConfigUtil().loadConfig(sourceDir) as DefaultJBakeConfiguration
        configuration.destinationDir = (output)
        configuration.templateDir = (File(sourceDir, "groovyMarkupTemplates"))
        configuration.setProperty("template.paper.file", "paper.tpl")
    }

    afterTest {
        if (contentStore != null && contentStore!!.isActive) {
            contentStore!!.close()
            contentStore!!.shutdown()
        }
    }

    "bakeWithAbsolutePaths" {
        configuration.templateDir = (File(sourceDir, "groovyMarkupTemplates"))
        configuration.contentDir = (File(sourceDir, "content"))
        configuration.assetDir = (File(sourceDir, "assets"))

        val oven = Oven(configuration)
        oven.bakeEverything()

        oven.errors.isEmpty()
    }

    "shouldBakeWithRelativeCustomPaths" {
        sourceDir = TestUtils.getTestResourcesAsSourceDir("/fixture-custom-relative")
        configuration = ConfigUtil().loadConfig(sourceDir) as DefaultJBakeConfiguration
        val assetDir = File(configuration.destinationDir, "css")
        val aboutFile = File(configuration.destinationDir, "about.html")
        val blogSubDir = File(configuration.destinationDir, "blog")


        val oven = Oven(configuration)
        oven.bakeEverything()

        oven.errors.isEmpty() shouldBe true
        configuration.destinationDir.exists() shouldBe true
        configuration.destinationDir.list()?.isNotEmpty() shouldBe true
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

        configuration = ConfigUtil().loadConfig(source.toFile()) as DefaultJBakeConfiguration
        val assetDir = File(configuration.destinationDir, "css")
        val aboutFile = File(configuration.destinationDir, "about.html")
        val blogSubDir = File(configuration.destinationDir, "blog")


        val oven = Oven(configuration)
        oven.bakeEverything()

        oven.errors.isEmpty() shouldBe true
        configuration.destinationDir.exists() shouldBe true
        configuration.destinationDir.list()?.isNotEmpty() shouldBe true
        assetDir.exists() shouldBe true
        assetDir.list()?.isNotEmpty() shouldBe true
        aboutFile.shouldBeAFile()
        aboutFile.length() shouldBeGreaterThan 0
        blogSubDir.exists() shouldBe true
        blogSubDir.list()?.isNotEmpty() shouldBe true
    }


    "shouldThrowExceptionIfSourceDirDoesNotExist" {
        configuration.setSourceDir(root.resolve("none").toFile())

        shouldThrow<JBakeException> { Oven(configuration) }
    }

    "shouldInstantiateNeededUtensils" {
        val template = TestUtils.newDir(root.toFile(), "template")
        val content = TestUtils.newDir(root.toFile(), "content")
        val assets = TestUtils.newDir(root.toFile(), "assets")

        configuration.templateDir = (template)
        configuration.contentDir = (content)
        configuration.assetDir = (assets)

        val oven = Oven(configuration)

        oven.utensils.contentStore.shouldNotBeNull()
        oven.utensils.crawler.shouldNotBeNull()
        oven.utensils.renderer.shouldNotBeNull()
        oven.utensils.asset.shouldNotBeNull()
        oven.utensils.configuration shouldBe configuration
    }

    "shouldInspectConfigurationDuringInstantiationFromUtils" {
        configuration.setSourceDir(root.resolve("none").toFile())

        val contentStore = mockk<ContentStore>()
        val crawler = mockk<Crawler>()
        val renderer = mockk<Renderer>()
        val asset = mockk<Asset>()

        val utensils = Utensils(
            configuration = configuration,
            contentStore = contentStore,
            crawler = crawler,
            renderer = renderer,
            asset = asset
        )

        shouldThrow<JBakeException> { Oven(utensils) }
    }

    "shouldCrawlRenderAndCopyAssets" {
        val template = TestUtils.newDir(root.toFile(), "template")
        val content = TestUtils.newDir(root.toFile(), "content")
        val assets = TestUtils.newDir(root.toFile(), "assets")

        configuration.templateDir = (template)
        configuration.contentDir = (content)
        configuration.assetDir = (assets)

        contentStore = spyk(ContentStore("memory", "documents" + System.currentTimeMillis()))

        val crawler = mockk<Crawler>(relaxed = true)
        val renderer = mockk<Renderer>(relaxed = true)
        val asset = mockk<Asset>(relaxed = true)

        val utensils = Utensils(
            configuration = configuration,
            contentStore = contentStore!!,
            renderer = renderer,
            crawler = crawler,
            asset = asset
        )

        val oven = Oven(utensils)

        oven.bakeEverything()

        verify(exactly = 1) { contentStore!!.startup() }
        verify(atLeast = 1) { renderer.renderIndex(any()) }
        verify(exactly = 1) { crawler.crawlContentDirectory() }
        verify(exactly = 1) { asset.copy() }
    }

    "localeConfiguration" {
        val language = configuration.jvmLocale

        val oven = Oven(configuration)
        oven.bakeEverything()

        Locale.getDefault() shouldBe Locale(language)
    }

    "noLocaleConfiguration" {
        configuration.setProperty(PropertyList.JVM_LOCALE.key, null)

        val language = Locale.getDefault().language
        val oven = Oven(configuration)
        oven.bakeEverything()

        Locale.getDefault().language shouldBe language
    }
})
