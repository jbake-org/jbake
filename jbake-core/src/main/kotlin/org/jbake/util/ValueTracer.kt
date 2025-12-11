package org.jbake.util

import org.jbake.model.DocumentModel
import org.jbake.template.model.JbakeTemplateModel
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

/**
 * A helper for chasing missing or wrong attributes through the pipeline.
 * Emits a single DEBUG/WARN line per stage so we can see where the value disappears.
 */
object ValueTracer {

    fun trace(stage: String, payload: Any?, context: String? = null) {
        //if (true) return // Disable temporarily

        val (hasAuthor, value) = extractKey(payload)
        val suffix = context?.let { "(Context: $it)" } ?: ""
        val msg = if (hasAuthor)
            "ValueTracer[$stage] $TRACED_KEY PRESENT = '$value' $suffix"
        else
            "ValueTracer[$stage] $TRACED_KEY MISSING $suffix"

        //val timestamp = Instant.now().atZone(ZoneId.systemDefault()).format(timeFormatter)
        //System.err.println("$timestamp DEBUG $msg")
        log.warn(msg)
    }

    private fun extractKey(payload: Any?): Pair<Boolean, Any?>
        = when (payload) {
            is DocumentModel -> payload.containsKey(AUTHOR_KEY) to payload[AUTHOR_KEY]
            is JbakeTemplateModel -> runCatching { payload.content }.getOrNull()
                ?.let { content -> content.containsKey(AUTHOR_KEY) to content[AUTHOR_KEY] }
                ?: (false to null)
            is Map<*, *> -> (payload.containsKey(AUTHOR_KEY)) to payload[AUTHOR_KEY]
            else -> false to null
        }

    private const val AUTHOR_KEY = "author"
    private const val DATE_KEY = "date"
    private const val TRACED_KEY = DATE_KEY
    private val log = LoggerFactory.getLogger(ValueTracer::class.java)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
}
