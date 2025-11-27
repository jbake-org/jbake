package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.jbake.TestUtils
import org.jbake.app.FileUtil.getUriPathToContentRoot
import org.jbake.app.FileUtil.isFileInDirectory
import org.jbake.app.FileUtil.runningLocation
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import java.io.File

class FileUtilTest : StringSpec({

    "testGetRunningLocation" {
        val path = runningLocation
        val pathA = File("build/classes").absolutePath
        val pathB = File("target/classes").absolutePath
        (path.path == pathA || path.path == pathB).shouldBeTrue()
    }

    "testIsFileInDirectory" {
        val fixtureDir = File(this.javaClass.getResource("/fixture").file)
        val jbakeFile = File(fixtureDir, "jbake.properties")
        isFileInDirectory(jbakeFile, fixtureDir).shouldBeTrue()

        val contentFile = File(File(fixtureDir, "content"), "projects.html")
        isFileInDirectory(contentFile, fixtureDir).shouldBeTrue()

        val contentDir = contentFile.getParentFile()
        isFileInDirectory(jbakeFile, contentDir).shouldBeFalse()
    }

    "testGetContentRoothPath" {
        val source = TestUtils.testResourcesAsSourceFolder
        val util = ConfigUtil()
        val config = util.loadConfig(source) as DefaultJBakeConfiguration

        var path = getUriPathToContentRoot(config, File(config.contentFolder, "index.html"))
        path shouldBe ""

        path = getUriPathToContentRoot(config, File(config.contentFolder, "/blog/index.html"))
        path shouldBe "../"

        path = getUriPathToContentRoot(config, File(config.contentFolder, "/blog/level2/index.html"))
        path shouldBe "../../"
    }
})

