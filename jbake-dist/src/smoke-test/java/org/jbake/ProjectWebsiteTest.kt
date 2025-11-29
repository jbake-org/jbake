package org.jbake

import org.apache.commons.lang3.SystemUtils
import org.assertj.core.api.Assertions
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class ProjectWebsiteTest {

    @Rule @JvmField
    var tempDir: TemporaryFolder = TemporaryFolder()
    private var projectDir = tempDir.newFolder("project")
    private var outputDir = File(projectDir, "output")
    private val jbakeExecutable: String =
        (if (SystemUtils.IS_OS_WINDOWS) "build\\install\\jbake\\bin\\jbake.bat"
        else "build/install/jbake/bin/jbake")
            .let { File(it).absolutePath }
    private var runner = BinaryRunner(projectDir)

    @Before
    @Throws(IOException::class, GitAPIException::class)
    fun setup() {
        cloneJbakeWebsite()
    }

    @Throws(GitAPIException::class)
    private fun cloneJbakeWebsite() {
        val cmd = Git.cloneRepository()
        cmd.setBare(false)
        cmd.setBranch("master")
        cmd.setRemote("origin")
        cmd.setURI(WEBSITE_REPO_URL)
        cmd.setDirectory(projectDir)
        cmd.call()

        Assertions.assertThat(File(projectDir, "README.md").exists()).isTrue()
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun shouldBakeWebsite() {
        val process = runner.runWithArguments(jbakeExecutable, "-b")
        Assertions.assertThat(process.exitValue()).isEqualTo(0)
        Assertions.assertThat(File(outputDir, "index.html")).exists()
        process.destroy()
    }

    companion object {
        private const val WEBSITE_REPO_URL = "https://github.com/jbake-org/jbake.org.git"
    }
}
