package org.jbake

import org.apache.commons.vfs2.util.Os
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.io.IOException
import java.util.*

@RunWith(Parameterized::class)
class BuiltInProjectsTest {
    @Parameterized.Parameter
    var projectName: String? = null

    @Parameterized.Parameter(1)
    var extension: String? = null

    @Rule
    var folder: TemporaryFolder = TemporaryFolder()
    private var projectFolder: File? = null
    private var templateFolder: File? = null
    private var outputFolder: File? = null
    private var jbakeExecutable: String? = null
    private var runner: BinaryRunner? = null

    @Before
    @kotlin.Throws(IOException::class)
    fun setup() {
        if (Os.isFamily(Os.OS_FAMILY_WINDOWS)) {
            jbakeExecutable = File("build\\install\\jbake\\bin\\jbake.bat").getAbsolutePath()
        } else {
            jbakeExecutable = File("build/install/jbake/bin/jbake").getAbsolutePath()
        }
        projectFolder = folder.newFolder("project")
        templateFolder = File(projectFolder, "templates")
        outputFolder = File(projectFolder, "output")
        runner = BinaryRunner(projectFolder)
    }

    @Test
    @kotlin.Throws(Exception::class)
    fun shouldBakeWithProject() {
        shouldInitProject(projectName, extension)
        shouldBakeProject()
    }

    @kotlin.Throws(IOException::class, InterruptedException::class)
    private fun shouldInitProject(projectName: String?, extension: String?) {
        val process = runner!!.runWithArguments(jbakeExecutable, "-i", "-t", projectName)
        Assertions.assertThat(process.exitValue()).isEqualTo(0)
        Assertions.assertThat(File(projectFolder, "jbake.properties")).exists()
        Assertions.assertThat(File(templateFolder, String.format("index.%s", extension))).exists()
        process.destroy()
    }

    @kotlin.Throws(IOException::class, InterruptedException::class)
    private fun shouldBakeProject() {
        val process = runner!!.runWithArguments(jbakeExecutable, "-b")
        Assertions.assertThat(process.exitValue()).isEqualTo(0)
        Assertions.assertThat(File(outputFolder, "index.html")).exists()
        process.destroy()
    }

    companion object {
        @Parameterized.Parameters(name = " {0} ")
        fun data(): Iterable<Array<Any?>?> {
            return Arrays.asList<Array<Any?>?>(
                *arrayOf<Array<Any?>?>(
                    arrayOf<Any?>("thymeleaf", "thyme"),
                    arrayOf<Any?>("freemarker", "ftl"),
                    arrayOf<Any?>("jade", "jade"),
                    arrayOf<Any?>("groovy", "gsp"),
                    arrayOf<Any?>("groovy-mte", "tpl")
                )
            )
        }
    }
}
