package org.jbake.app.configuration

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.jbake.TestUtils
import java.io.File
import java.io.FileWriter
import java.util.*

class JBakeConfigurationFactoryTest : StringSpec({
    var root: File? = null

    "shouldReturnDefaultConfigurationWithDefaultFolders" {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val templateFolder = TestUtils.newFolder(root, "templates")
        val assetFolder = TestUtils.newFolder(root, "assets")

        val configuration = JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)

        configuration.sourceFolder shouldBe sourceFolder
        configuration.destinationFolder shouldBe destinationFolder
        configuration.templateFolder shouldBe templateFolder
        configuration.assetFolder shouldBe assetFolder
        configuration.clearCache shouldBe true
    }

    "shouldReturnDefaultConfigurationWithCustomFolders" {
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

        val configuration = JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)

        configuration.templateFolderName shouldBe "templates/custom"
        configuration.assetFolderName shouldBe "assets/custom"
        configuration.contentFolderName shouldBe "content/custom"

        configuration.sourceFolder shouldBe sourceFolder
        configuration.destinationFolder shouldBe destinationFolder
        configuration.templateFolder shouldBe templateFolder
        configuration.assetFolder shouldBe assetFolder
        configuration.contentFolder shouldBe contentFolder

        configuration.clearCache shouldBe true
    }


    "shouldReturnADefaultConfigurationWithSitehost" {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val siteHost = "http://www.jbake.org"

        val configuration = JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)

        configuration.siteHost shouldBe siteHost
    }

    "shouldReturnAJettyConfiguration" {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val siteHost = "http://localhost:8820/"

        val configuration = JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceFolder, destinationFolder, true)

        configuration.siteHost shouldBe siteHost
    }

    "shouldUseDefaultEncodingUTF8" {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val factory = JBakeConfigurationFactory()
        factory.createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)

        factory.configUtil.encoding shouldBe "UTF-8"
    }

    "shouldUseCustomEncoding" {
        val util = spyk(ConfigUtil())
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val factory = JBakeConfigurationFactory()
        factory.configUtil = util
        factory.setEncoding("latin1")
            .createDefaultJbakeConfiguration(sourceFolder, destinationFolder, null as File?, true)

        factory.configUtil.encoding shouldBe "latin1"
        verify { util.loadConfig(sourceFolder, null) }
    }

    "shouldBeAbleToAddCustomProperties" {
        val sourceFolder = root!!
        val destinationFolder = TestUtils.newFolder(root, "output")
        val config = JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceFolder, destinationFolder, true)
        val properties = Properties()
        properties.setProperty("custom.key", "custom value")
        properties.setProperty("custom.key2", "custom value 2")

        config.addConfiguration(properties)

        config.get("custom.key") shouldBe "custom value"
        config.get("custom.key2") shouldBe "custom value 2"
    }
})
