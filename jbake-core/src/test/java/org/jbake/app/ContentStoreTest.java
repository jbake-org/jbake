package org.jbake.app;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jbake.FakeDocumentBuilder;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes.Status;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbake.app.ContentStore.quoteIdentifier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ContentStoreTest extends ContentStoreIntegrationTest {

    public static final String DOC_TYPE_POST = "post";

    @Test
    public void shouldGetCountForPublishedDocuments() throws Exception {

        for (int i = 0; i < 5; i++) {
            FakeDocumentBuilder builder = new FakeDocumentBuilder(DOC_TYPE_POST);
            builder.withStatus("published")
                    .withRandomSha1()
                    .build();
        }

        FakeDocumentBuilder builder = new FakeDocumentBuilder(DOC_TYPE_POST);
        builder.withStatus("draft")
                .withRandomSha1()
                .build();

        assertEquals(6, db.getDocumentCount(DOC_TYPE_POST));
        assertEquals(5, db.getPublishedCount(DOC_TYPE_POST));
    }

    @Test
    public void testMergeDocument() {
        final String uri = "test/testMergeDocument";

        ODocument doc = new ODocument(DOC_TYPE_POST);
        DocumentModel values = new DocumentModel();
        values.setType(DOC_TYPE_POST);
        values.setSourceUri(uri);
        values.put("foo", "originalValue");
        doc.fromMap(values);
        doc.save();

        // 1st
        values.put("foo", "newValue");
        db.mergeDocument(values);

        DocumentList<DocumentModel> docs = db.getDocumentByUri(DOC_TYPE_POST, uri);
        assertEquals(1, docs.size());
        assertEquals("newValue", docs.get(0).get("foo"));

        // 2nd
        values.put("foo", "anotherValue");
        db.mergeDocument(values);

        docs = db.getDocumentByUri(DOC_TYPE_POST, uri);
        assertEquals(1, docs.size());
        assertEquals("anotherValue", docs.get(0).get("foo"));

        db.deleteContent(DOC_TYPE_POST, uri);
        docs = db.getDocumentByUri(DOC_TYPE_POST, uri);
        assertEquals(0, docs.size());
    }

    @Test
    public void testStoreTypeWithSpecialCharacters() {
        final String typeWithHyphen = "type-with-hyphen";

        DocumentTypes.addDocumentType(typeWithHyphen);

        final String tagWithHyphen = "tag-with-hyphen";
        final String uri = "test/testMergeDocument";

        DocumentModel model = new DocumentModel();
        model.setType(typeWithHyphen);
        model.setTags(new String[]{tagWithHyphen});
        model.setStatus(Status.DRAFT);
        model.setDate(new Date());
        model.setRendered(false);
        model.setSourceUri(uri);
        model.setCached(true);
        model.put("foo", "originalValue");

        ODocument doc = new ODocument(typeWithHyphen);
        doc.fromMap(model);
        doc.save();

        DocumentList<DocumentModel> documentList1 = db.getAllContent(typeWithHyphen);

        assertEquals(1, documentList1.size());

        DocumentList<DocumentModel> documentList2 = db.getAllContent(typeWithHyphen, true);

        assertEquals(1, documentList2.size());

        DocumentList<DocumentModel> documentList3 =  db.getDocumentByUri(typeWithHyphen, uri);

        assertEquals(1, documentList3.size());

        long documentCount1 = db.getDocumentCount(typeWithHyphen);

        assertEquals(1L, documentCount1);

        DocumentList<DocumentModel> documentList4 = db.getDocumentStatus(typeWithHyphen, uri);

        assertEquals(1, documentList4.size());
        assertEquals(Boolean.FALSE, documentList4.get(0).getRendered());

        long documentCount2 = db.getPublishedCount(typeWithHyphen);
        assertEquals(0, documentCount2);

        DocumentModel published = new DocumentModel();
        published.setSourceUri(uri);
        published.setType(typeWithHyphen);
        published.setStatus(Status.PUBLISHED);
        db.mergeDocument(published);

        DocumentList<DocumentModel> documentList5 = db.getUnrenderedContent(typeWithHyphen);
        assertEquals(1, documentList5.size());
        assertEquals(Boolean.FALSE, documentList5.get(0).getRendered());
        assertEquals(typeWithHyphen, documentList5.get(0).getType());
        assertThat(documentList5.get(0).getTags()).contains(tagWithHyphen);

        long documentCount3 = db.getPublishedCount(typeWithHyphen);
        assertEquals(1, documentCount3);

        db.markContentAsRendered(typeWithHyphen);

        DocumentList<DocumentModel> documentList6 = db.getPublishedContent(typeWithHyphen);
        assertEquals(1, documentList6.size());
        assertEquals(Boolean.TRUE, documentList6.get(0).getRendered());
        assertEquals(typeWithHyphen, documentList6.get(0).getType());
        assertThat(documentList6.get(0).getTags()).contains(tagWithHyphen);

        DocumentList<DocumentModel> documentList7 = db.getPublishedDocumentsByTag(tagWithHyphen);
        assertEquals(1, documentList7.size());
        assertEquals(Boolean.TRUE, documentList7.get(0).getRendered());
        assertEquals(typeWithHyphen, documentList7.get(0).getType());
        assertThat(documentList7.get(0).getTags()).contains(tagWithHyphen);

        DocumentList<DocumentModel> documentList8 = db.getPublishedPostsByTag(tagWithHyphen);
        assertEquals(0, documentList8.size());

        Set<String> tags = db.getAllTags();
        assertEquals(Collections.singleton(tagWithHyphen), tags);

        db.deleteContent(typeWithHyphen, uri);

        long documentCount4 = db.getDocumentCount(typeWithHyphen);
        assertEquals(0, documentCount4);

        db.deleteAllByDocType(typeWithHyphen);
    }

    @Test
    public void testIdentifierQuoting() {
        assertNull(quoteIdentifier(null));
        assertEquals("`normalIdentifier`", quoteIdentifier("normalIdentifier"));
        assertEquals("`identifier-with-hyphen`", quoteIdentifier("identifier-with-hyphen"));
        assertEquals("`identifier-with\\\\backslash`", quoteIdentifier("identifier-with\\backslash"));
        assertEquals("`identifier-with\\`backtick`", quoteIdentifier("identifier-with`backtick"));
    }
}
