package org.jbake.app;

import org.jbake.FakeDocumentBuilder;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes.Status;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

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
    public void testStoreTypeWithSpecialCharacters() {
        final String typeWithHyphen = "type-with-hyphen";

        DocumentTypes.addDocumentType(typeWithHyphen);

        final String tagWithHyphenBackslashAndBacktick = "identifier-with\\`backtick";
        final String uri = "test/testMergeDocument";

        DocumentModel model = DocumentModel.createDefaultDocumentModel();
        model.setType(typeWithHyphen);
        model.setTags(new String[]{tagWithHyphenBackslashAndBacktick});
        model.setDate(new Date());
        model.setSourceUri(uri);
        model.put("foo", "originalValue");

        db.addDocument(model);

        DocumentList<DocumentModel> documentList1 = db.getAllContent(typeWithHyphen);

        assertEquals(1, documentList1.size());

        DocumentList<DocumentModel> documentList2 = db.getAllContent(typeWithHyphen, true);

        assertEquals(1, documentList2.size());

        DocumentList<DocumentModel> documentList3 =  db.getDocumentByUri(uri);

        assertEquals(1, documentList3.size());

        long documentCount1 = db.getDocumentCount(typeWithHyphen);

        assertEquals(1L, documentCount1);

        DocumentList<DocumentModel> documentList4 = db.getDocumentStatus(uri);

        assertEquals(1, documentList4.size());
        assertEquals(Boolean.FALSE, documentList4.get(0).getRendered());

        long documentCount2 = db.getPublishedCount(typeWithHyphen);
        assertEquals(0, documentCount2);

        DocumentModel published = new DocumentModel();
        published.setSourceUri("test/another-testdocument.adoc");
        published.setTags(new String[]{tagWithHyphenBackslashAndBacktick});
        published.setType(typeWithHyphen);
        published.setStatus(Status.PUBLISHED);
        published.setCached(true);
        published.setRendered(false);

        db.addDocument(published);

        DocumentList<DocumentModel> documentList5 = db.getUnrenderedContent();
        assertEquals(2, documentList5.size());
        assertEquals(Boolean.FALSE, documentList5.get(0).getRendered());
        assertEquals(typeWithHyphen, documentList5.get(0).getType());
        assertThat(documentList5.get(0).getTags()).contains(tagWithHyphenBackslashAndBacktick);

        long documentCount3 = db.getPublishedCount(typeWithHyphen);
        assertEquals(1, documentCount3);

        db.markContentAsRendered(published);

        DocumentList<DocumentModel> documentList6 = db.getPublishedContent(typeWithHyphen);
        assertEquals(1, documentList6.size());
        assertEquals(Boolean.TRUE, documentList6.get(0).getRendered());
        assertEquals(typeWithHyphen, documentList6.get(0).getType());
        assertThat(documentList6.get(0).getTags()).contains(tagWithHyphenBackslashAndBacktick);

        DocumentList<DocumentModel> documentList7 = db.getPublishedDocumentsByTag(tagWithHyphenBackslashAndBacktick);
        assertEquals(1, documentList7.size());
        assertEquals(Boolean.TRUE, documentList7.get(0).getRendered());
        assertEquals(typeWithHyphen, documentList7.get(0).getType());
        assertThat(documentList7.get(0).getTags()).contains(tagWithHyphenBackslashAndBacktick);

        DocumentList<DocumentModel> documentList8 = db.getPublishedPostsByTag(tagWithHyphenBackslashAndBacktick);
        assertEquals(0, documentList8.size());

        Set<String> tags = db.getAllTags();
        assertEquals(Collections.singleton(tagWithHyphenBackslashAndBacktick), tags);

        db.deleteContent(uri);

        long documentCount4 = db.getDocumentCount(typeWithHyphen);
        assertEquals(1, documentCount4);

        db.deleteAllByDocType(typeWithHyphen);
    }

}
