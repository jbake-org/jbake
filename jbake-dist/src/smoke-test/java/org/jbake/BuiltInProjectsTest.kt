package org.jbake

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.io.path.createTempDirectory

class BuiltInProjectsTest : StringSpec({

    fun testTemplate(projectName: String, extension: String) {
        "$projectName template should bake successfully" {
            val jbakeExec = BinaryRunner.jbakeExecutableRelative.absolutePath

            val tempDir = createTempDirectory("jbake-smoke-test").toFile()
            try {
                val projectDir = File(tempDir, "project")
                val runner = BinaryRunner(tempDir)

                // Run JBake to initialize the dir - should create project/, templates/ etc.
                val initProcess = runner.runWithArguments(jbakeExec, "-i", "-t", projectName)

                initProcess.exitValue() shouldBe 0
                File(tempDir, "jbake.properties").shouldExist()

                val templateDir = File(tempDir, "templates") //.also { it.mkdir() }
                File(templateDir, "index.$extension").shouldExist()
                initProcess.destroy()

                // Bake project
                val bakeProcess = runner.runWithArguments(jbakeExec, "-b")
                bakeProcess.exitValue() shouldBe 0

                val outputDir = File(tempDir, "output") //.also { it.mkdir() }
                File(outputDir, "index.html").shouldExist()
                bakeProcess.destroy()

                tempDir.deleteRecursively()
            }
            finally { /*tempDir.deleteRecursively()*/ }
        }
    }

    // Test each template
    testTemplate("thymeleaf", "thyme")
    testTemplate("freemarker", "ftl")
    testTemplate("jade", "jade")
    testTemplate("groovy", "gsp")
    testTemplate("groovy-mte", "tpl")
})
