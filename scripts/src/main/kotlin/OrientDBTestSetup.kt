import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.OrientDBConfig
import java.io.File

/**
 * Sets up OrientDB test database for JBake tests.
 */
object OrientDBTestSetup {
    @JvmStatic
    fun main(args: Array<String>) {
        // Disable OrientDB's script manager to avoid JSR223 dependencies
        System.setProperty("orientdb.script.pool.enabled", "false")

        val dbPath = "./databases/jbaketest"
        val dbName = "jbaketest"
        val adminUser = "admin"
        val adminPass = "admin"

        println("Setting up OrientDB test database...")
        println("Database path: $dbPath")

        // Ensure the databases directory exists
        File(dbPath).parentFile?.mkdirs()

        try {
            // Create OrientDB instance without authentication for embedded mode
            val orient = OrientDB("embedded:./databases", OrientDBConfig.defaultConfig())

            // Drop existing database if it exists
            if (orient.exists(dbName)) {
                println("Dropping existing database '$dbName'...")
                orient.drop(dbName)
            }

            // Create database - in embedded mode, this creates with default admin user
            //orient.create(dbName, ODatabaseType.PLOCAL)
            // Alternative way:
            orient.execute("CREATE DATABASE $dbName PLOCAL USERS ($adminUser IDENTIFIED BY '$adminPass' ROLE admin)")
            println("Database '$dbName' created with admin user.")

            // Test opening the database.
            val db = orient.open(dbName, adminUser, adminPass)

            db.close()
            orient.close()
            println("Database setup complete!")

        } catch (e: Exception) {
            println("Error setting up database: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}

