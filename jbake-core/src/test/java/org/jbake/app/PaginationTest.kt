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
package org.jbake.app

import org.assertj.core.api.Assertions
import org.jbake.FakeDocumentBuilder
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes.documentTypes
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * @author jdlee
 */
class PaginationTest : ContentStoreIntegrationTest() {
    @Before
    fun setUpOwn() {
        for (docType in documentTypes) {
            var fileBaseName = docType
            if (docType == "masterindex") {
                fileBaseName = "index"
            }
            ContentStoreIntegrationTest.Companion.config.setTemplateFileNameForDocType(docType, fileBaseName + ".ftl")
        }

        ContentStoreIntegrationTest.Companion.config.setPaginateIndex(true)
        ContentStoreIntegrationTest.Companion.config.setPostsPerPage(1)
    }

    @Test
    fun testPagination() {
        val TOTAL_POSTS = 5
        val PER_PAGE = 2
        val cal = Calendar.getInstance(Locale.ENGLISH)
        for (i in 1..TOTAL_POSTS) {
            cal.add(Calendar.SECOND, 5)
            val builder = FakeDocumentBuilder("post")
            builder.withCached(true)
                .withStatus("published")
                .withDate(cal.getTime())
                .build()
        }

        var pageCount = 1
        var start = 0
        ContentStoreIntegrationTest.Companion.db.setLimit(PER_PAGE)

        while (start < TOTAL_POSTS) {
            ContentStoreIntegrationTest.Companion.db.setStart(start)
            val posts: DocumentList = ContentStoreIntegrationTest.Companion.db.getPublishedPosts(true)

            Assertions.assertThat(posts.size).isLessThanOrEqualTo(2)

            if (posts.size > 1) {
                val post = posts.get(0) as DocumentModel
                val nextPost = posts.get(1) as DocumentModel

                Assertions.assertThat(post.date).isAfter(nextPost.date)
            }

            pageCount++
            start += PER_PAGE
        }
        Assert.assertEquals(4, pageCount.toLong())
    }
}
