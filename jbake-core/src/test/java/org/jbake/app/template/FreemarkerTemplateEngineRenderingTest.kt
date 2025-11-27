package org.jbake.app.template

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

class FreemarkerTemplateEngineRenderingTest : AbstractTemplateEngineRenderingTest("freemarkerTemplates", "ftl") {
    @Test fun renderPaginatedIndex() {
        config.setPaginateIndex(true)
        config.setPostsPerPage(1)

        expectedInOutput["index"] = mutableListOf(
            "\">Previous</a>",
            "3/\">Next</a>",
            "2 of 3"
        )

        renderer.renderIndexPaging("index.html")

        val outputFile = File(File(destinationFolder, "2"), "index.html")
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())

        for (string in getExpectedInOutput("index")) {
            assertThat(output).contains(string)
        }

        assertThat(output).contains("Post Url: blog%2F2013%2Fsecond-post.html")
    }

    @Test fun shouldFallbackToRenderSingleIndexIfNoPostArePresent() {
        config.setPaginateIndex(true)
        config.setPostsPerPage(1)

        db.deleteAllByDocType("post")

        renderer.renderIndexPaging("index.html")

        val paginatedFile = File(destinationFolder, "index2.html")
        assertFalse("paginated file is not rendered", paginatedFile.exists())

        val indexFile = File(destinationFolder, "index.html")
        assertTrue("index file exists", indexFile.exists())
    }
}
