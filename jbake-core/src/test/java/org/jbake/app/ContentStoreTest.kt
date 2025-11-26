package org.jbake.app

import org.assertj.core.api.Assertions.assertThat
import org.jbake.addTestDocument
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentModel.Companion.createDefaultDocumentModel
import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.ModelAttributes
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.Boolean
import java.util.*
import kotlin.Long
import kotlin.String
import kotlin.arrayOf
import kotlin.repeat

class ContentStoreTest : ContentStoreIntegrationTest() {
    @Test fun shouldGetCountForPublishedDocuments() {
        repeat(5) {
            db.addTestDocument(type = DOC_TYPE_POST, status = "published")
        }

        db.addTestDocument(type = DOC_TYPE_POST, status = "draft")

        assertEquals(6, db.getDocumentCount(DOC_TYPE_POST))
        assertEquals(5, db.getPublishedCount(DOC_TYPE_POST))
    }

    @Test fun testStoreTypeWithSpecialCharacters() {
        val typeWithHyphen = "type-with-hyphen"

        addDocumentType(typeWithHyphen)

        val tagWithHyphenBackslashAndBacktick = "identifier-with\\`backtick"
        val uri = "test/testMergeDocument"

        val model = createDefaultDocumentModel()
        model.type = typeWithHyphen
        model.tags = arrayOf<String>(tagWithHyphenBackslashAndBacktick)
        model.date = Date()
        model.sourceUri = uri
        model["foo"] = "originalValue"

        db.addDocument(model)

        val documentList1: DocumentList<DocumentModel> =
            db.getAllContent(typeWithHyphen)

        assertEquals(1, documentList1.size.toLong())

        val documentList2: DocumentList<DocumentModel> =
            db.getAllContent(typeWithHyphen, true)

        assertEquals(1, documentList2.size.toLong())

        val documentList3: DocumentList<DocumentModel> = db.getDocumentByUri(uri)

        assertEquals(1, documentList3.size.toLong())

        val documentCount1: Long = db.getDocumentCount(typeWithHyphen)

        assertEquals(1L, documentCount1)

        val documentList4: DocumentList<DocumentModel> =
            db.getDocumentStatus(uri)

        assertEquals(1, documentList4.size.toLong())
        assertEquals(Boolean.FALSE, documentList4[0]!!.rendered)

        val documentCount2: Long = db.getPublishedCount(typeWithHyphen)
        assertEquals(0, documentCount2)

        val published = DocumentModel()
        published.sourceUri = "test/another-testdocument.adoc"
        published.tags = arrayOf<String>(tagWithHyphenBackslashAndBacktick)
        published.type = typeWithHyphen
        published.status = ModelAttributes.Status.PUBLISHED
        published.cached = true
        published.rendered = false

        db.addDocument(published)

        val documentList5: DocumentList<DocumentModel> = db.unrenderedContent
        assertEquals(2, documentList5.size.toLong())
        assertEquals(Boolean.FALSE, documentList5[0].rendered)
        assertEquals(typeWithHyphen, documentList5[0].type)
        assertThat<String>(documentList5[0].tags).contains(tagWithHyphenBackslashAndBacktick)

        val documentCount3: Long = db.getPublishedCount(typeWithHyphen)
        assertEquals(1, documentCount3)

        db.markContentAsRendered(published)

        val documentList6: DocumentList<DocumentModel> =
            db.getPublishedContent(typeWithHyphen)
        assertEquals(1, documentList6.size.toLong())
        assertEquals(Boolean.TRUE, documentList6[0].rendered)
        assertEquals(typeWithHyphen, documentList6[0].type)
        assertThat<String>(documentList6[0].tags).contains(tagWithHyphenBackslashAndBacktick)

        val documentList7: DocumentList<DocumentModel> =
            db.getPublishedDocumentsByTag(tagWithHyphenBackslashAndBacktick)
        assertEquals(1, documentList7.size.toLong())
        assertEquals(Boolean.TRUE, documentList7[0].rendered)
        assertEquals(typeWithHyphen, documentList7[0].type)
        assertThat<String>(documentList7[0].tags).contains(tagWithHyphenBackslashAndBacktick)

        val documentList8: DocumentList<DocumentModel> =
            db.getPublishedPostsByTag(tagWithHyphenBackslashAndBacktick)
        assertEquals(0, documentList8.size.toLong())

        val tags: MutableSet<String> = db.allTags
        assertEquals(mutableSetOf<String>(tagWithHyphenBackslashAndBacktick), tags)

        db.deleteContent(uri)

        val documentCount4: Long = db.getDocumentCount(typeWithHyphen)
        assertEquals(1, documentCount4)

        db.deleteAllByDocType(typeWithHyphen)
    }

    companion object {
        const val DOC_TYPE_POST: String = "post"
    }
}
