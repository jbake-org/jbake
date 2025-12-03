package org.jbake.util

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.util.Logging.logger
import org.slf4j.Logger

class DataFileUtil(private val db: ContentStore, private val defaultDocType: String?) {

    fun get(ref: String?): MutableMap<String,  Any> {

        var result: MutableMap<String,  Any> = HashMap()
        val docs: DocumentList<*> = db.getDocumentByUri(ref)

        if (docs.isEmpty())
            log.warn("Unable to locate content for ref: $ref")
        else if (docs.size != 1)
            log.warn("Located multiple hits for ref: $ref")
        else {
            val doc = docs[0]
            result = if (doc is MutableMap<*, *>) {
                @Suppress("UNCHECKED_CAST")
                doc as? MutableMap<String, Any> ?: mutableMapOf()
            } else mutableMapOf()
        }
        return result
    }

    private val log: Logger by logger()
}
