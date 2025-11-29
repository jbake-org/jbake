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
    lateinit var rootPath: File
    lateinit var tempDir: File

    beforeTest {
        rootPath = TestUtils.testResourcesAsSourceFolder
        if (!rootPath.exists()) throw Exception("Cannot find base path for test!")
        config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration
        // Override base template config option.
        config.setExampleProject("freemarker", "test.zip")

        // Create temp directory for each test
        tempDir = Files.createTempDirectory("jbake-init-test").toFile()
    }

    afterTest {
        // Clean up temp directory
        tempDir.deleteRecursively()
    }

    "initOK" {
        val init = Init(config)
        val initPath = File(tempDir, "init")
        initPath.mkdir()
        init.run(initPath, rootPath, "freemarker")
        val testFile = File(initPath, "testfile.txt")
        testFile.shouldExist()
    }

    "initFailDestinationContainsContent" {
        val init = Init(config)
        val initPath = File(tempDir, "init")
        initPath.mkdir()
        val contentDir = File(initPath.path, config.contentDirName ?: "content")
        contentDir.mkdir()

        shouldThrow<Exception> {
            init.run(initPath, rootPath, "freemarker")
        }

        val testFile = File(initPath, "testfile.txt")
        testFile.shouldNotExist()
    }

    "initFailInvalidTemplateType" {
        val init = Init(config)
        val initPath = File(tempDir, "init")
        initPath.mkdir()

        shouldThrow<Exception> {
            init.run(initPath, rootPath, "invalid")
        }

        val testFile = File(initPath, "testfile.txt")
        testFile.shouldNotExist()
    }
})

