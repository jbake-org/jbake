package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import org.jbake.app.configuration.DefaultJBakeConfiguration

/**
 * Base class for parametrized database tests.
 * Tests extending this class will run against the specified database type.
 */
abstract class ParametrizedDatabaseTest(
    private val dbType: DatabaseType,
    body: ParametrizedDatabaseTest.() -> Unit
) : StringSpec() {

    protected lateinit var db: ContentStore
    protected lateinit var config: DefaultJBakeConfiguration

    init {
        beforeSpec {
            ContentStoreIntegrationTest.setUpClass(dbType)
            db = ContentStoreIntegrationTest.db
            config = ContentStoreIntegrationTest.config
        }

        beforeTest {
            db.startup()
        }

        afterTest {
            db.drop()
        }

        afterSpec {
            ContentStoreIntegrationTest.cleanUpClass()
        }

        body()
    }
}

