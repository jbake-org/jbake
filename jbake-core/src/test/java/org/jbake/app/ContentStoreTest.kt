package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.jbake.addTestDocument
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentModel.Companion.createDefaultDocumentModel
import org.jbake.model.DocumentTypeRegistry.addDocumentType
import org.jbake.model.ModelAttributes
import java.time.LocalDate

class ContentStoreTestHsqldb : ContentStoreTestBase(DatabaseType.HSQLDB)
class ContentStoreTestNeo4j : ContentStoreTestBase(DatabaseType.NEO4J)
class ContentStoreTestOrientdb : ContentStoreTestBase(DatabaseType.ORIENTDB)

abstract class ContentStoreTestBase(dbType: DatabaseType) : StringSpec({

    lateinit var db: ContentStore

    beforeSpec {
        ContentStoreIntegrationTest.setUpClass(dbType)
        db = ContentStoreIntegrationTest.db
    }

    beforeTest {
        db.startup()
    }

    afterTest {
        db.drop()
    }

    afterSpec {
        ContentStoreIntegrationTest.cleanUpClass()
    }

    "shouldGetCountForPublishedDocuments" {
        repeat(5) {
            db.addTestDocument(type = DOC_TYPE_POST, status = "published")
        }

        db.addTestDocument(type = DOC_TYPE_POST, status = "draft")

        db.getDocumentCount(DOC_TYPE_POST) shouldBe 6
        db.getPublishedCount(DOC_TYPE_POST) shouldBe 5
    }

    "testStoreTypeWithSpecialCharacters" {
            val typeWithHyphen = "type-with-hyphen"

            addDocumentType(typeWithHyphen)

            val tagWithHyphenBackslashAndBacktick = "identifier-with\\`backtick"
            val uri = "test/testMergeDocument"

            val model = createDefaultDocumentModel()
            model.type = typeWithHyphen
            model.tags = listOf(tagWithHyphenBackslashAndBacktick)
            model.date = LocalDate.now()
            model.sourceUri = uri
            model["foo"] = "originalValue"

            db.addDocument(model)

            val documentList1: DocumentList<DocumentModel> =
                db.getAllContent(typeWithHyphen)

            documentList1.size.toLong() shouldBe 1

            val documentList2: DocumentList<DocumentModel> =
                db.getAllContent(typeWithHyphen, true)

            documentList2.size.toLong() shouldBe 1

            val documentList3: DocumentList<DocumentModel> = db.getDocumentByUri(uri)

            documentList3.size.toLong() shouldBe 1

            val documentCount1: Long = db.getDocumentCount(typeWithHyphen)

            documentCount1 shouldBe 1L

            val documentList4: DocumentList<DocumentModel> =
                db.getDocumentStatus(uri)

            documentList4.size.toLong() shouldBe 1
            documentList4[0].rendered shouldBe false

            val documentCount2: Long = db.getPublishedCount(typeWithHyphen)
            documentCount2 shouldBe 0

            val published = DocumentModel()
            published.sourceUri = "test/another-testdocument.adoc"
            published.tags = listOf(tagWithHyphenBackslashAndBacktick)
            published.type = typeWithHyphen
            published.status = ModelAttributes.Status.PUBLISHED
            published.cached = true
            published.rendered = false

            db.addDocument(published)

            val documentList5: DocumentList<DocumentModel> = db.unrenderedContent
            documentList5.size.toLong() shouldBe 2
            documentList5[0].rendered shouldBe false
            documentList5[0].type shouldBe typeWithHyphen
            documentList5[0].tags shouldContain tagWithHyphenBackslashAndBacktick

            val documentCount3: Long = db.getPublishedCount(typeWithHyphen)
            documentCount3 shouldBe 1

            db.markContentAsRendered(published)

            val documentList6: DocumentList<DocumentModel> =
                db.getPublishedContent(typeWithHyphen)
            documentList6.size.toLong() shouldBe 1
            documentList6[0].rendered shouldBe true
            documentList6[0].type shouldBe typeWithHyphen
            documentList6[0].tags shouldContain tagWithHyphenBackslashAndBacktick

            val documentList7: DocumentList<DocumentModel> =
                db.getPublishedDocumentsByTag(tagWithHyphenBackslashAndBacktick)
            documentList7.size.toLong() shouldBe 1
            documentList7[0].rendered shouldBe true
            documentList7[0].type shouldBe typeWithHyphen
            documentList7[0].tags shouldContain tagWithHyphenBackslashAndBacktick

            val documentList8: DocumentList<DocumentModel> =
                db.getPublishedPostsByTag(tagWithHyphenBackslashAndBacktick)
            documentList8.size.toLong() shouldBe 0

            val tags: MutableSet<String> = db.allTags
            tags shouldBe mutableSetOf(tagWithHyphenBackslashAndBacktick)

            db.deleteContent(uri)

            val documentCount4: Long = db.getDocumentCount(typeWithHyphen)
            documentCount4 shouldBe 1

            db.deleteAllByDocType(typeWithHyphen)
    }
}) {
    companion object {
        const val DOC_TYPE_POST: String = "post"
    }
}
