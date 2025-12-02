package org.jbake

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.io.path.createTempDirectory

class BuiltInProjectsTest : StringSpec({
    val jbakeExec = BinaryRunner.jbakeExecutableRelative.absolutePath

    fun testTemplate(projectName: String, extension: String) {
        "$projectName template should bake successfully" {
            val tempDir = createTempDirectory("jbake-smoke-test").toFile()
            try {
                val projectDir = File(tempDir, "project")
                val templateDir = File(projectDir, "templates")
                val outputDir = File(projectDir, "output")
                val runner = BinaryRunner(projectDir)

                // Initialize project
                val initProcess = runner.runWithArguments(jbakeExec, "-i", "-t", projectName)
                initProcess.exitValue() shouldBe 0
                File(projectDir, "jbake.properties").shouldExist()
                File(templateDir, "index.$extension").shouldExist()
                initProcess.destroy()

                // Bake project
                val bakeProcess = runner.runWithArguments(jbakeExec, "-b")
                bakeProcess.exitValue() shouldBe 0
                File(outputDir, "index.html").shouldExist()
                bakeProcess.destroy()
            }
            finally { tempDir.deleteRecursively() }
        }
    }

    // Test each template
    testTemplate("thymeleaf", "thyme")
    testTemplate("freemarker", "ftl")
    testTemplate("jade", "jade")
    testTemplate("groovy", "gsp")
    testTemplate("groovy-mte", "tpl")
})
