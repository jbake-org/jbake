package org.jbake.app;

import org.jbake.FakeDocumentBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContentStoreTest {

    private static ContentStore db;

    @BeforeClass
    public static void setUpClass() {
        db = DBUtil.createDataStore("memory", "documents" + System.currentTimeMillis());
    }

    @AfterClass
    public static void cleanUpClass() {
        db.close();
        db.shutdown();
    }

    @Before
    public void setUp() {
        db.updateSchema();
    }

    @After
    public void tearDown() throws Exception {
        db.drop();
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

        assertEquals(6, db.getDocumentCount("post"));
        assertEquals(5, db.getPublishedCount("post"));
    }

}