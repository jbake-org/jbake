package org.jbake.app

import com.orientechnologies.orient.core.db.record.OTrackedList
import com.orientechnologies.orient.core.sql.executor.OResult
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel

object DBUtil {
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
        doc.propertyNames.forEach { key ->
            result[key] = doc.getProperty(key)
        }
        return result
    }

    /**
     * Converts a DB list into a String array
     *
     * @param entry Entry input to be converted
     * @return input entry as String[]
     */
    fun toStringArray(entry: Any): Array<String> = when (entry) {
        is Array<*> -> entry as? Array<String> ?: emptyArray()
        is OTrackedList<*> -> (entry as? OTrackedList<String>)?.toTypedArray() ?: emptyArray()
        is ArrayList<*> -> (entry as? ArrayList<String>)?.toTypedArray() ?: emptyArray()
        else -> arrayOf()
    }
}
