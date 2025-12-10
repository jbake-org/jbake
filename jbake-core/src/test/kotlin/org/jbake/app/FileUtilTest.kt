package org.jbake.app

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.jbake.TestUtils
import org.jbake.app.FileUtil.getUriPathToContentRoot
import org.jbake.app.FileUtil.isFileInDirectory
import org.jbake.app.FileUtil.templateDirForTestsOrForRuntime
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import java.io.File

class FileUtilTest : StringSpec({

    "testGetRunningLocation" {

        // The point of this test is to check the System properties being passed form the build tool, and FileUtil.runningLocation taking it properly.

        val path = templateDirForTestsOrForRuntime

        // If jbake.buildOutputDir is set, runningLocation resolves to classes subdirectory
        val buildOutputDir = System.getProperty("jbake.buildOutputDir")
        if (buildOutputDir != null) {
            val expectedClassesDir = File(buildOutputDir, "classes")
            withClue("runningLocation should point to jbake.buildOutputDir/classes/java/main or jbake.buildOutputDir, but was: ${path.path}; jbake.buildOutputDir = $buildOutputDir") {
                if (expectedClassesDir.exists())
                    path.path shouldBe expectedClassesDir.path
                else
                    path.path shouldBe buildOutputDir
            }
        }
        else {
            // Otherwise, check for standard build output directories
            withClue("runningLocation should point to build/classes/java/main or target/classes, but was: ${path.path}; jbake.buildOutputDir = ${System.getProperty("jbake.buildOutputDir")}") {
                val pathA = File("build/classes").absolutePath
                val pathB = File("target/classes").absolutePath
                (path.path == pathA || path.path == pathB).shouldBeTrue()
            }
        }
    }

    "testIsFileInDirectory" {
        val fixtureDir = File(this.javaClass.getResource("/fixture").file)
        val jbakeFile = fixtureDir.resolve("jbake.properties")
        isFileInDirectory(jbakeFile, fixtureDir).shouldBeTrue()

        val contentFile = File(fixtureDir.resolve("content"), "projects.html")
        isFileInDirectory(contentFile, fixtureDir).shouldBeTrue()

        val contentDir = contentFile.getParentFile()
        isFileInDirectory(jbakeFile, contentDir).shouldBeFalse()
    }

    "testGetContentRoothPath" {
        val source = TestUtils.testResourcesAsSourceDir
        val util = ConfigUtil()
        val config = util.loadConfig(source) as DefaultJBakeConfiguration

        var path = getUriPathToContentRoot(config, config.contentDir.resolve("index.html"))
        path shouldBe ""

        path = getUriPathToContentRoot(config, config.contentDir.resolve("blog/index.html"))
        path shouldBe "../"

        path = getUriPathToContentRoot(config, config.contentDir.resolve("blog/level2/index.html"))
        path shouldBe "../../"
    }
})

