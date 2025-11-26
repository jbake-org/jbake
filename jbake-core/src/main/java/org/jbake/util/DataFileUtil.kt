package org.jbake.util

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DataFileUtil(private val db: ContentStore, private val defaultDocType: String?) {

    fun get(ref: String?): MutableMap<String,  Any> {

        var result: MutableMap<String,  Any> = HashMap()
        val docs: DocumentList<*> = db.getDocumentByUri(ref)
        if (docs.isEmpty()) {
            log.warn("Unable to locate content for ref: {}", ref)
        }
        else {
            if (docs.size == 1)
                result = docs[0] as? MutableMap<String, Any> ?: mutableMapOf()
            else
                log.warn("Located multiple hits for ref: {}", ref)
        }
        return result
    }

    private val log: Logger = LoggerFactory.getLogger(DataFileUtil::class.java)
}
