// orientdb-test-setup.main.kts
// This Kotlin script creates a test OrientDB database with admin/admin credentials for JBake tests.
// Usage: Execute via Gradle task or directly with proper classpath

@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("com.orientechnologies:orientdb-core:3.2.47")

import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.OrientDBConfig
import com.orientechnologies.orient.core.sql.executor.OResultSet
import java.io.File

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
    val orient = OrientDB("plocal:./databases", OrientDBConfig.defaultConfig())

    // Drop existing database if it exists
    if (orient.exists(dbName)) {
        println("Dropping existing database '$dbName'...")
        orient.drop(dbName)
    }

    // Create database
    orient.create(dbName, ODatabaseType.PLOCAL)
    println("Database '$dbName' created.")

    // Open database with default credentials
    val db = orient.open(dbName, "admin", "admin")

    println("Database setup complete!")
    println("You can now connect with user: $adminUser, password: $adminPass")

    db.close()
    orient.close()

} catch (e: Exception) {
    println("Error setting up database: ${e.message}")
    e.printStackTrace()
    throw e
}


fun `Check if admin user exists`(db: com.orientechnologies.orient.core.db.ODatabaseSession) {
    val result: OResultSet = db.query("SELECT FROM OUser WHERE name = ?", adminUser)
    if (!result.hasNext()) {
        db.command("INSERT INTO OUser SET name = ?, password = ?, status = 'ACTIVE'", adminUser, adminPass)
        db.command("INSERT INTO ORole SET name = ?, mode = 0, rules = {}", adminUser)
        db.command("UPDATE OUser SET roles = (SELECT FROM ORole WHERE name = ?) WHERE name = ?", adminUser, adminUser)
        println("Admin user created.")
    } else {
        println("Admin user already exists.")
    }
}
