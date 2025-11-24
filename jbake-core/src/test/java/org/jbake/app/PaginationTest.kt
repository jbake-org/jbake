package org.jbake.app

import org.assertj.core.api.Assertions.assertThat
import org.jbake.FakeDocumentBuilder
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes.documentTypes
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class PaginationTest : ContentStoreIntegrationTest() {
    @Before
    fun setUpOwn() {
        for (docType in documentTypes) {
            var fileBaseName = docType
            if (docType == "masterindex") {
                fileBaseName = "index"
            }
            config.setTemplateFileNameForDocType(docType, "$fileBaseName.ftl")
        }

        config.setPaginateIndex(true)
        config.setPostsPerPage(1)
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
        db.setLimit(PER_PAGE)

        while (start < TOTAL_POSTS) {
            db.setStart(start)
            val posts: DocumentList<DocumentModel> = db.getPublishedPosts(true)

            assertThat(posts.size).isLessThanOrEqualTo(2)

            if (posts.size > 1) {
                val post = posts[0] as DocumentModel
                val nextPost = posts[1] as DocumentModel

                assertThat(post.date).isAfter(nextPost.date)
            }

            pageCount++
            start += PER_PAGE
        }
        Assert.assertEquals(4, pageCount.toLong())
    }
}
