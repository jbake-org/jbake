package org.jbake.app;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class ContentStoreIntegrationTest {

    protected static ContentStore db;

    @BeforeClass
    public static void setUpClass() {
        //setUpDatabase(StorageType.MEMORY);
    }

    protected static void setUpDatabase(StorageType storageType)
    {
        db = DBUtil.createDataStore(storageType.toString(), "documents" + System.currentTimeMillis());
    }

    /**
     * Override this in the test to use other storage type.
     * @return The storage type string for the OrientDB URL.
     */

    @AfterClass
    public static void cleanUpClass() {
        db.close();
        db.shutdown();
    }

    @Before
    public void setUp() {
        if (db == null)
            throw new IllegalStateException("The test must declare @BeforeClass to call setupUpDatabase().");
        // TODO: This should rather use JUnit's @Rule ExternalResource: https://junit.org/junit4/javadoc/4.12/org/junit/rules/ExternalResource.html

        db.updateSchema();
    }

    @After
    public void tearDown() throws Exception {
        db.drop();
    }



    protected enum StorageType {
        MEMORY, PLOCAL;

        @Override
        public String toString()
        {
            return this.name().toLowerCase();
        }
    }

}
