package org.jbake.app;

import java.io.File;
import java.nio.file.Path;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class ContentStoreIntegrationTest {

    @TempDir
    protected static Path folder;
    protected static ContentStore db;
    protected static DefaultJBakeConfiguration config;
    protected static StorageType storageType = StorageType.MEMORY;
    protected static File sourceFolder;

    @BeforeAll
    static void setUpClass() throws Exception {

        sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        assertTrue(sourceFolder.exists(), "Cannot find sample data structure!");

        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(sourceFolder);
        config.setSourceFolder(sourceFolder);

        assertEquals(".html", config.getOutputExtension());
        config.setDatabaseStore(storageType.toString());
        // OrientDB v3.1.x doesn't allow DB name to be a path even though docs say it's allowed
        String dbPath = folder.resolve("documents" + System.currentTimeMillis()).toFile().getName();

        // setting the database path with a colon creates an invalid url for OrientDB.
        // only one colon is expected. there is no documentation about proper url path for windows available :(
        if (OS.current() == OS.WINDOWS) {
            dbPath = dbPath.replace(":", "");
        }
        config.setDatabasePath(dbPath);
        db = DBUtil.createDataStore(config);
    }

    @AfterAll
    static void cleanUpClass() {
        db.close();
        db.shutdown();
    }

    @BeforeEach
    void setUp() {
        db.startup();
    }

    @AfterEach
    void tearDown() {
        db.drop();
    }

    protected enum StorageType {
        MEMORY, PLOCAL;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

}
