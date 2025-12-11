package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.jbake.addTestDocument
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentModel.Companion.createDefaultDocumentModel
import org.jbake.model.DocumentTypeRegistry.addDocumentType
import org.jbake.model.ModelAttributes
import org.jbake.util.sec
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class ContentStoreTestHsqldb : ContentStoreTestBase(DatabaseType.HSQLDB)
class ContentStoreTestNeo4j : ContentStoreTestBase(DatabaseType.NEO4J)
class ContentStoreTestOrientdb : ContentStoreTestBase(DatabaseType.ORIENTDB)

abstract class ContentStoreTestBase(dbType: DatabaseType) : StringSpec({

    lateinit var db: ContentStore

    // Shared test data
    val typeWithHyphen = "type-with-hyphen"
    val tagWithWeirdChars = "identifier-with\\`backtick"
    val testUri = "test/testMergeDocument"

    beforeSpec {
        ContentStoreIntegrationTest.setUpClass(dbType)
        db = ContentStoreIntegrationTest.db
    }

    beforeTest {
        db.startup()
        addDocumentType(typeWithHyphen)
    }

    afterTest {
        db.drop()
    }

    afterSpec {
        ContentStoreIntegrationTest.cleanUpClass()
    }

    "shouldGetCountForPublishedDocuments" {
        repeat(5) {
            db.addTestDocument(type = DOC_TYPE_POST, status = "published", date = OffsetDateTime.now().sec())
        }
        db.addTestDocument(type = DOC_TYPE_POST, status = "draft", date = OffsetDateTime.now().sec())

        db.getDocumentCount(DOC_TYPE_POST) shouldBe 6
        db.getPublishedCount(DOC_TYPE_POST) shouldBe 5
    }

    "shouldStoreAndRetrieveDocumentWithSpecialCharacters" {
        val model = createDefaultDocumentModel()
        model.type = typeWithHyphen
        model.tags = listOf(tagWithWeirdChars)
        model.date = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        model.sourceUri = testUri
        model["foo"] = "originalValue"

        db.addDocument(model)

        db.getAllContent(typeWithHyphen).size shouldBe 1
        db.getAllContent(typeWithHyphen, true).size shouldBe 1
        db.getDocumentByUri(testUri).size shouldBe 1
        db.getDocumentCount(typeWithHyphen) shouldBe 1L
    }

    "shouldGetDocumentStatus" {
        val model = createDefaultDocumentModel()
        model.type = typeWithHyphen
        model.tags = listOf(tagWithWeirdChars)
        model.date = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        model.sourceUri = testUri

        db.addDocument(model)

        val statusList = db.getDocumentStatus(testUri)
        statusList.size shouldBe 1
        statusList[0].rendered shouldBe false
    }

    "shouldCountPublishedDocuments" {
        val draft = createDefaultDocumentModel()
        draft.type = typeWithHyphen
        draft.sourceUri = testUri
        draft.date = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        db.addDocument(draft)

        db.getPublishedCount(typeWithHyphen) shouldBe 0

        val published = DocumentModel()
        published.sourceUri = "test/published.adoc"
        published.type = typeWithHyphen
        published.status = ModelAttributes.Status.PUBLISHED
        db.addDocument(published)

        db.getPublishedCount(typeWithHyphen) shouldBe 1
    }

    "shouldGetUnrenderedContent" {
        val doc1 = createDefaultDocumentModel()
        doc1.type = typeWithHyphen
        doc1.sourceUri = testUri
        doc1.tags = listOf(tagWithWeirdChars)
        doc1.date = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        db.addDocument(doc1)

        val doc2 = DocumentModel()
        doc2.sourceUri = "test/another.adoc"
        doc2.tags = listOf(tagWithWeirdChars)
        doc2.type = typeWithHyphen
        doc2.status = ModelAttributes.Status.PUBLISHED
        doc2.cached = true
        doc2.rendered = false
        db.addDocument(doc2)

        val unrendered = db.unrenderedContent
        unrendered.size shouldBe 2
        unrendered[0].rendered shouldBe false
        unrendered[0].type shouldBe typeWithHyphen
        unrendered[0].tags shouldContain tagWithWeirdChars
    }

    "shouldMarkContentAsRendered" {
        val published = DocumentModel()
        published.sourceUri = testUri
        published.tags = listOf(tagWithWeirdChars)
        published.type = typeWithHyphen
        published.status = ModelAttributes.Status.PUBLISHED
        published.cached = true
        published.rendered = false
        db.addDocument(published)

        db.markContentAsRendered(published)

        val content = db.getPublishedContent(typeWithHyphen)
        content.size shouldBe 1
        content[0].rendered shouldBe true
        content[0].type shouldBe typeWithHyphen
        content[0].tags shouldContain tagWithWeirdChars
    }

    "shouldGetPublishedDocumentsByTag" {
        val published = DocumentModel()
        published.sourceUri = testUri
        published.tags = listOf(tagWithWeirdChars)
        published.type = typeWithHyphen
        published.status = ModelAttributes.Status.PUBLISHED
        published.rendered = true
        db.addDocument(published)

        val byTag = db.getPublishedDocumentsByTag(tagWithWeirdChars)
        byTag.size shouldBe 1
        byTag[0].rendered shouldBe true
        byTag[0].type shouldBe typeWithHyphen
        byTag[0].tags shouldContain tagWithWeirdChars

        // Posts only - should be empty since type is not "post"
        db.getPublishedPostsByTag(tagWithWeirdChars).size shouldBe 0
    }

    "shouldGetAllTags" {
        val doc = DocumentModel()
        doc.sourceUri = testUri
        doc.tags = listOf(tagWithWeirdChars)
        doc.type = typeWithHyphen
        doc.status = ModelAttributes.Status.PUBLISHED
        db.addDocument(doc)

        db.allTags shouldBe mutableSetOf(tagWithWeirdChars)
    }

    "shouldDeleteContent" {
        val doc1 = DocumentModel()
        doc1.sourceUri = testUri
        doc1.type = typeWithHyphen
        db.addDocument(doc1)

        val doc2 = DocumentModel()
        doc2.sourceUri = "test/another.adoc"
        doc2.type = typeWithHyphen
        db.addDocument(doc2)

        db.getDocumentCount(typeWithHyphen) shouldBe 2

        db.deleteContent(testUri)
        db.getDocumentCount(typeWithHyphen) shouldBe 1

        db.deleteAllByDocType(typeWithHyphen)
        db.getDocumentCount(typeWithHyphen) shouldBe 0
    }
}) {
    companion object {
        const val DOC_TYPE_POST: String = "post"
    }
}
