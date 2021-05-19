package org.jbake.app;

import com.orientechnologies.orient.core.record.impl.ODocument;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jbake.FakeDocumentBuilder;
import static org.jbake.app.ContentStore.quoteIdentifier;
import org.jbake.app.Crawler.Attributes.Status;
import org.jbake.model.DocumentAttributes;
import org.jbake.model.DocumentTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ContentStoreTest extends ContentStoreIntegrationTest {

    public static final String DOC_TYPE_POST = "post";

    @Test
    public void shouldGetCountForPublishedDocuments() throws Exception {

        for (int i = 0; i < 5; i++) {
            FakeDocumentBuilder builder = new FakeDocumentBuilder(DOC_TYPE_POST);
            builder.withName("dummyfile" + i)
                    .withStatus("published")
                    .withRandomSha1()
                    .build();
        }

        FakeDocumentBuilder builder = new FakeDocumentBuilder(DOC_TYPE_POST);
        builder.withName("draftdummy")
                .withStatus("draft")
                .withRandomSha1()
                .build();

        assertEquals(6, db.getDocumentCount(DOC_TYPE_POST));
        assertEquals(5, db.getPublishedCount(DOC_TYPE_POST));
    }

    @Test
    public void testMergeDocument() {
        final String uri = "test/testMergeDocument";

        ODocument doc = new ODocument(DOC_TYPE_POST);
        Map<String, String> values = new HashMap();
        values.put(Crawler.Attributes.TYPE, DOC_TYPE_POST);
        values.put(DocumentAttributes.SOURCE_URI.toString(), uri);
        values.put("foo", "originalValue");
        doc.fromMap(values);
        doc.save();

        // 1st
        values.put("foo", "newValue");
        db.mergeDocument(values);

        DocumentList docs = db.getDocumentByUri(DOC_TYPE_POST, uri);
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

        Map<String, Object> values = new HashMap();
        values.put(Crawler.Attributes.TYPE, typeWithHyphen);
        values.put(Crawler.Attributes.TAG, tagWithHyphen);
        values.put(Crawler.Attributes.TAGS, new String[]{tagWithHyphen});
        values.put(Crawler.Attributes.STATUS, Status.DRAFT);
        values.put(Crawler.Attributes.DATE, new Date());
        values.put(DocumentAttributes.RENDERED.toString(), false);
        values.put(DocumentAttributes.SOURCE_URI.toString(), uri);
        values.put(DocumentAttributes.CACHED.toString(), true);
        values.put(DocumentAttributes.RENDERED.toString(), false);
        values.put("foo", "originalValue");

        ODocument doc = new ODocument(typeWithHyphen);
        doc.fromMap(values);
        doc.save();

        DocumentList documentList1 = db.getAllContent(typeWithHyphen);

        assertEquals(1, documentList1.size());

        DocumentList documentList2 = db.getAllContent(typeWithHyphen, true);

        assertEquals(1, documentList2.size());

        DocumentList documentList3 =  db.getDocumentByUri(typeWithHyphen, uri);

        assertEquals(1, documentList3.size());

        long documentCount1 = db.getDocumentCount(typeWithHyphen);

        assertEquals(1L, documentCount1);

        DocumentList documentList4 = db.getDocumentStatus(typeWithHyphen, uri);

        assertEquals(1, documentList4.size());
        assertEquals(Boolean.FALSE, documentList4.get(0).get(String.valueOf(DocumentAttributes.RENDERED)));

        long documentCount2 = db.getPublishedCount(typeWithHyphen);
        assertEquals(0, documentCount2);

        Map<String,Object> published = new HashMap<>();
        published.put(DocumentAttributes.SOURCE_URI.toString(), uri);
        published.put(Crawler.Attributes.TYPE, typeWithHyphen);
        published.put(Crawler.Attributes.STATUS, Status.PUBLISHED);
        db.mergeDocument(published);

        DocumentList documentList5 = db.getUnrenderedContent(typeWithHyphen);
        assertEquals(1, documentList5.size());
        assertEquals(Boolean.FALSE, documentList5.get(0).get(String.valueOf(DocumentAttributes.RENDERED)));
        assertEquals(typeWithHyphen, documentList5.get(0).get(Crawler.Attributes.TYPE));
        assertEquals(tagWithHyphen, documentList5.get(0).get(Crawler.Attributes.TAG));

        long documentCount3 = db.getPublishedCount(typeWithHyphen);
        assertEquals(1, documentCount3);

        db.markContentAsRendered(typeWithHyphen);

        DocumentList documentList6 = db.getPublishedContent(typeWithHyphen);
        assertEquals(1, documentList6.size());
        assertEquals(Boolean.TRUE, documentList6.get(0).get(String.valueOf(DocumentAttributes.RENDERED)));
        assertEquals(typeWithHyphen, documentList6.get(0).get(Crawler.Attributes.TYPE));
        assertEquals(tagWithHyphen, documentList6.get(0).get(Crawler.Attributes.TAG));

        DocumentList documentList7 = db.getPublishedDocumentsByTag(tagWithHyphen);
        assertEquals(1, documentList7.size());
        assertEquals(Boolean.TRUE, documentList7.get(0).get(String.valueOf(DocumentAttributes.RENDERED)));
        assertEquals(typeWithHyphen, documentList7.get(0).get(Crawler.Attributes.TYPE));
        assertEquals(tagWithHyphen, documentList7.get(0).get(Crawler.Attributes.TAG));

        DocumentList documentList8 = db.getPublishedPostsByTag(tagWithHyphen);
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
