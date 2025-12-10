package org.jbake.template

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.MapConfiguration
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.template.model.RenderContext
import org.jbake.util.Logging.logger
import org.slf4j.Logger

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
    private val log: Logger by logger()

    override fun get(db: org.jbake.app.ContentStore, model: MutableMap<String, Any>, key: String): T? {

        // Try to reconstruct RenderContext from the legacy map.
        // The legacy model may contain either a JBakeConfiguration instance or a plain Map<String,Any>.
        // If a plain map is present, wrap it into a CompositeConfiguration + DefaultJBakeConfiguration.
        // We'll accumulate extra custom data to pass to RenderContext (e.g., normalized config map)
        val extraData: MutableMap<String, Any> = HashMap()

        @Suppress("DEPRECATION")
        val configObj: org.jbake.app.configuration.JBakeConfiguration = when (val raw = model["config"]) {
            is org.jbake.app.configuration.JBakeConfiguration -> raw
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val rawMap = raw as? Map<String, Any> ?: emptyMap()

                val normalizedMap: MutableMap<String, Any> = HashMap(rawMap)
                for ((k, v) in rawMap.entries)
                    k.replace('_', '.').let { if (!normalizedMap.containsKey(it)) normalizedMap[it] = v }

                // Expose normalizedMap to typed extractors via custom data.
                extraData["__raw_config_map"] = normalizedMap
                val cc = CompositeConfiguration()
                cc.addConfiguration(MapConfiguration(normalizedMap))
                DefaultJBakeConfiguration(cc)
            }
            null -> {
                val cc = CompositeConfiguration()
                DefaultJBakeConfiguration(cc)
            }
            else -> {
                log.warn("Unsupported config type in model: ${raw::class}. Falling back to empty configuration.")
                val cc = CompositeConfiguration()
                DefaultJBakeConfiguration(cc)
            }
        }

        // Extract common fields first so they are available for the explicit-config branch
        val content = model["content"] as? org.jbake.model.DocumentModel
        val renderer = model["renderer"] as? DelegatingTemplateEngine

        // Prefer explicit 'jbake_config' if provided (delegating engine sets this).
        if (model.containsKey("jbake_config") && model["jbake_config"] is org.jbake.app.configuration.JBakeConfiguration) {
            val explicit = model["jbake_config"] as org.jbake.app.configuration.JBakeConfiguration
            val explicitContext = RenderContext(
                config = explicit,
                db = db,
                content = content,
                renderer = renderer,
                customData = model.filterKeys { it !in setOf("config", "content", "renderer", "db", "jbake_config") } + extraData
            )
            return typedExtractor.extract(explicitContext, key)
        }

        val context = RenderContext(
            config = configObj,
            db = db,
            content = content,
            renderer = renderer,
            customData = model.filterKeys { it !in setOf("config", "content", "renderer", "db") } + extraData
        )

        if (model["config"] is Map<*, *>) {
            val cfgMap = model["config"] as Map<*, *>
            if (cfgMap.containsKey("tag.path") || cfgMap.containsKey("tag_path")) {
                log.info("ModelExtractorAdapter: config contains tag.path='${cfgMap["tag.path"]}', tag_path='${cfgMap["tag_path"]}'.")
            } else {
                log.info("ModelExtractorAdapter: config does NOT contain tag.path or tag_path keys. size=${cfgMap.size}")
            }
        }

        return typedExtractor.extract(context, key)
    }
}
