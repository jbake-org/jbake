package org.jbake

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import org.eclipse.jgit.api.Git
import java.io.File
import kotlin.io.path.createTempDirectory

class ProjectWebsiteTest : StringSpec({

    val WEBSITE_REPO_URL = "https://github.com/jbake-org/jbake.org.git"

    "should bake JBake website" {
        val tempDir = createTempDirectory("jbake-website-test-").toFile()
        try {
            val projectDir = tempDir.resolve("project")
            val outputDir = File(projectDir, "output")

            // Clone JBake website repository
            Git.cloneRepository()
                .setBare(false)
                .setBranch("master")
                .setRemote("origin")
                .setURI(WEBSITE_REPO_URL)
                .setDirectory(projectDir)
                .call()

            File(projectDir, "README.md").shouldExist()

            // Bake the website
            val runner = BinaryRunner(projectDir)
            val process = runner.runWithArguments(BinaryRunner.jbakeExecutableRelative.absolutePath, "-b")
            withClue("\n========= JBake process output: =========\n\n${runner.processOutput}\n\n====================================\n\n") {
                process.exitValue() shouldBe 0
            }
            File(outputDir, "index.html").shouldExist()
            process.destroy()
        }
        finally {
            tempDir.deleteRecursively()
        }
    }
})
