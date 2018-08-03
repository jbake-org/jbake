package org.jbake.app;

import com.orientechnologies.orient.core.record.impl.ODocument;
import java.util.HashMap;
import java.util.Map;
import org.jbake.FakeDocumentBuilder;
import org.jbake.model.DocumentAttributes;
import static org.junit.Assert.assertEquals;
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
        Map<String, String> values = new HashMap(){{
            put(Crawler.Attributes.TYPE, DOC_TYPE_POST);
            put(DocumentAttributes.SOURCE_URI.toString(), uri);
            put("foo", "originalValue");
        }};
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

}
