package org.jbake.app

import org.assertj.core.api.Assertions
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.launcher.Init
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class InitTest {
    @Rule
    var folder: TemporaryFolder = TemporaryFolder()

    var config: DefaultJBakeConfiguration? = null
    private var rootPath: File? = null

    @Before
    fun setup() {
        rootPath = TestUtils.testResourcesAsSourceFolder
        if (!rootPath!!.exists()) {
            throw Exception("Cannot find base path for test!")
        }
        config = ConfigUtil().loadConfig(rootPath!!) as DefaultJBakeConfiguration
        // override base template config option
        config!!.setExampleProject("freemarker", "test.zip")
    }

    @Test
    fun initOK() {
        val init = Init(config!!)
        val initPath = folder.newFolder("init")
        init.run(initPath, rootPath, "freemarker")
        val testFile = File(initPath, "testfile.txt")
        Assertions.assertThat(testFile).exists()
    }

    @Test
    @Throws(IOException::class)
    fun initFailDestinationContainsContent() {
        val init = Init(config!!)
        val initPath = folder.newFolder("init")
        val contentFolder = File(initPath.getPath(), config!!.getContentFolderName())
        contentFolder.mkdir()
        try {
            init.run(initPath, rootPath, "freemarker")
            Assertions.fail<Any?>("Shouldn't be able to initialise folder with content folder within it!")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val testFile = File(initPath, "testfile.txt")
        Assertions.assertThat(testFile).doesNotExist()
    }

    @Test
    @Throws(IOException::class)
    fun initFailInvalidTemplateType() {
        val init = Init(config!!)
        val initPath = folder.newFolder("init")
        try {
            init.run(initPath, rootPath, "invalid")
            Assertions.fail<Any?>("Shouldn't be able to initialise folder with invalid template type")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val testFile = File(initPath, "testfile.txt")
        Assertions.assertThat(testFile).doesNotExist()
    }
}
