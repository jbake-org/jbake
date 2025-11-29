package org.jbake

import org.apache.commons.lang3.SystemUtils
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.io.IOException

@RunWith(Parameterized::class)
class BuiltInProjectsTest {
    @Parameterized.Parameter
    var projectName: String? = null

    @Parameterized.Parameter(1)
    var extension: String? = null

    @Rule @JvmField
    var folder: TemporaryFolder = TemporaryFolder()
    private lateinit var projectFolder: File
    private lateinit var templateFolder: File
    private lateinit var outputFolder: File
    private lateinit var jbakeExecutable: String
    private lateinit var runner: BinaryRunner

    @Before
    @Throws(IOException::class)
    fun setup() {
        jbakeExecutable =
            if (SystemUtils.IS_OS_WINDOWS) File("build\\install\\jbake\\bin\\jbake.bat").absolutePath
            else File("build/install/jbake/bin/jbake").absolutePath
        projectFolder = folder.newFolder("project")
        templateFolder = File(projectFolder, "templates")
        outputFolder = File(projectFolder, "output")
        runner = BinaryRunner(projectFolder)
    }

    @Test
    @Throws(Exception::class)
    fun shouldBakeWithProject() {
        shouldInitProject(projectName, extension)
        shouldBakeProject()
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun shouldInitProject(projectName: String?, extension: String?) {
        val process = runner.runWithArguments(jbakeExecutable, "-i", "-t", projectName)
        Assertions.assertThat(process.exitValue()).isEqualTo(0)
        Assertions.assertThat(File(projectFolder, "jbake.properties")).exists()
        Assertions.assertThat(File(templateFolder, String.format("index.%s", extension))).exists()
        process.destroy()
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun shouldBakeProject() {
        val process = runner.runWithArguments(jbakeExecutable, "-b")
        Assertions.assertThat(process.exitValue()).isEqualTo(0)
        Assertions.assertThat(File(outputFolder, "index.html")).exists()
        process.destroy()
    }

    companion object {
        @Parameterized.Parameters(name = " {0} ")
        fun data(): Iterable<Array<Any>> {
            return listOf(
                arrayOf("thymeleaf", "thyme"),
                arrayOf("freemarker", "ftl"),
                arrayOf("jade", "jade"),
                arrayOf("groovy", "gsp"),
                arrayOf("groovy-mte", "tpl")
            )
        }
    }
}
