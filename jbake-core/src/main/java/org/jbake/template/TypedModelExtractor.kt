package org.jbake.template

import org.jbake.template.model.RenderContext
import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.MapConfiguration
import org.jbake.app.configuration.DefaultJBakeConfiguration

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
    private val log = org.slf4j.LoggerFactory.getLogger(ModelExtractorAdapter::class.java)

    override fun get(db: org.jbake.app.ContentStore, model: MutableMap<String, Any>, key: String): T? {
        val rawType = model["config"]?.let { it::class } ?: "<null>"
        log.info("ModelExtractorAdapter.get invoked for key='{}' with config raw type: {}", key, rawType)
        // Try to reconstruct RenderContext from the legacy map.
        // The legacy model may contain either a JBakeConfiguration instance or a plain Map<String,Any>.
        // If a plain map is present, wrap it into a CompositeConfiguration + DefaultJBakeConfiguration.
        val configObj: org.jbake.app.configuration.JBakeConfiguration = when (val raw = model["config"]) {
            is org.jbake.app.configuration.JBakeConfiguration -> raw
            is Map<*, *> -> {
                // Create a CompositeConfiguration backed by the legacy map so DefaultJBakeConfiguration can be used.
                // Use the deprecated constructor that accepts only a CompositeConfiguration to avoid calling
                // setupPaths with a null sourceFolder (which would throw).
                val cc = CompositeConfiguration()
                @Suppress("UNCHECKED_CAST")
                cc.addConfiguration(MapConfiguration(raw as Map<String, Any>))
                @Suppress("DEPRECATION")
                DefaultJBakeConfiguration(cc)
            }
            null -> {
                // No config provided in the model: create a minimal DefaultJBakeConfiguration backed by an empty CompositeConfiguration.
                val cc = CompositeConfiguration()
                @Suppress("DEPRECATION")
                DefaultJBakeConfiguration(cc)
            }
            else -> {
                log.warn("Unsupported config type in model: {}. Falling back to empty configuration.", raw::class)
                val cc = CompositeConfiguration()
                @Suppress("DEPRECATION")
                DefaultJBakeConfiguration(cc)
            }
        }

        val content = model["content"] as? org.jbake.model.DocumentModel
        val renderer = model["renderer"] as? DelegatingTemplateEngine

        val context = RenderContext(
            config = configObj,
            db = db,
            content = content,
            renderer = renderer,
            customData = model.filterKeys { it !in setOf("config", "content", "renderer", "db") }
        )

        return typedExtractor.extract(context, key)
    }
}
