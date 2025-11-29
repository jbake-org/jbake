package org.jbake.app.configuration

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.jbake.TestUtils
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.util.*

class JBakeConfigurationFactoryTest : StringSpec({

    lateinit var root: File

    beforeTest {
        root = Files.createTempDirectory("jbake-test").toFile()
    }

    afterTest {
        root.deleteRecursively()
    }

    "shouldReturnDefaultConfigurationWithDefaultFolders" {
        val sourceDir = root
        val destinationFolder = TestUtils.newDir(root, "output")
        val templateDir = TestUtils.newDir(root, "templates")
        val assetDir = TestUtils.newDir(root, "assets")

        val configuration = JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceDir, destinationFolder, true)

        configuration.sourceDir shouldBe sourceDir
        configuration.destinationDir shouldBe destinationFolder
        configuration.templateDir shouldBe templateDir
        configuration.assetDir shouldBe assetDir
        configuration.clearCache shouldBe true
    }

    "shouldReturnDefaultConfigurationWithCustomFolders" {
        val sourceDir = root
        val destinationFolder = TestUtils.newDir(root, "output/custom")
        val templateDir = TestUtils.newDir(root, "templates/custom")
        val assetDir = TestUtils.newDir(root, "assets/custom")
        val contentDir = TestUtils.newDir(root, "content/custom")


        val properties = File(sourceDir, "jbake.properties")

        val pw = FileWriter(properties)
        pw.write("template.folder=templates/custom\n")
        pw.write("asset.folder=assets/custom\n")
        pw.write("content.folder=content/custom\n")
        pw.close()

        val configuration = JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceDir, destinationFolder, true)

        configuration.templateDirName shouldBe "templates/custom"
        configuration.assetDirName shouldBe "assets/custom"
        configuration.contentDirName shouldBe "content/custom"

        configuration.sourceDir shouldBe sourceDir
        configuration.destinationDir shouldBe destinationFolder
        configuration.templateDir shouldBe templateDir
        configuration.assetDir shouldBe assetDir
        configuration.contentDir shouldBe contentDir

        configuration.clearCache shouldBe true
    }


    "shouldReturnADefaultConfigurationWithSitehost" {
        val sourceDir = root
        val destinationFolder = TestUtils.newDir(root, "output")
        val siteHost = "http://www.jbake.org"

        val configuration = JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceDir, destinationFolder, true)

        configuration.siteHost shouldBe siteHost
    }

    "shouldReturnAJettyConfiguration" {
        val sourceDir = root
        val destinationFolder = TestUtils.newDir(root, "output")
        val siteHost = "http://localhost:8820/"

        val configuration = JBakeConfigurationFactory().createJettyJbakeConfiguration(sourceDir, destinationFolder, true)

        configuration.siteHost shouldBe siteHost
    }

    "shouldUseDefaultEncodingUTF8" {
        val sourceDir = root
        val destinationFolder = TestUtils.newDir(root, "output")
        val factory = JBakeConfigurationFactory()
        factory.createDefaultJbakeConfiguration(sourceDir, destinationFolder, true)

        factory.configUtil.encoding shouldBe "UTF-8"
    }

    "shouldUseCustomEncoding" {
        val util = spyk(ConfigUtil())
        val sourceDir = root
        val destinationFolder = TestUtils.newDir(root, "output")
        val factory = JBakeConfigurationFactory()
        factory.configUtil = util
        factory.setEncoding("latin1")
            .createDefaultJbakeConfiguration(sourceDir, destinationFolder, null as File?, true)

        factory.configUtil.encoding shouldBe "latin1"
        verify { util.loadConfig(sourceDir, null) }
    }

    "shouldBeAbleToAddCustomProperties" {
        val sourceDir = root
        val destinationFolder = TestUtils.newDir(root, "output")
        val config = JBakeConfigurationFactory().createDefaultJbakeConfiguration(sourceDir, destinationFolder, true)
        val properties = Properties()
        properties.setProperty("custom.key", "custom value")
        properties.setProperty("custom.key2", "custom value 2")

        config.addConfiguration(properties)

        config.get("custom.key") shouldBe "custom value"
        config.get("custom.key2") shouldBe "custom value 2"
    }
})
