package org.jbake.app

import io.kotest.matchers.shouldBe
import org.apache.commons.lang3.SystemUtils
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import java.io.File
import java.nio.file.Files
import java.util.*

abstract class ContentStoreIntegrationTest {
    protected fun setUp() {
        db.startup()
    }

    protected fun tearDown() {
        db.drop()
    }

    // TBD: Not used anywhere else??
    internal enum class StorageType {
        MEMORY, PLOCAL;

        override fun toString(): String {
            return this.name.lowercase(Locale.getDefault())
        }
    }

    companion object {
        internal lateinit var tempDir       : File
        internal lateinit var db           : ContentStore
        internal lateinit var config       : DefaultJBakeConfiguration
        internal var          storageType  : StorageType = StorageType.MEMORY
        internal lateinit var sourceDir : File

        fun setUpClass() {
            // Create temp directory
            tempDir = Files.createTempDirectory("jbake-test").toFile()

            sourceDir = TestUtils.testResourcesAsSourceDir
            if (!sourceDir.exists()) throw AssertionError("Cannot find sample data structure!")

            config = ConfigUtil().loadConfig(sourceDir) as DefaultJBakeConfiguration
            config.setSourceDir(sourceDir)

            config.outputExtension shouldBe ".html"
            config.databaseStore   = storageType.toString() // TBD: Not used anywhere else??

            // OrientDB v3.1.x doesn't allow DB name to be a path even though docs say it's allowed
            var dbPath : String = File(tempDir, "documents" + (System.currentTimeMillis() - 1764000000000)).name

            // Setting the database path with a colon is invalid for OrientDB URL on Windows.
            // Only one colon is expected. There is no documentation available about proper URL :(
            if (SystemUtils.IS_OS_WINDOWS)
                dbPath = dbPath.replace(":", "")

            config.databasePath = dbPath
            db = DbUtils.createDataStore(config)
        }

        fun cleanUpClass() {
            db.close()
            db.shutdown()
            tempDir.deleteRecursively()
        }
    }
}
