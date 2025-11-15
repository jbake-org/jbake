package org.jbake.app.configuration

import org.assertj.core.api.AssertionsForClassTypes
import org.jbake.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito
import java.io.File
import java.io.FileWriter
import java.util.*


class JBakeConfigurationFactoryTest {
    @TempDir
    var root: File? = null

    @Test
    @Throws(Exception::class)
    fun shouldReturnDefaultConfigurationWithDefaultFolders() {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val templateFolder = TestUtils.newFolder(root, "templates")
        val assetFolder = TestUtils.newFolder(root, "assets")

        val configuration: JBakeConfiguration =
            JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)

        assertThat(configuration.sourceFolder).isEqualTo(sourceFolder)
        assertThat(configuration.destinationFolder).isEqualTo(destinationFolder)
        assertThat(configuration.templateFolder).isEqualTo(templateFolder)
        assertThat(configuration.assetFolder).isEqualTo(assetFolder)
        assertThat(configuration.clearCache).isEqualTo(true)
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnDefaultConfigurationWithCustomFolders() {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output/custom")
        val templateFolder = TestUtils.newFolder(root, "templates/custom")
        val assetFolder = TestUtils.newFolder(root, "assets/custom")
        val contentFolder = TestUtils.newFolder(root, "content/custom")


        val properties = File(sourceFolder, "jbake.properties")

        val pw = FileWriter(properties)
        pw.write("template.folder=templates/custom\n")
        pw.write("asset.folder=assets/custom\n")
        pw.write("content.folder=content/custom\n")
        pw.close()

        val configuration: JBakeConfiguration =
            JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)

        assertThat(configuration.templateFolderName).isEqualTo("templates/custom")
        assertThat(configuration.assetFolderName).isEqualTo("assets/custom")
        assertThat(configuration.contentFolderName).isEqualTo("content/custom")

        assertThat(configuration.sourceFolder).isEqualTo(sourceFolder)
        assertThat(configuration.destinationFolder).isEqualTo(destinationFolder)
        assertThat(configuration.templateFolder).isEqualTo(templateFolder)
        assertThat(configuration.assetFolder).isEqualTo(assetFolder)
        assertThat(configuration.contentFolder).isEqualTo(contentFolder)

        assertThat(configuration.clearCache).isEqualTo(true)
    }


    @Test
    @Throws(Exception::class)
    fun shouldReturnADefaultConfigurationWithSitehost() {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val siteHost = "http://www.jbake.org"

        val configuration: JBakeConfiguration =
            JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)

        assertThat(configuration.siteHost).isEqualTo(siteHost)
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnAJettyConfiguration() {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val siteHost = "http://localhost:8820/"

        val configuration: JBakeConfiguration =
            JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, destinationFolder, true)

        assertThat(configuration.siteHost).isEqualTo(siteHost)
    }

    @Test
    @Throws(Exception::class)
    fun shouldUseDefaultEncodingUTF8() {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val factory = JBakeConfigurationFactory()
        val configuration: JBakeConfiguration =
            factory.createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)

        AssertionsForClassTypes.assertThat(factory.configUtil.encoding).isEqualTo("UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun shouldUseCustomEncoding() {
        val util = Mockito.spy<ConfigUtil>(ConfigUtil::class.java)
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val factory = JBakeConfigurationFactory()
        factory.configUtil = util
        val configuration: JBakeConfiguration = factory.setEncoding("latin1")
            .createDefaultJbakeConfiguration(sourceFolder, destinationFolder, null as File?, true)

        AssertionsForClassTypes.assertThat(factory.configUtil.encoding).isEqualTo("latin1")
        Mockito.verify<ConfigUtil?>(util).loadConfig(sourceFolder, null)
    }

    @Test
    fun shouldBeAbleToAddCustomProperties() {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val config = JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)
        val properties = Properties()
        properties.setProperty("custom.key", "custom value")
        properties.setProperty("custom.key2", "custom value 2")

        config.addConfiguration(properties)

        AssertionsForClassTypes.assertThat<Any?>(config.get("custom.key")).isEqualTo("custom value")
        AssertionsForClassTypes.assertThat<Any?>(config.get("custom.key2")).isEqualTo("custom value 2")
    }
}
