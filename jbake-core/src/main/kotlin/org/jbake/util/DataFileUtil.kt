package org.jbake.util

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.util.Logging.logger
import org.slf4j.Logger

class DataFileUtil(private val db: ContentStore, private val defaultDocType: String?) {

    fun get(ref: String?): MutableMap<String,  Any> {

        var result = mutableMapOf<String,  Any>()
        val docs: DocumentList<*> = db.getDocumentByUri(ref)

        if (docs.isEmpty())
            log.warn("Unable to locate content for ref: $ref")
        else if (docs.size != 1)
            log.warn("Located multiple hits for ref: $ref")

        else @Suppress("UNCHECKED_CAST")
            return if (docs[0] !is MutableMap<*, *>) mutableMapOf()
            else docs[0] as? MutableMap<String, Any> ?: mutableMapOf()

        return result
    }

    private val log: Logger by logger()
}

/**
 * Recursively convert OffsetDateTime to java.util.Date in the model for Freemarker compatibility.
 */
fun convertDatesInModel(value: Any?): Any? = when (value) {
    is Map<*, *> -> value.mapValues { (_, v) -> convertDatesInModel(v) }
    is Collection<*> -> value.map { convertDatesInModel(it) }
    /* Not needed, it is accounted for in the Map case above.
    is org.jbake.model.DocumentModel -> {
        val map = HashMap<String, Any>()
        map.putAll(value)
        value.date?.let { map[ModelAttributes.DOC_DATE] = java.util.Date.from(it.toInstant()) }
        map.mapValues { (_, v) -> convertDatesInModel(v) }
    }*/
    ///is java.time.OffsetDateTime -> java.util.Date.from(value.toInstant()) /// TODO Remove when Freemarker adapter stable.
    else -> value
}
