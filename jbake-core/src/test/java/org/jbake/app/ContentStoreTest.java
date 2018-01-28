package org.jbake.app;

import org.jbake.FakeDocumentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContentStoreTest {

    private ContentStore db;

    @Before
    public void setUp() throws Exception {
        db = DBUtil.createDataStore("memory", "documents" + System.currentTimeMillis());
    }

    @After
    public void tearDown() throws Exception {
        db.drop();
        db.close();
        db.shutdown();
    }

    @Test
    public void shouldGetCountForPublishedDocuments() throws Exception {

        for (int i = 0; i < 5; i++) {
            FakeDocumentBuilder builder = new FakeDocumentBuilder("post");
            builder.withName("dummyfile" + i)
                    .withStatus("published")
                    .withRandomSha1()
                    .build();
        }

        FakeDocumentBuilder builder = new FakeDocumentBuilder("post");
        builder.withName("draftdummy")
                .withStatus("draft")
                .withRandomSha1()
                .build();

        assertEquals( 6, db.getDocumentCount("post"));
        assertEquals( 5, db.getPublishedCount("post"));
    }

}