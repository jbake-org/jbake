package org.jbake.app

import org.assertj.core.api.Assertions.assertThat
import org.jbake.addTestDocument
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
            val fileBaseName = if (docType == "masterindex") "index" else docType
            config.setTemplateFileNameForDocType(docType, "$fileBaseName.ftl")
        }
        config.setPaginateIndex(true)
        config.setPostsPerPage(1)
    }

    @Test
    fun testPagination() {
        val TOTAL_POSTS = 5
        val cal = Calendar.getInstance(Locale.ENGLISH)

        // Create posts with incrementing dates
        repeat(TOTAL_POSTS) {
            cal.add(Calendar.SECOND, 5)
            db.addTestDocument(type = "post", status = "published", cached = true, date = cal.time)
        }

        var pageCount = 1
        var paginationOffset = 0
        db.paginationLimit = 2

        while (paginationOffset < TOTAL_POSTS) {
            db.paginationOffset = paginationOffset
            val posts: DocumentList<DocumentModel> = db.getPublishedPosts(true)

            assertThat(posts.size).isLessThanOrEqualTo(2)

            if (posts.size > 1)
                assertThat(posts[0].date).isAfter(posts[1].date)

            pageCount++
            paginationOffset += 2
        }
        Assert.assertEquals(4, pageCount.toLong())
    }
}
