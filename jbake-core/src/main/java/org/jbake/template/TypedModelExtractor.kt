package org.jbake.template

import org.jbake.template.model.RenderContext

/**
 * Type-safe model extractor that works with RenderContext instead of untyped maps.
 * Extracts specific data from the context for template rendering.
 *
 * @param T The type of data returned by this model extractor.
 */
interface TypedModelExtractor<T> {
    /**
     * Extract data from the render context.
     *
     * @param context The type-safe rendering context
     * @param key The key being requested (for custom extractors)
     * @return The extracted value
     */
    fun extract(context: RenderContext, key: String): T?
}

/**
 * Adapter to bridge between old ModelExtractor and new TypedModelExtractor.
 */
class ModelExtractorAdapter<T>(private val typedExtractor: TypedModelExtractor<T>) : ModelExtractor<T> {

    override fun get(db: org.jbake.app.ContentStore, model: MutableMap<String, Any>, key: String): T? {
        // Try to reconstruct RenderContext from the legacy map
        // This is a temporary bridge during migration
        val config = model["config"] as? org.jbake.app.configuration.JBakeConfiguration
            ?: throw IllegalStateException("Config not found in model")

        val content = model["content"] as? org.jbake.model.DocumentModel
        val renderer = model["renderer"] as? DelegatingTemplateEngine

        val context = RenderContext(
            config = config,
            db = db,
            content = content,
            renderer = renderer,
            customData = model.filterKeys { it !in setOf("config", "content", "renderer", "db") }
        )

        return typedExtractor.extract(context, key)
    }
}

