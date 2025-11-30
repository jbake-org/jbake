package org.jbake.app

import com.orientechnologies.orient.core.db.record.OTrackedList
import com.orientechnologies.orient.core.sql.executor.OResult
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel

object DbUtils {
    private var contentStore: ContentStore? = null

    @Deprecated("")
    fun createDataStore(type: String, name: String): ContentStore {
        if (contentStore == null) {
            contentStore = ContentStore(type, name)
        }
        return contentStore!!
    }

    @Deprecated("")
    fun updateSchema(db: ContentStore) {
        db.updateSchema()
    }

    @JvmStatic
    fun createDataStore(configuration: JBakeConfiguration): ContentStore {
        return contentStore ?: ContentStore(configuration.databaseStore, configuration.databasePath)
            .also { contentStore = it }
    }

    fun closeDataStore() {
        contentStore = null
    }

    fun documentToModel(doc: OResult): DocumentModel {
        val result = DocumentModel()
        for (key in doc.propertyNames) {
            val value = doc.getProperty<Any>(key) ?: continue // Skip null values - treat them as missing keys instead
            result[key] = value
        }
        return result
    }

    /**
     * Converts a DB list into a String array
     *
     * @param entry Entry input to be converted
     * @return input entry as String[]
     */
    @Suppress("UNCHECKED_CAST")
    fun toStringArray(entry: Any): Array<String> = when (entry) {
        is Array<*> -> entry as Array<String>
        is OTrackedList<*> -> (entry as OTrackedList<String>).toTypedArray()
        is ArrayList<*> -> (entry as ArrayList<String>).toTypedArray()
        else -> arrayOf()
    }
}
