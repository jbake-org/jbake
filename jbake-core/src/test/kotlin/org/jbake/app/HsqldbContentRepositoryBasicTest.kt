package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.jbake.model.DocumentModel
import java.time.OffsetDateTime

class HsqldbContentRepositoryBasicTest : StringSpec({

    "basic insert and count should work" {
        val dbPath = "/tmp/jbake-test-${System.currentTimeMillis()}"
        val repo = HsqldbContentRepository("local", dbPath)
        repo.startup()

        val doc = DocumentModel().apply {
            sourceUri = "test.html"
            type = "post"
            status = "published"
            title = "Test"
            date = OffsetDateTime.now()
            rendered = false
        }

        repo.addDocument(doc)

        val count = repo.getDocumentCount("post")
        count shouldBe 1

        repo.close()
    }

    "multiple startup calls should work" {
        val repo = HsqldbContentRepository("memory", "test-multi-${System.currentTimeMillis()}")
        repo.startup()

        val doc1 = DocumentModel().apply {
            sourceUri = "test1.html"
            type = "post"
            status = "published"
            title = "Test 1"
            date = OffsetDateTime.now()
            rendered = false
        }

        repo.addDocument(doc1)
        repo.getDocumentCount("post") shouldBe 1

        // Call startup again (simulating what tests do)
        repo.startup()

        // Data should still be there
        repo.getDocumentCount("post") shouldBe 1

        // Drop and verify empty
        repo.drop()
        repo.getDocumentCount("post") shouldBe 0

        // Add new document
        val doc2 = DocumentModel().apply {
            sourceUri = "test2.html"
            type = "post"
            status = "published"
            title = "Test 2"
            date = OffsetDateTime.now()
            rendered = false
        }

        repo.addDocument(doc2)
        repo.getDocumentCount("post") shouldBe 1

        repo.close()
    }
})

