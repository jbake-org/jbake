package org.jbake.util

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.util.Logging.logger
import org.slf4j.Logger

/**
 * Used in templates - passed under "data".
 */
class DataFileUtil(private val db: ContentStore, private val defaultDocType: String?) {

    fun get(uri: String?): MutableMap<String,  Any> {
        uri ?: return mutableMapOf()
        return loadDocumentsByUri(uri)
    }

    // Only used in tests
    fun loadDocumentsByUri(uri: String): MutableMap<String,  Any> {

        val result = mutableMapOf<String,  Any>()
        val docs: DocumentList<*> = db.getDocumentByUri(uri)

        if (docs.isEmpty())
            log.warn("Unable to locate content for ref: $uri")
        else if (docs.size != 1)
            log.warn("Located multiple hits for ref: $uri")

        else @Suppress("UNCHECKED_CAST")
            return if (docs[0] !is MutableMap<*, *>) mutableMapOf()
            else docs[0] as? MutableMap<String, Any> ?: mutableMapOf()

        return result
    }

    private val log: Logger by logger()
}
