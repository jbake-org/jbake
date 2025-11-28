package org.jbake.render

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldBeAFile
import io.mockk.mockk
import org.jbake.TestUtils
import org.jbake.app.ContentStore
import org.jbake.app.Renderer
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.template.DelegatingTemplateEngine
import java.io.File
import java.nio.file.Files

class RendererTest : StringSpec({
    lateinit var folder: File
    lateinit var config: DefaultJBakeConfiguration
    lateinit var outputPath: File
    lateinit var db: ContentStore
    lateinit var renderingEngine: DelegatingTemplateEngine

    beforeTest {
        folder = Files.createTempDirectory("jbake-test").toFile()
        db = mockk(relaxed = true)
        renderingEngine = mockk(relaxed = true)

        val sourcePath = TestUtils.testResourcesAsSourceFolder
        if (!sourcePath.exists())
            throw Exception("Cannot find base path for test!")
        outputPath = File(folder, "output").apply { mkdirs() }
        config = ConfigUtil().loadConfig(sourcePath) as DefaultJBakeConfiguration
        config.destinationFolder = outputPath
    }

    afterTest {
        folder.deleteRecursively()
    }

    "testRenderFileWorksWhenPathHasDotInButFileDoesNot".config(enabled = !TestUtils.isWindows) {
        val FOLDER = "real.path"
        val FILENAME = "about"

        config.setOutputExtension("")
        config.templateFolder = File(folder, "templates").apply { mkdirs() }
        val renderer = Renderer(db, config, renderingEngine)

        val content = DocumentModel()
        content.type = "page"
        content.uri = "$FOLDER/$FILENAME"
        content.status = "published"

        renderer.render(content)

        val outputFile = outputPath.resolve(FOLDER).resolve(FILENAME)
        outputFile.shouldBeAFile()
    }
})
