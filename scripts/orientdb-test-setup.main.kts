// orientdb-test-setup.main.kts
// This Kotlin script creates a test OrientDB database with admin/admin credentials for JBake tests.
// Usage: run with kotlinc or as a Kotlin script (kscript or kotlinc -script)

@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("com.orientechnologies:orientdb-core:3.2.46")
@file:DependsOn("com.orientechnologies:orientdb-server:3.2.46")

// Just throwing deps on it and see what sticks.
@file:DependsOn("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.21")
@file:DependsOn("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
@file:DependsOn("org.jetbrains.kotlin:kotlin-script-runtime:2.2.21")
@file:DependsOn("org.jetbrains.kotlin:kotlin-scripting-jvm:2.2.21")
@file:DependsOn("org.jetbrains.kotlin:kotlin-scripting-common:2.2.21")
@file:DependsOn("org.jetbrains.kotlin:kotlin-scripting-dependencies:2.2.21")
@file:DependsOn("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:2.2.21")
@file:DependsOn("org.jetbrains.kotlin:kotlin-scripting-jsr223:2.2.20")
@file:DependsOn("org.jetbrains.kotlin:kotlin-script-runtime:2.2.21")

@file:Repository("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")

import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.OrientDBConfig

val dbPath = "./databases/jbake-test"
val dbName = "jbake-test"
val adminUser = "admin"
val adminPass = "admin"
val rootUser = "root"
val rootPass = "root"

val orient = OrientDB("plocal:", OrientDBConfig.defaultConfig())

// Create database if it doesn't exist
if (!orient.exists(dbName)) {
    orient.create(dbName, ODatabaseType.PLOCAL)
    println("Database '$dbName' created.")
} else {
    println("Database '$dbName' already exists.")
}

// Open as root
val db = orient.open(dbName, rootUser, rootPass)

// Check if admin user exists
val result = db.query("SELECT FROM OUser WHERE name = ?", adminUser)
if (!result.hasNext()) {
    db.command("INSERT INTO OUser SET name = ?, password = ?, status = 'ACTIVE'", adminUser, adminPass)
    db.command("INSERT INTO ORole SET name = ?, mode = 0, rules = {}", adminUser)
    db.command("UPDATE OUser SET roles = (SELECT FROM ORole WHERE name = ?) WHERE name = ?", adminUser, adminUser)
    println("Admin user created.")
} else {
    println("Admin user already exists.")
}

// Verify
val users = db.query("SELECT FROM OUser")
while (users.hasNext()) {
    println(users.next())
}

db.close()
orient.close()

