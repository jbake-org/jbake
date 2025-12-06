package org.jbake.util

import org.jbake.model.DocumentModel
import org.jbake.template.model.TemplateModel
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Tiny helper used while chasing the missing `author` attribute through the pipeline.
 * Emits a single DEBUG/WARN line per stage so we can see where the value disappears.
 *
 * Keep this file here and it's usage temporary until the root cause is found.
 */
object AuthorTracer {
    fun trace(stage: String, payload: Any?, where: String? = null) {
        if (true) return // Disable temporarily

        val (hasAuthor, value) = extractAuthor(payload)
        val suffix = where?.let { "(Observed in: $it)" } ?: ""
        val msg = if (hasAuthor)
            "AuthorTracer[$stage] author='$value' $suffix"
        else
            "AuthorTracer[$stage] author missing $suffix"

        val timestamp = Instant.now().atZone(ZoneId.systemDefault()).format(timeFormatter)
        System.err.println("$timestamp DEBUG $msg")
        log.warn(msg)
    }

    private fun extractAuthor(payload: Any?): Pair<Boolean, Any?>
        = when (payload) {
            is DocumentModel -> payload.containsKey(AUTHOR_KEY) to payload[AUTHOR_KEY]
            is TemplateModel -> runCatching { payload.content }.getOrNull()
                ?.let { content -> content.containsKey(AUTHOR_KEY) to content[AUTHOR_KEY] }
                ?: (false to null)
            is Map<*, *> -> (payload.containsKey(AUTHOR_KEY)) to payload[AUTHOR_KEY]
            else -> false to null
        }

    private const val AUTHOR_KEY = "author"
    private val log = LoggerFactory.getLogger(AuthorTracer::class.java)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
}
