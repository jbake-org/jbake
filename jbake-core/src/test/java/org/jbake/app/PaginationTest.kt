package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jbake.addTestDocument
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypeRegistry.documentTypes
import java.util.*

class PaginationTest : StringSpec({

    lateinit var db: ContentStore
    lateinit var config: org.jbake.app.configuration.DefaultJBakeConfiguration

    beforeSpec {
        ContentStoreIntegrationTest.setUpClass()
        db = ContentStoreIntegrationTest.db
        config = ContentStoreIntegrationTest.config
    }

    beforeTest {
        db.startup()

        for (docType in documentTypes) {
            val fileBaseName = if (docType == "masterindex") "index" else docType
            config.setTemplateFileNameForDocType(docType, "$fileBaseName.ftl")
        }
        config.setPaginateIndex(true)
        config.setPostsPerPage(1)
    }

    afterTest {
        db.drop()
    }

    afterSpec {
        ContentStoreIntegrationTest.cleanUpClass()
    }

    "testPagination" {
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

            posts.size shouldBeLessThanOrEqual 2

            if (posts.size > 1) {
                val date0 = posts[0].date
                val date1 = posts[1].date
                date0.shouldNotBeNull()
                date1.shouldNotBeNull()
                date0 shouldBeGreaterThan date1
            }

            pageCount++
            paginationOffset += 2
        }
        pageCount.toLong() shouldBe 4
    }
})

