package org.jbake

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
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

                // Run JBake to initialize the dir (with verbose logging to debug failures)
                val initProcess = runner.runWithArguments(jbakeExec, "-vvv", "-i", "-t", projectName)
                withClue("JBake init run printed:\n\n${runner.processOutput}\n") {
                    initProcess.exitValue() shouldBe 0
                }
                tempDir.resolve("jbake.properties").shouldExist()
                tempDir.resolve("templates/index.$extension").shouldExist()
                initProcess.destroy()

                // Bake project (with verbose logging to debug failures)
                val bakeProcess = runner.runWithArguments(jbakeExec, "-vvv", "-b")
                withClue("JBake bake run printed:\n\n${runner.processOutput}\n") {
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
