package org.jbake.app;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.FakeDocumentBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentStoreTest extends ContentStoreIntegrationTest {

    private ContentStore db;

    public void setUp() {
        JBakeConfiguration configuration = mock(JBakeConfiguration.class);
        when(configuration.getDatabaseStore()).thenReturn("memory");
        when(configuration.getDatabasePath()).thenReturn("documents" + System.currentTimeMillis());
        db = DBUtil.createDataStore(configuration);
        db.startup();
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