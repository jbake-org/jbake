package org.jbake.render

import org.assertj.core.api.Assertions.assertThat
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

    @Rule @JvmField
    var folder: TemporaryFolder = TemporaryFolder()
    private lateinit var config: DefaultJBakeConfiguration
    private lateinit var outputPath: File

    @Mock private lateinit var db: ContentStore

    @Mock private lateinit var renderingEngine: DelegatingTemplateEngine

    @Before
    fun setup() {
        val sourcePath = TestUtils.testResourcesAsSourceFolder
        if (!sourcePath.exists())
            throw Exception("Cannot find base path for test!")
        outputPath = folder.newFolder("output")
        config = ConfigUtil().loadConfig(sourcePath) as DefaultJBakeConfiguration
        config.destinationFolder = (outputPath)
    }

    /**
     * See issue #300
     *
     * @throws Exception
     */
    @Test
    fun testRenderFileWorksWhenPathHasDotInButFileDoesNot() {
        Assume.assumeFalse("Ignore running on Windows", TestUtils.isWindows)
        val FOLDER = "real.path"

        val FILENAME = "about"
        config.setOutputExtension("")
        config.templateFolder = (folder.newFolder("templates"))
        val renderer = Renderer(db, config, renderingEngine)

        val content = DocumentModel()
        content.type = "page"
        content.uri = "$FOLDER/$FILENAME"
        content.status = "published"

        renderer.render(content)

        val outputFile = outputPath.resolve(FOLDER).resolve(FILENAME)
        assertThat(outputFile).isFile()
    }
}
