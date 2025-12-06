package org.jbake.app

enum class DatabaseType(val storeName: String) {
    HSQLDB("hsqldb"),
    NEO4J("neo4j"),
    ORIENTDB("orientdb");

    override fun toString() = storeName
}
