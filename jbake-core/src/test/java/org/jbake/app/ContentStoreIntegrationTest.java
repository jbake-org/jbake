package org.jbake.app;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class ContentStoreIntegrationTest {

    protected static ContentStore db;

    @BeforeClass
    public static void setUpClass() {
        db = DBUtil.createDataStore("plocal", "documents" + System.currentTimeMillis());
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
}
