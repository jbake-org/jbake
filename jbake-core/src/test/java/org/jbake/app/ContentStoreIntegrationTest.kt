package org.jbake.app

import org.apache.commons.vfs2.util.Os
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.junit.*
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.*

abstract class ContentStoreIntegrationTest {
    @Before
    fun setUp() {
        db!!.startup()
    }

    @After
    fun tearDown() {
        db!!.drop()
    }

    internal enum class StorageType {
        MEMORY, PLOCAL;

        override fun toString(): String {
            return this.name.lowercase(Locale.getDefault())
        }
    }

    companion object {
        @ClassRule @JvmField
        var folder: TemporaryFolder = TemporaryFolder()

        internal lateinit var db: ContentStore
        protected var config: DefaultJBakeConfiguration? = null
        internal var storageType: StorageType = StorageType.MEMORY
        protected var sourceFolder: File? = null

        @BeforeClass @JvmStatic
        fun setUpClass() {
            sourceFolder = TestUtils.testResourcesAsSourceFolder
            Assert.assertTrue("Cannot find sample data structure!", sourceFolder!!.exists())

            config = ConfigUtil().loadConfig(sourceFolder!!) as DefaultJBakeConfiguration
            config.setSourceFolder(sourceFolder)

            Assert.assertEquals(".html", config.outputExtension)
            config.setDatabaseStore(storageType.toString())
            // OrientDB v3.1.x doesn't allow DB name to be a path even though docs say it's allowed
            var dbPath: String = folder.newFolder("documents" + System.currentTimeMillis()).getName()

            // setting the database path with a colon creates an invalid url for OrientDB.
            // only one colon is expected. there is no documentation about proper url path for windows available :(
            if (Os.isFamily(Os.OS_FAMILY_WINDOWS)) {
                dbPath = dbPath.replace(":", "")
            }
            config.setDatabasePath(dbPath)
            db = DBUtil.createDataStore(config)
        }

        @AfterClass @JvmStatic
        fun cleanUpClass() {
            db!!.close()
            db!!.shutdown()
        }
    }
}
