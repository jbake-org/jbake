package org.jbake.app

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.launcher.Init
import java.io.File
import java.nio.file.Files

class InitTest : StringSpec({

    lateinit var config: DefaultJBakeConfiguration
    var rootPath: File = TestUtils.testResourcesAsSourceDir
    lateinit var tempDir: File
    lateinit var init: Init
    lateinit var initPath: File

    beforeTest {
        if (!rootPath.exists()) throw Exception("Cannot find base path for test!")
        config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
        // Override base template config option.
        config.setExampleProject("freemarker", "test.zip")

        // Create temp directory for each test
        tempDir = Files.createTempDirectory("jbake-init-test").toFile()
        initPath = tempDir.resolve("init").apply { mkdir() }
        init = Init(config)
    }

    afterTest {
        tempDir.deleteRecursively()
    }

    "initOK" {
        init.run(initPath, rootPath, "freemarker")
        initPath.resolve("testfile.txt").shouldExist()
    }

    "initFailDestinationContainsContent" {
        File(initPath.path, config.contentDirName ?: "content").mkdir()

        shouldThrow<Exception> {
            init.run(initPath, rootPath, "freemarker")
        }

        initPath.resolve("testfile.txt").shouldNotExist()
    }

    "initFailInvalidTemplateType" {
        shouldThrow<Exception> {
            init.run(initPath, rootPath, "invalid")
        }

        initPath.resolve("testfile.txt").shouldNotExist()
    }
})
