package org.jbake.app

import org.assertj.core.api.Assertions.assertThat
import org.jbake.TestUtils
import org.jbake.app.FileUtil.getUriPathToContentRoot
import org.jbake.app.FileUtil.isFileInDirectory
import org.jbake.app.FileUtil.runningLocation
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FileUtilTest {

    @Test fun testGetRunningLocation() {
        val path = runningLocation
        val pathA = File("build/classes").absolutePath
        val pathB = File("target/classes").absolutePath
        assertTrue("Running location should be $pathA or $pathB, was: ${path.path}", path.path == pathA || path.path == pathB)
    }

    @Test fun testIsFileInDirectory() {
        val fixtureDir = File(this.javaClass.getResource("/fixture").file)
        val jbakeFile = File(fixtureDir, "jbake.properties")
        assertTrue("jbake.properties expected to be in /fixture directory", isFileInDirectory(jbakeFile, fixtureDir))

        val contentFile = File(File(fixtureDir, "content"), "projects.html")
        assertTrue("projects.html expected to be nested in the /fixture directory", isFileInDirectory(contentFile, fixtureDir))

        val contentDir = contentFile.getParentFile()
        assertFalse("jbake.properties file should not be in the /fixture/content directory", isFileInDirectory(jbakeFile, contentDir))
    }

    @Test fun testGetContentRoothPath() {
        val source = TestUtils.testResourcesAsSourceFolder
        val util = ConfigUtil()
        val config = util.loadConfig(source) as DefaultJBakeConfiguration

        var path = getUriPathToContentRoot(config, File(config.contentFolder, "index.html"))
        assertThat(path).isEqualTo("")

        path = getUriPathToContentRoot(config, File(config.contentFolder, "/blog/index.html"))
        assertThat(path).isEqualTo("../")

        path = getUriPathToContentRoot(config, File(config.contentFolder, "/blog/level2/index.html"))
        assertThat(path).isEqualTo("../../")
    }
}
