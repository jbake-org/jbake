package org.jbake

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import java.io.IOException
import kotlin.io.path.createTempDirectory

data class TemplateTestCase(val name: String, val extension: String) {
    override fun toString() = "$name template"
}

class BuiltInProjectsTest : FunSpec({

    context("should bake successfully") {
        withData(
            TemplateTestCase("freemarker", "ftl"),
            TemplateTestCase("thymeleaf", "thyme"),
            TemplateTestCase("jade", "jade"),
            TemplateTestCase("groovy", "gsp"),
            TemplateTestCase("groovy-mte", "tpl"),
        ) { (projectName, extension) ->
            val jbakeExec = BinaryRunner.jbakeExecutableRelative.absolutePath
            val tempDir = createTempDirectory(buildOutputDir, "jbake-smoke-test-").toFile()
            try {
                val runner = BinaryRunner(tempDir)

                // Run JBake to initialize the dir
                val initProcess = runner.runWithArguments(jbakeExec, "-i", "-t", projectName)
                initProcess.exitValue() shouldBe 0
                tempDir.resolve("jbake.properties").shouldExist()
                tempDir.resolve("templates/index.$extension").shouldExist()
                initProcess.destroy()

                // Bake project
                val bakeProcess = runner.runWithArguments(jbakeExec, "-b")
                val jbakeOutput = try {
                    bakeProcess.inputStream.bufferedReader().readText()
                } catch (e: IOException) {
                    throw Exception("Failed to read JBake process output: ${e.message}")
                }
                withClue("JBake process output:\n\n$jbakeOutput\n\n") {
                    bakeProcess.exitValue() shouldBe 0
                }
                tempDir.resolve("output/index.html").shouldExist()
                bakeProcess.destroy()
            } finally {
                tempDir.deleteRecursively()
            }
        }
    }
})
