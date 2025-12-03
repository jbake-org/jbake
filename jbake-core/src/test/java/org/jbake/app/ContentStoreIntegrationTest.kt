package org.jbake.app

import io.kotest.matchers.shouldBe
import org.apache.commons.lang3.SystemUtils
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.DatabaseType
import java.io.File
import java.nio.file.Files

abstract class ContentStoreIntegrationTest {
    protected fun setUp() {
        db.startup()
    }

    protected fun tearDown() {
        db.drop()
    }

    companion object {
        internal lateinit var tempDir: File
        internal lateinit var db: ContentStore
        internal lateinit var config: DefaultJBakeConfiguration
        internal var databaseType: DatabaseType = DatabaseType.HSQLDB
        internal lateinit var sourceDir: File

        fun setUpClass(dbType: DatabaseType = DatabaseType.HSQLDB) {
            databaseType = dbType
            tempDir = Files.createTempDirectory("jbake-test-${dbType.storeName}").toFile()

            sourceDir = TestUtils.testResourcesAsSourceDir
            if (!sourceDir.exists()) throw AssertionError("Cannot find sample data structure!")

            config = ConfigUtil().loadConfig(sourceDir) as DefaultJBakeConfiguration
            config.setSourceDir(sourceDir)
            config.outputExtension shouldBe ".html"

            val dbPath = createDbPath(dbType)
            db = createContentStore(dbType, dbPath)
        }

        private fun createDbPath(dbType: DatabaseType): String {
            val timestamp = System.currentTimeMillis() - 1764000000000
            var dbPath = File(tempDir, "documents-${dbType.storeName}-$timestamp").name

            if (SystemUtils.IS_OS_WINDOWS)
                dbPath = dbPath.replace(":", "")

            return dbPath
        }

        private fun createContentStore(dbType: DatabaseType, dbPath: String): ContentStore = when (dbType) {
            DatabaseType.HSQLDB -> ContentStore("memory", dbPath)
            DatabaseType.NEO4J -> ContentStore("memory", dbPath)
            DatabaseType.ORIENTDB -> ContentStore("memory", dbPath)
        }

        fun cleanUpClass() {
            db.close()
            db.shutdown()
            tempDir.deleteRecursively()
        }
    }
}
