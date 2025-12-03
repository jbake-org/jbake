package org.jbake

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.IOException
import kotlin.io.path.createTempDirectory

class BuiltInProjectsTest : StringSpec({

    fun testTemplate(projectName: String, extension: String) {
        "$projectName template should bake successfully" {
            val jbakeExec = BinaryRunner.jbakeExecutableRelative.absolutePath

            val tempDir = createTempDirectory("jbake-smoke-test").toFile()
            try {
                val projectDir = tempDir.resolve("project")
                val runner = BinaryRunner(tempDir)

                // Run JBake to initialize the dir - should create project/, templates/ etc.
                val initProcess = runner.runWithArguments(jbakeExec, "-i", "-t", projectName)

                initProcess.exitValue() shouldBe 0
                tempDir.resolve("jbake.properties").shouldExist()

                val templateDir = tempDir.resolve("templates") //.also { it.mkdir() }
                File(templateDir, "index.$extension").shouldExist()
                initProcess.destroy()

                // Bake project
                val bakeProcess = runner.runWithArguments(jbakeExec, "-b")
                val jbakeOutput = try {
                    bakeProcess.inputStream.bufferedReader().readText()
                }
                catch (e: IOException) {
                    throw Exception("Failed to read JBake process output: ${e.message}")
                }
                withClue("JBake process output:\n\n\n$jbakeOutput\n\n\n") {
                    bakeProcess.exitValue() shouldBe 0
                }

                val outputDir = tempDir.resolve("output") //.also { it.mkdir() }
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
