package org.jbake.util

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.util.Logging.logger
import org.slf4j.Logger
import java.time.ZoneId

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

/**
 * Recursively convert Java time types to java.util.Date in the model for Freemarker compatibility.
 * Handles OffsetDateTime, ZonedDateTime, Instant, LocalDateTime and LocalDate.
 */
fun convertDatesInModel(value: Any?): Any? = when (value) {
    is Map<*, *> -> value.mapValues { (_, v) -> convertDatesInModel(v) } // Also handles DocumentModel etc.
    is Collection<*> -> value.map { convertDatesInModel(it) }
    is java.time.OffsetDateTime -> java.util.Date.from(value.toInstant())
    is java.time.ZonedDateTime -> java.util.Date.from(value.toInstant())
    is java.time.Instant -> java.util.Date.from(value)
    is java.time.LocalDateTime -> java.util.Date.from(value.atZone(ZoneId.systemDefault()).toInstant())
    is java.time.LocalDate -> java.util.Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant())
    else -> value
}
