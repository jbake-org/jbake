/*
 * The MIT License
 *
 * Copyright 2015 jdlee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jbake.app.template

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions
import org.jbake.app.ContentStoreIntegrationTest
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

/**
 * @author jdlee
 */
class FreemarkerTemplateEngineRenderingTest : AbstractTemplateEngineRenderingTest("freemarkerTemplates", "ftl") {
    @Test
    @Throws(Exception::class)
    fun renderPaginatedIndex() {
        ContentStoreIntegrationTest.Companion.config.setPaginateIndex(true)
        ContentStoreIntegrationTest.Companion.config.setPostsPerPage(1)

        outputStrings.put(
            "index", mutableListOf<String>(
                "\">Previous</a>",
                "3/\">Next</a>",
                "2 of 3"
            )
        )

        renderer.renderIndexPaging("index.html")

        val outputFile = File(destinationFolder, 2.toString() + File.separator + "index.html")
        val output = FileUtils.readFileToString(outputFile, Charset.defaultCharset())

        for (string in getOutputStrings("index")) {
            Assertions.assertThat(output).contains(string)
        }

        Assertions.assertThat(output).contains("Post Url: blog%2F2013%2Fsecond-post.html")
    }

    @Test
    @Throws(Exception::class)
    fun shouldFallbackToRenderSingleIndexIfNoPostArePresent() {
        ContentStoreIntegrationTest.Companion.config.setPaginateIndex(true)
        ContentStoreIntegrationTest.Companion.config.setPostsPerPage(1)

        ContentStoreIntegrationTest.Companion.db.deleteAllByDocType("post")

        renderer.renderIndexPaging("index.html")

        val paginatedFile = File(destinationFolder, "index2.html")
        Assert.assertFalse("paginated file is not rendered", paginatedFile.exists())

        val indexFile = File(destinationFolder, "index.html")
        Assert.assertTrue("index file exists", indexFile.exists())
    }
}
