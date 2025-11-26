package org.jbake.app

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.PropertyList
import org.jbake.model.DocumentTypes.resetDocumentTypes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class OvenTest {

    @TempDir var root: Path? = null

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
        configuration.templateFolder = (File(sourceFolder, "groovyMarkupTemplates"))
        configuration.setProperty("template.paper.file", "paper.tpl")
    }

    @AfterEach
    fun tearDown() {
        if (contentStore != null && contentStore!!.isActive) {
            contentStore!!.close()
            contentStore!!.shutdown()
        }
    }

    @Test fun bakeWithAbsolutePaths() {
        configuration.templateFolder = (File(sourceFolder, "groovyMarkupTemplates"))
        configuration.contentFolder = (File(sourceFolder, "content"))
        configuration.assetFolder = (File(sourceFolder, "assets"))

        val oven = Oven(configuration)
        oven.bakeEverything()

        assertThat(oven.errors).isEmpty()
    }

    @Test fun shouldBakeWithRelativeCustomPaths() {
        sourceFolder = TestUtils.getTestResourcesAsSourceFolder("/fixture-custom-relative")
        configuration = ConfigUtil().loadConfig(sourceFolder) as DefaultJBakeConfiguration
        val assetFolder = File(configuration.destinationFolder, "css")
        val aboutFile = File(configuration.destinationFolder, "about.html")
        val blogSubFolder = File(configuration.destinationFolder, "blog")


        val oven = Oven(configuration)
        oven.bakeEverything()

        assertThat(oven.errors).isEmpty()
        assertThat(configuration.destinationFolder).isNotEmptyDirectory()
        assertThat(assetFolder).isNotEmptyDirectory()
        assertThat(aboutFile).isFile()
        assertThat(aboutFile).isNotEmpty()
        assertThat(blogSubFolder).isNotEmptyDirectory()
    }

    @Test fun shouldBakeWithAbsoluteCustomPaths() {
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
        val assetFolder = File(configuration.destinationFolder, "css")
        val aboutFile = File(configuration.destinationFolder, "about.html")
        val blogSubFolder = File(configuration.destinationFolder, "blog")


        val oven = Oven(configuration)
        oven.bakeEverything()

        assertThat(oven.errors).isEmpty()
        assertThat(configuration.destinationFolder).isNotEmptyDirectory()
        assertThat(assetFolder).isNotEmptyDirectory()
        assertThat(aboutFile).isFile()
        assertThat(aboutFile).isNotEmpty()
        assertThat(blogSubFolder).isNotEmptyDirectory()
    }


    @Test fun shouldThrowExceptionIfSourceFolderDoesNotExist() {
        configuration.setSourceFolder(root!!.resolve("none").toFile())

        assertThrows( JBakeException::class.java){ Oven(configuration) }
    }

    @Test fun shouldInstantiateNeededUtensils() {
        val template = TestUtils.newFolder(root!!.toFile(), "template")
        val content = TestUtils.newFolder(root!!.toFile(), "content")
        val assets = TestUtils.newFolder(root!!.toFile(), "assets")

        configuration.templateFolder = (template)
        configuration.contentFolder = (content)
        configuration.assetFolder = (assets)

        val oven = Oven(configuration)

        assertThat(oven.utensils.contentStore).isNotNull()
        assertThat(oven.utensils.crawler).isNotNull()
        assertThat(oven.utensils.renderer).isNotNull()
        assertThat(oven.utensils.asset).isNotNull()
        assertThat(oven.utensils.configuration).isEqualTo(configuration)
    }

    @Test fun shouldInspectConfigurationDuringInstantiationFromUtils() {
        configuration.setSourceFolder(root!!.resolve("none").toFile())

        val contentStore = mock(ContentStore::class.java)
        val crawler = mock(Crawler::class.java)
        val renderer = mock(Renderer::class.java)
        val asset = mock(Asset::class.java)

        val utensils = Utensils(
            configuration = configuration,
            contentStore = contentStore,
            crawler = crawler,
            renderer = renderer,
            asset = asset
        )

        assertThrows(JBakeException::class.java){ Oven(utensils) }
    }

    @Test fun shouldCrawlRenderAndCopyAssets() {
        val template = TestUtils.newFolder(root!!.toFile(), "template")
        val content = TestUtils.newFolder(root!!.toFile(), "content")
        val assets = TestUtils.newFolder(root!!.toFile(), "assets")

        configuration.templateFolder = (template)
        configuration.contentFolder = (content)
        configuration.assetFolder = (assets)

        contentStore = spy(ContentStore("memory", "documents" + System.currentTimeMillis()))

        val crawler = mock(Crawler::class.java)
        val renderer = mock(Renderer::class.java)
        val asset = mock(Asset::class.java)

        val utensils = Utensils(
            configuration = configuration,
            contentStore = contentStore!!,
            renderer = renderer!!,
            crawler = crawler!!,
            asset = asset!!
        )

        val oven = Oven(utensils)

        oven.bakeEverything()

        verify(contentStore!!, times(1)).startup()
        verify(renderer, atLeastOnce()).renderIndex(ArgumentMatchers.anyString())
        verify(crawler, times(1)).crawl()
        verify(asset, times(1)).copy()
    }

    @Test fun localeConfiguration() {
        val language = configuration.jvmLocale

        val oven = Oven(configuration)
        oven.bakeEverything()

        MatcherAssert.assertThat(Locale.getDefault(), Is.`is`(Locale(language)))
    }

    @Test fun noLocaleConfiguration() {
        configuration.setProperty(PropertyList.JVM_LOCALE.key, null)

        val language = Locale.getDefault().language
        val oven = Oven(configuration)
        oven.bakeEverything()

        MatcherAssert.assertThat(Locale.getDefault(), Is.`is`(Locale(language)))
    }
}
