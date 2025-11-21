package org.jbake.app

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.PropertyList
import org.jbake.model.DocumentTypes.resetDocumentTypes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class OvenTest {
    @TempDir
    var root: Path? = null

    private lateinit var configuration: DefaultJBakeConfiguration
    private lateinit var sourceFolder: File
    private var contentStore: ContentStore? = null

    @BeforeEach
    fun setUp() {
        // reset values to known state otherwise previous test case runs can affect the success of this test case
        resetDocumentTypes()
        val output = root!!.resolve("output").toFile()
        sourceFolder = TestUtils.testResourcesAsSourceFolder
        configuration = ConfigUtil().loadConfig(sourceFolder) as DefaultJBakeConfiguration
        configuration.destinationFolder = (output)
        configuration.setTemplateFolder(File(sourceFolder, "groovyMarkupTemplates"))
        configuration.setProperty("template.paper.file", "paper.tpl")
    }

    @AfterEach
    fun tearDown() {
        if (contentStore != null && contentStore!!.isActive) {
            contentStore!!.close()
            contentStore!!.shutdown()
        }
    }

    @Test
    fun bakeWithAbsolutePaths() {
        configuration.setTemplateFolder(File(sourceFolder, "groovyMarkupTemplates"))
        configuration.setContentFolder(File(sourceFolder, "content"))
        configuration.setAssetFolder(File(sourceFolder, "assets"))

        val oven = Oven(configuration)
        oven.bake()

        Assertions.assertThat<Throwable?>(oven.getErrors()).isEmpty()
    }

    @Test
    fun shouldBakeWithRelativeCustomPaths() {
        sourceFolder = TestUtils.getTestResourcesAsSourceFolder("/fixture-custom-relative")
        configuration = ConfigUtil().loadConfig(sourceFolder!!) as DefaultJBakeConfiguration
        val assetFolder = File(configuration.getDestinationFolder(), "css")
        val aboutFile = File(configuration.getDestinationFolder(), "about.html")
        val blogSubFolder = File(configuration.getDestinationFolder(), "blog")


        val oven = Oven(configuration)
        oven.bake()

        Assertions.assertThat<Throwable?>(oven.getErrors()).isEmpty()
        Assertions.assertThat(configuration.getDestinationFolder()).isNotEmptyDirectory()
        Assertions.assertThat(assetFolder).isNotEmptyDirectory()
        Assertions.assertThat(aboutFile).isFile()
        Assertions.assertThat(aboutFile).isNotEmpty()
        Assertions.assertThat(blogSubFolder).isNotEmptyDirectory()
    }

    @Test
    fun shouldBakeWithAbsoluteCustomPaths() {
        // given

        val source = root!!.resolve("source")
        val theme = root!!.resolve("theme")
        val destination = root!!.resolve("destination")

        val originalSource = TestUtils.testResourcesAsSourceFolder
        FileUtils.copyDirectory(originalSource, source.toFile())
        val originalTheme = TestUtils.getTestResourcesAsSourceFolder("/fixture-theme")
        FileUtils.copyDirectory(originalTheme, theme.toFile())

        val expectedTemplateFolder = theme.resolve("templates")
        val expectedAssetFolder = theme.resolve("assets")
        val expectedDestination = destination.resolve("output")

        val properties = source.resolve("jbake.properties")


        val fw = Files.newBufferedWriter(properties)

        fw.write(PropertyList.ASSET_FOLDER.key + "=" + TestUtils.getOsPath(expectedAssetFolder))
        fw.newLine()
        fw.write(PropertyList.TEMPLATE_FOLDER.key + "=" + TestUtils.getOsPath(expectedTemplateFolder))
        fw.newLine()
        fw.write(PropertyList.DESTINATION_FOLDER.key + "=" + TestUtils.getOsPath(expectedDestination))
        fw.close()

        configuration = ConfigUtil().loadConfig(source.toFile()) as DefaultJBakeConfiguration
        val assetFolder = File(configuration.getDestinationFolder(), "css")
        val aboutFile = File(configuration.getDestinationFolder(), "about.html")
        val blogSubFolder = File(configuration.getDestinationFolder(), "blog")


        val oven = Oven(configuration)
        oven.bake()

        Assertions.assertThat<Throwable?>(oven.getErrors()).isEmpty()
        Assertions.assertThat(configuration.getDestinationFolder()).isNotEmptyDirectory()
        Assertions.assertThat(assetFolder).isNotEmptyDirectory()
        Assertions.assertThat(aboutFile).isFile()
        Assertions.assertThat(aboutFile).isNotEmpty()
        Assertions.assertThat(blogSubFolder).isNotEmptyDirectory()
    }


    @Test
    fun shouldThrowExceptionIfSourceFolderDoesNotExist() {
        configuration.setSourceFolder(root!!.resolve("none").toFile())

        org.junit.jupiter.api.Assertions.assertThrows<JBakeException?>(
            JBakeException::class.java,
            Executable { Oven(configuration) })
    }

    @Test
    fun shouldInstantiateNeededUtensils() {
        val template = TestUtils.newFolder(root!!.toFile(), "template")
        val content = TestUtils.newFolder(root!!.toFile(), "content")
        val assets = TestUtils.newFolder(root!!.toFile(), "assets")

        configuration.setTemplateFolder(template)
        configuration.setContentFolder(content)
        configuration.setAssetFolder(assets)

        val oven = Oven(configuration)

        Assertions.assertThat<ContentStore?>(oven.utensils.contentStore).isNotNull()
        Assertions.assertThat<Crawler?>(oven.utensils.crawler).isNotNull()
        Assertions.assertThat<Renderer?>(oven.utensils.renderer).isNotNull()
        Assertions.assertThat<Asset?>(oven.utensils.asset).isNotNull()
        Assertions.assertThat<JBakeConfiguration?>(oven.utensils.configuration).isEqualTo(configuration)
    }

    @Test
    fun shouldInspectConfigurationDuringInstantiationFromUtils() {
        configuration.setSourceFolder(root!!.resolve("none").toFile())

        val contentStore = Mockito.mock(ContentStore::class.java)
        val crawler = Mockito.mock(Crawler::class.java)
        val renderer = Mockito.mock(Renderer::class.java)
        val asset = Mockito.mock(Asset::class.java)

        val utensils = Utensils(
            configuration = configuration,
            contentStore = contentStore,
            crawler = crawler,
            renderer = renderer,
            asset = asset
        )

        org.junit.jupiter.api.Assertions.assertThrows<JBakeException?>(
            JBakeException::class.java,
            Executable { Oven(utensils) })
    }

    @Test
    fun shouldCrawlRenderAndCopyAssets() {
        val template = TestUtils.newFolder(root!!.toFile(), "template")
        val content = TestUtils.newFolder(root!!.toFile(), "content")
        val assets = TestUtils.newFolder(root!!.toFile(), "assets")

        configuration.setTemplateFolder(template)
        configuration.setContentFolder(content)
        configuration.setAssetFolder(assets)

        contentStore = Mockito.spy<ContentStore?>(ContentStore("memory", "documents" + System.currentTimeMillis()))

        val crawler = Mockito.mock<Crawler?>(Crawler::class.java)
        val renderer = Mockito.mock<Renderer?>(Renderer::class.java)
        val asset = Mockito.mock<Asset?>(Asset::class.java)

        val utensils = Utensils(
            configuration = configuration,
            contentStore = contentStore!!,
            renderer = renderer!!,
            crawler = crawler!!,
            asset = asset!!
        )

        val oven = Oven(utensils)

        oven.bake()

        Mockito.verify<ContentStore?>(contentStore, Mockito.times(1)).startup()
        Mockito.verify<Renderer?>(renderer, Mockito.atLeastOnce()).renderIndex(ArgumentMatchers.anyString())
        Mockito.verify<Crawler?>(crawler, Mockito.times(1)).crawl()
        Mockito.verify<Asset?>(asset, Mockito.times(1)).copy()
    }

    @Test
    fun localeConfiguration() {
        val language = configuration.getJvmLocale()

        val oven = Oven(configuration)
        oven.bake()

        MatcherAssert.assertThat<Locale?>(Locale.getDefault(), Is.`is`<Locale?>(Locale(language)))
    }

    @Test
    fun noLocaleConfiguration() {
        configuration.setProperty(PropertyList.JVM_LOCALE.key, null)

        val language = Locale.getDefault().getLanguage()
        val oven = Oven(configuration)
        oven.bake()

        MatcherAssert.assertThat<Locale?>(Locale.getDefault(), Is.`is`<Locale?>(Locale(language)))
    }
}
