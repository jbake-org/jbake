package org.jbake.template

import org.jbake.app.ContentStore
import org.jbake.app.NoModelExtractorException
import org.jbake.model.DocumentTypeUtils
import org.jbake.template.model.PublishedCustomExtractor
import org.jbake.template.model.TypedDocumentsExtractor
import org.jbake.util.Logging.logger
import org.slf4j.Logger
import java.io.IOException
import java.util.*

/**
 * A singleton class giving access to model extractors. Model extractors are loaded based on classpath. New
 * rendering may be registered either at runtime (not recommanded) or by putting a descriptor file on classpath (recommanded).
 *
 * The descriptor file must be found in *META-INF* directory and named
 * *org.jbake.template.ModelExtractors.properties*. The format of the file is easy:
 *
 *   ```
 *   org.jbake.template.model.AllPosts=all_posts
 *   org.jbake.template.model.AllContent=all_content
 *   ```
 * ...where the key is the class of the extractor (must implement [ModelExtractor]  and the value is the key by which values are to be accessed in model.
 *
 *
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered.
 * This allows JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better fit for embedding.
 */
class ModelExtractorsRegistry private constructor() {

    private val extractors: MutableMap<String, ModelExtractor<*>> = TreeMap<String, ModelExtractor<*>>()
    private val log: Logger by logger()

    init {
        loadEngines()
    }

    fun reset() {
        extractors.clear()
        loadEngines()
    }

    fun registerEngine(key: String, extractor: ModelExtractor<*>) {
        val old = extractors.put(key, extractor)
        if (old != null)
            log.warn("Registered a model extractor for key [.$key] but another one was already defined: " + old)
    }

    /**
     * Convenience overload to register a TypedModelExtractor by automatically adapting it to the legacy
     * ModelExtractor interface via ModelExtractorAdapter. This lets new typed extractors be registered
     * without touching all registration sites.
     */
    fun registerEngine(key: String, extractor: TypedModelExtractor<*>) {
        registerEngine(key, ModelExtractorAdapter(extractor))
    }

    /**
     * This method is used internally to load markup engines. Markup engines are found using descriptor files on
     * classpath, so adding an engine is as easy as adding a jar on classpath with the descriptor file included.
     */
    private fun loadEngines() {
        try {
            val cl = ModelExtractorsRegistry::class.java.getClassLoader()
            val resources = cl.getResources(PROPERTIES)
            while (resources.hasMoreElements()) {
                val url = resources.nextElement()
                val props = Properties()
                props.load(url.openStream())
                for (entry in props.entries) {
                    val className = entry.key as String
                    val extensions: Array<String> =
                        (entry.value as String).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    loadAndRegisterEngine(className, *extensions)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadAndRegisterEngine(className: String?, vararg extensions: String) {
        val engine: ModelExtractor<*>? = tryLoadEngine(className)
        if (engine != null) {
            for (extension in extensions) {
                registerEngine(extension, engine)
            }
        }
    }

    @Throws(NoModelExtractorException::class)
    fun <Type> extractAndTransform(
        db: ContentStore,
        key: String,
        map: MutableMap<String, Any>,
        adapter: TemplateEngineAdapter<Type>
    ): Type {
        val extractor = extractors[key] ?: throw NoModelExtractorException("no model extractor for key \"$key\"")
        val extractedValue = extractor.get(db, map, key)
        return adapter.adapt(key, extractedValue!!)
    }

    /**
     * @see java.util.Map.containsKey
     * @param key A key a [ModelExtractor] is registered with
     * @return true if key is registered
     */
    fun containsKey(key: Any): Boolean = extractors.containsKey(key)

    /**
     *  A @[Set] of all known keys a @[ModelExtractor] is registered with,
     * @see java.util.Map.keySet
     */
    fun keySet(): MutableSet<String> = extractors.keys

    fun registerExtractorsForCustomTypes(docType: String) {
        val pluralizedDoctype = DocumentTypeUtils.pluralize(docType)
        if (!containsKey(pluralizedDoctype)) {
            log.info("register new extractors for document type: $docType")
            registerEngine(pluralizedDoctype, TypedDocumentsExtractor())
            registerEngine("published_$pluralizedDoctype", PublishedCustomExtractor(docType))
        }
    }

    companion object {
        private const val PROPERTIES = "META-INF/org.jbake.template.ModelExtractors.properties"

        val instance by lazy { ModelExtractorsRegistry() }

        /**
         * This method is used to search for a specific class, telling if loading the engine would succeed.
         * This is typically used to avoid loading optional modules.
         *
         * @param engineClassName Engine class, used both as a hint to find it and to create the engine itself.
         * @return null if the engine is not available, an instance of the engine otherwise
         */
        private fun tryLoadEngine(engineClassName: String?): ModelExtractor<*>? {
            return try {
                val cl = ModelExtractorsRegistry::class.java.getClassLoader()
                val klass = Class.forName(engineClassName, false, cl)

                // If it's a TypedModelExtractor, instantiate and wrap with adapter
                if (TypedModelExtractor::class.java.isAssignableFrom(klass)) {
                    @Suppress("UNCHECKED_CAST")
                    val typedClass = klass as Class<out TypedModelExtractor<*>>
                    val typedInstance = typedClass.getDeclaredConstructor().newInstance()
                    return ModelExtractorAdapter(typedInstance)
                }
                // Fallback to old ModelExtractor
                @Suppress("UNCHECKED_CAST")
                val engineClass = klass as Class<out ModelExtractor<*>>
                engineClass.getDeclaredConstructor().newInstance()
            }
            catch (e: Exception) {
                when (e) {
                    is ClassNotFoundException,
                    is InstantiationException,
                    is IllegalAccessException -> null /* A dependency of the engine may not be found on classpath. */
                    else -> throw e
                }
            }
        }
    }

}

interface ModelExtractor<T> {
    fun get(db: ContentStore, model: MutableMap<String, Any>, key: String): T?
}
