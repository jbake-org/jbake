package org.jbake.app;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jbake.FakeDocumentBuilder;
import org.jbake.model.DocumentAttributes;
import org.jbake.model.DocumentStatus;
import org.jbake.model.DocumentTypes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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