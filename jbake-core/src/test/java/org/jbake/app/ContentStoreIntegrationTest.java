package org.jbake.app;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public abstract class ContentStoreIntegrationTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();
    protected static ContentStore db;
    protected static DefaultJBakeConfiguration config;
    protected static File sourceFolder;

    @BeforeClass
    public static void setUpClass() throws Exception {

        sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        Assert.assertTrue("Cannot find sample data structure!", sourceFolder.exists());

        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourceFolder);
        config.setSourceFolder(sourceFolder);

        Assert.assertEquals(".html", config.getOutputExtension());
        db = new ContentStore();
    }

    @AfterClass
    public static void cleanUpClass() {
        // no-op
    }

    @Before
    public void setUp() {
        // no-op: in-memory store is always ready
    }

    @After
    public void tearDown() {
        db.drop();
    }
}
