package org.jbake.app;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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

        Map<String, Object> fileContents = new HashMap<String, Object>();

        for (int i = 0; i < 5; i++) {
            fileContents.put("name", "dummyfile" + i);
            fileContents.put("status", "published");
            createFakeDocument("post", fileContents);
        }

        fileContents.put("name", "draftdummy");
        fileContents.put("status", "draft");
        createFakeDocument("post",fileContents);

        assertEquals( 6, db.getDocumentCount("post"));
        assertEquals( 5, db.getPublishedCount("post"));
    }

    private void createFakeDocument(String name, Map<String, Object> fileContents) {
        ODocument doc = new ODocument(name);
        doc.fields(fileContents);
        doc.save();
    }

}