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
        if (contentStore == null) {
            val storeType = configuration.databaseStore
            contentStore = ContentStore(storeType, configuration.databasePath)
        }

        return contentStore!!
    }

    fun closeDataStore() {
        contentStore = null
    }

    fun documentToModel(doc: OResult): DocumentModel {
        val result = DocumentModel()

        for (key in doc.getPropertyNames()) {
            result.put(key, doc.getProperty<Any?>(key))
        }
        return result
    }

    /**
     * Converts a DB list into a String array
     *
     * @param entry Entry input to be converted
     * @return input entry as String[]
     */
    fun toStringArray(entry: Any): Array<String> {
        if (entry is Array<*>) {
            return entry as Array<String>
        } else if (entry is OTrackedList<*>) {
            val list = entry as OTrackedList<String>
            return list.toTypedArray<String>()
        } else if (entry is ArrayList<*>) {
            val list = entry as ArrayList<String>
            return list.toTypedArray<String>()
        }
        return arrayOf()
    }
}
