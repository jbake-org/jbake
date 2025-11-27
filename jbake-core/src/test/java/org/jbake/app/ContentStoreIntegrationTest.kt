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

    internal enum class StorageType {
        MEMORY, PLOCAL;

        override fun toString(): String {
            return this.name.lowercase(Locale.getDefault())
        }
    }

    companion object {
        internal lateinit var folder: File
        internal lateinit var db: ContentStore
        internal lateinit var config: DefaultJBakeConfiguration
        internal var storageType: StorageType = StorageType.MEMORY
        internal var sourceFolder: File? = null

        fun setUpClass() {
            // Create temp folder
            folder = Files.createTempDirectory("jbake-test").toFile()

            sourceFolder = TestUtils.testResourcesAsSourceFolder
            if (!sourceFolder!!.exists()) {
                throw AssertionError("Cannot find sample data structure!")
            }

            config = ConfigUtil().loadConfig(sourceFolder!!) as DefaultJBakeConfiguration
            config.setSourceFolder(sourceFolder)

            config.outputExtension shouldBe ".html"
            config.databaseStore = (storageType.toString())

            // OrientDB v3.1.x doesn't allow DB name to be a path even though docs say it's allowed
            var dbPath: String = File(folder, "documents" + System.currentTimeMillis()).name

            // setting the database path with a colon creates an invalid url for OrientDB.
            // only one colon is expected. there is no documentation about proper url path for windows available :(
            if (SystemUtils.IS_OS_WINDOWS) {
                dbPath = dbPath.replace(":", "")
            }
            config.databasePath = (dbPath)
            db = DBUtil.createDataStore(config)
        }

        fun cleanUpClass() {
            db.close()
            db.shutdown()
            folder.deleteRecursively()
        }
    }
}
