package org.jbake.render

import org.assertj.core.api.Assertions
import org.jbake.TestUtils
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.template.DelegatingTemplateEngine
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class RendererTest {
    @Rule
    var folder: TemporaryFolder = TemporaryFolder()
    private var config: DefaultJBakeConfiguration? = null
    private var outputPath: File? = null

    @Mock
    private val db: ContentStore? = null

    @Mock
    private val renderingEngine: DelegatingTemplateEngine? = null

    @Before
    @Throws(Exception::class)
    fun setup() {
        val sourcePath = TestUtils.getTestResourcesAsSourceFolder()
        if (!sourcePath.exists()) {
            throw Exception("Cannot find base path for test!")
        }
        outputPath = folder.newFolder("output")
        config = ConfigUtil().loadConfig(sourcePath) as DefaultJBakeConfiguration
        config!!.setDestinationFolder(outputPath)
    }

    /**
     * See issue #300
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testRenderFileWorksWhenPathHasDotInButFileDoesNot() {
        Assume.assumeFalse("Ignore running on Windows", TestUtils.isWindows())
        val FOLDER = "real.path"

        val FILENAME = "about"
        config!!.setOutputExtension("")
        config!!.setTemplateFolder(folder.newFolder("templates"))
        val renderer = Renderer(db!!, config!!, renderingEngine!!)

        val content = DocumentModel()
        content.type = "page"
        content.uri = "/" + FOLDER + "/" + FILENAME
        content.status = "published"

        renderer.render(content)

        val outputFile =
            File(outputPath!!.getAbsolutePath() + File.separatorChar + FOLDER + File.separatorChar + FILENAME)
        Assertions.assertThat(outputFile).isFile()
    }
}
