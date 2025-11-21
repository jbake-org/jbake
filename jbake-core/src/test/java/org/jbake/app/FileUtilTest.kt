package org.jbake.app

import org.assertj.core.api.Assertions
import org.jbake.TestUtils
import org.jbake.app.FileUtil.getUriPathToContentRoot
import org.jbake.app.FileUtil.isFileInDirectory
import org.jbake.app.FileUtil.runningLocation
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.junit.Assert
import org.junit.Test
import java.io.File

/**
 * Created by frank on 28.03.16.
 */
class FileUtilTest {
    @Test
    fun testGetRunningLocation() {
        val path = runningLocation
        Assert.assertEquals(File("build/classes").absolutePath, path.path)
    }

    @Test
    fun testIsFileInDirectory() {
        val fixtureDir = File(this.javaClass.getResource("/fixture").file)
        val jbakeFile = File(fixtureDir.getCanonicalPath() + File.separatorChar + "jbake.properties")
        Assert.assertTrue(
            "jbake.properties expected to be in /fixture directory",
            isFileInDirectory(jbakeFile, fixtureDir)
        )

        val contentFile =
            File(fixtureDir.getCanonicalPath() + File.separatorChar + "content" + File.separatorChar + "projects.html")
        Assert.assertTrue(
            "projects.html expected to be nested in the /fixture directory",
            isFileInDirectory(contentFile, fixtureDir)
        )

        val contentDir = contentFile.getParentFile()
        Assert.assertFalse(
            "jbake.properties file should not be in the /fixture/content directory",
            isFileInDirectory(jbakeFile, contentDir)
        )
    }

    @Test
    fun testGetContentRoothPath() {
        val source = TestUtils.testResourcesAsSourceFolder
        val util = ConfigUtil()
        val config = util.loadConfig(source) as DefaultJBakeConfiguration

        var path = getUriPathToContentRoot(config, File(config.contentFolder, "index.html"))
        Assertions.assertThat(path).isEqualTo("")

        path = getUriPathToContentRoot(config, File(config.contentFolder, "/blog/index.html"))
        Assertions.assertThat(path).isEqualTo("../")

        path = getUriPathToContentRoot(config, File(config.contentFolder, "/blog/level2/index.html"))
        Assertions.assertThat(path).isEqualTo("../../")
    }
}
