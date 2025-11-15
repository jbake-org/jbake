package org.jbake.app

import org.assertj.core.api.Assertions
import org.jbake.FakeDocumentBuilder
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentModel.Companion.createDefaultDocumentModel
import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.ModelAttributes
import org.junit.Assert
import org.junit.Test
import java.lang.Boolean
import java.util.*
import kotlin.Exception
import kotlin.Long
import kotlin.String
import kotlin.Throws
import kotlin.arrayOf

class ContentStoreTest : ContentStoreIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun shouldGetCountForPublishedDocuments() {
        for (i in 0..4) {
            val builder = FakeDocumentBuilder(DOC_TYPE_POST)
            builder.withStatus("published")
                .withRandomSha1()
                .build()
        }

        val builder = FakeDocumentBuilder(DOC_TYPE_POST)
        builder.withStatus("draft")
            .withRandomSha1()
            .build()

        Assert.assertEquals(6, ContentStoreIntegrationTest.Companion.db.getDocumentCount(DOC_TYPE_POST))
        Assert.assertEquals(5, ContentStoreIntegrationTest.Companion.db.getPublishedCount(DOC_TYPE_POST))
    }

    @Test
    fun testStoreTypeWithSpecialCharacters() {
        val typeWithHyphen = "type-with-hyphen"

        addDocumentType(typeWithHyphen)

        val tagWithHyphenBackslashAndBacktick = "identifier-with\\`backtick"
        val uri = "test/testMergeDocument"

        val model = createDefaultDocumentModel()
        model.type = typeWithHyphen
        model.tags = arrayOf<String>(tagWithHyphenBackslashAndBacktick)
        model.date = Date()
        model.setSourceUri(uri)
        model.put("foo", "originalValue")

        ContentStoreIntegrationTest.Companion.db.addDocument(model)

        val documentList1: DocumentList<DocumentModel> =
            ContentStoreIntegrationTest.Companion.db.getAllContent(typeWithHyphen)

        Assert.assertEquals(1, documentList1.size.toLong())

        val documentList2: DocumentList<DocumentModel> =
            ContentStoreIntegrationTest.Companion.db.getAllContent(typeWithHyphen, true)

        Assert.assertEquals(1, documentList2.size.toLong())

        val documentList3: DocumentList<DocumentModel> = ContentStoreIntegrationTest.Companion.db.getDocumentByUri(uri)

        Assert.assertEquals(1, documentList3.size.toLong())

        val documentCount1: Long = ContentStoreIntegrationTest.Companion.db.getDocumentCount(typeWithHyphen)

        Assert.assertEquals(1L, documentCount1)

        val documentList4: DocumentList<DocumentModel> =
            ContentStoreIntegrationTest.Companion.db.getDocumentStatus(uri)

        Assert.assertEquals(1, documentList4.size.toLong())
        Assert.assertEquals(Boolean.FALSE, documentList4.get(0)!!.rendered)

        val documentCount2: Long = ContentStoreIntegrationTest.Companion.db.getPublishedCount(typeWithHyphen)
        Assert.assertEquals(0, documentCount2)

        val published = DocumentModel()
        published.setSourceUri("test/another-testdocument.adoc")
        published.tags = arrayOf<String>(tagWithHyphenBackslashAndBacktick)
        published.type = typeWithHyphen
        published.status = ModelAttributes.Status.PUBLISHED
        published.cached = true
        published.rendered = false

        ContentStoreIntegrationTest.Companion.db.addDocument(published)

        val documentList5: DocumentList<DocumentModel> = ContentStoreIntegrationTest.Companion.db.unrenderedContent
        Assert.assertEquals(2, documentList5.size.toLong())
        Assert.assertEquals(Boolean.FALSE, documentList5.get(0)!!.rendered)
        Assert.assertEquals(typeWithHyphen, documentList5.get(0)!!.type)
        Assertions.assertThat<String?>(documentList5.get(0)!!.tags).contains(tagWithHyphenBackslashAndBacktick)

        val documentCount3: Long = ContentStoreIntegrationTest.Companion.db.getPublishedCount(typeWithHyphen)
        Assert.assertEquals(1, documentCount3)

        ContentStoreIntegrationTest.Companion.db.markContentAsRendered(published)

        val documentList6: DocumentList<DocumentModel> =
            ContentStoreIntegrationTest.Companion.db.getPublishedContent(typeWithHyphen)
        Assert.assertEquals(1, documentList6.size.toLong())
        Assert.assertEquals(Boolean.TRUE, documentList6.get(0)!!.rendered)
        Assert.assertEquals(typeWithHyphen, documentList6.get(0)!!.type)
        Assertions.assertThat<String?>(documentList6.get(0)!!.tags).contains(tagWithHyphenBackslashAndBacktick)

        val documentList7: DocumentList<DocumentModel> =
            ContentStoreIntegrationTest.Companion.db.getPublishedDocumentsByTag(tagWithHyphenBackslashAndBacktick)
        Assert.assertEquals(1, documentList7.size.toLong())
        Assert.assertEquals(Boolean.TRUE, documentList7.get(0)!!.rendered)
        Assert.assertEquals(typeWithHyphen, documentList7.get(0)!!.type)
        Assertions.assertThat<String?>(documentList7.get(0)!!.tags).contains(tagWithHyphenBackslashAndBacktick)

        val documentList8: DocumentList<DocumentModel> =
            ContentStoreIntegrationTest.Companion.db.getPublishedPostsByTag(tagWithHyphenBackslashAndBacktick)
        Assert.assertEquals(0, documentList8.size.toLong())

        val tags: MutableSet<String?> = ContentStoreIntegrationTest.Companion.db.allTags
        Assert.assertEquals(mutableSetOf<String?>(tagWithHyphenBackslashAndBacktick), tags)

        ContentStoreIntegrationTest.Companion.db.deleteContent(uri)

        val documentCount4: Long = ContentStoreIntegrationTest.Companion.db.getDocumentCount(typeWithHyphen)
        Assert.assertEquals(1, documentCount4)

        ContentStoreIntegrationTest.Companion.db.deleteAllByDocType(typeWithHyphen)
    }

    companion object {
        const val DOC_TYPE_POST: String = "post"
    }
}
