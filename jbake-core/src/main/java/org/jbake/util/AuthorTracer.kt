package org.jbake.util

import org.jbake.model.DocumentModel
import org.jbake.template.model.TemplateModel
import org.slf4j.LoggerFactory

/**
 * Tiny helper used while chasing the missing `author` attribute through the pipeline.
 * Emits a single DEBUG/WARN line per stage so we can see where the value disappears.
 */
object AuthorTracer {
    private val log = LoggerFactory.getLogger(AuthorTracer::class.java)

    fun trace(stage: String, payload: Any?, context: String? = null) {
        val (hasAuthor, value) = extractAuthor(payload)
        val suffix = context?.let { " (context=$it)" } ?: ""
        if (hasAuthor)
            log.debug("AuthorTracer[{}] author='{}'{}", stage, value, suffix)
        else
            log.warn("AuthorTracer[{}] author missing{}", stage, suffix)
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
}

