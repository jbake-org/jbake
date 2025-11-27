package org.jbake.app

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.launcher.Init
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class InitTest {

    @Rule @JvmField
    var folder: TemporaryFolder = TemporaryFolder()

    private lateinit var config: DefaultJBakeConfiguration
    private lateinit var rootPath: File

    @Before
    fun setup() {
        rootPath = TestUtils.testResourcesAsSourceFolder
        if (!rootPath.exists()) throw Exception("Cannot find base path for test!")
        config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
        // Override base template config option.
        config.setExampleProject("freemarker", "test.zip")
    }

    @Test fun initOK() {
        val init = Init(config)
        val initPath = folder.newFolder("init")
        init.run(initPath, rootPath, "freemarker")
        val testFile = File(initPath, "testfile.txt")
        assertThat(testFile).exists()
    }

    @Test fun initFailDestinationContainsContent() {
        val init = Init(config)
        val initPath = folder.newFolder("init")
        val contentFolder = File(initPath.path, config.contentFolderName)
        contentFolder.mkdir()
        try {
            init.run(initPath, rootPath, "freemarker")
            fail("Shouldn't be able to initialise folder with content folder within it!")
        } catch (e: Exception) {/* Expected. */ }

        val testFile = File(initPath, "testfile.txt")
        assertThat(testFile).doesNotExist()
    }

    @Test fun initFailInvalidTemplateType() {
        val init = Init(config)
        val initPath = folder.newFolder("init")
        try {
            init.run(initPath, rootPath, "invalid")
            fail("Shouldn't be able to initialise folder with invalid template type")
        } catch (e: Exception) { /* Expected. */ }

        val testFile = File(initPath, "testfile.txt")
        assertThat(testFile).doesNotExist()
    }
}
