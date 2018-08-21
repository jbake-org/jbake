package org.jbake.app;

import org.jbake.FakeDocumentBuilder;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContentStoreTest extends ContentStoreIntegrationTest {

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
