package org.jbake.app.template

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.apache.commons.io.FileUtils
import java.nio.charset.Charset

class FreemarkerTemplateEngineRenderingTest : StringSpec({
    lateinit var helper: TemplateTestHelper

    beforeSpec {
        helper = TemplateTestHelper("freemarkerTemplates", "ftl")
        helper.setupClass()
    }

    beforeTest {
        helper.setupTest()
    }

    afterTest {
        helper.teardownTest()
    }

    afterSpec {
        helper.teardownClass()
    }

    "renderPaginatedIndex" {
        helper.config.setPaginateIndex(true)
        helper.config.setPostsPerPage(1)

        helper.expectedInOutput["index"] = mutableListOf(
            "\">Previous</a>",
            "3/\">Next</a>",
            "2 of 3"
        )

        helper.renderer.renderIndexPaging("index.html")

        val outputFile = helper.destinationDir.resolve("2").resolve("index.html")
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())

        for (string in helper.getExpectedInOutput("index")) {
            output shouldContain string
        }

        output shouldContain "Post Url: blog%2F2013%2Fsecond-post.html"
    }

    "shouldFallbackToRenderSingleIndexIfNoPostArePresent" {
        helper.config.setPaginateIndex(true)
        helper.config.setPostsPerPage(1)

        helper.db.deleteAllByDocType("post")

        helper.renderer.renderIndexPaging("index.html")

        val paginatedFile = helper.destinationDir.resolve("index2.html")
        paginatedFile.exists() shouldBe false

        val indexFile = helper.destinationDir.resolve("index.html")
        indexFile.exists() shouldBe true
    }
})
