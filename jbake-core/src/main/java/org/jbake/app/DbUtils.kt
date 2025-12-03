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
        return contentStore ?: ContentStore(configuration.databaseStore, configuration.databasePath ?: "jbake")
            .also { contentStore = it }
    }

    fun closeDataStore() {
        contentStore = null
    }


    /** TODO: Move to OrientDB-specific utility class. */
    fun orientdbDocumentToModel(doc: OResult): DocumentModel {
        val result = DocumentModel()
        for (key in doc.propertyNames) {
            val value = doc.getProperty<Any>(key) ?: continue // Skip null values - treat them as missing keys instead
            result[key] = value
        }
        return result
    }

    /**
     * Converts a DB list into a String array. TODO: Quite fragile, check how it is used.
     * @return input entry as String[]
     */
    fun toStringArray(entry: Any): Array<String> = when (entry) {
        is Array<*> -> entry as Array<String>
        is List<*> -> (entry as List<String>).toTypedArray()
        is ArrayList<*> -> (entry as ArrayList<String>).toTypedArray()
        is OTrackedList<*> -> (entry as OTrackedList<String>).toTypedArray() // TODO: OrientDB specific
        else -> arrayOf()
    }
}
