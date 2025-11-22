package org.jbake.template

import org.jbake.app.ContentStore
import org.jbake.model.DocumentTypeUtils
import org.jbake.template.model.PublishedCustomExtractor
import org.jbake.template.model.TypedDocumentsExtractor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

/**
 *
 * A singleton class giving access to model extractors. Model extractors are loaded based on classpath. New
 * rendering may be registered either at runtime (not recommanded) or by putting a descriptor file on classpath (recommanded).
 *
 * The descriptor file must be found in *META-INF* directory and named
 * *org.jbake.template.ModelExtractors.properties*. The format of the file is easy:
 * `org.jbake.template.model.AllPosts=all_posts<br></br> org.jbake.template.model.AllContent=all_content<br></br> `
 *
 * where the key is the class of the extractor (must implement [ModelExtractor]  and the value is the key
 * by which values are to be accessed in model.
 *
 *
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered. This allows
 * JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better fit
 * for embedding.
 *
 * @author ndx
 * @author Cédric Champeau
 */
class ModelExtractors private constructor() {
    private val extractors: MutableMap<String, ModelExtractor<*>> = TreeMap<String, ModelExtractor<*>>()

    init {
        loadEngines()
    }

    fun reset() {
        extractors.clear()
        loadEngines()
    }

    fun registerEngine(key: String, extractor: ModelExtractor<*>) {
        val old = extractors.put(key, extractor)
        if (old != null) {
            log.warn("Registered a model extractor for key [.{}] but another one was already defined: {}", key, old)
        }
    }

    /**
     * This method is used internally to load markup engines. Markup engines are found using descriptor files on
     * classpath, so adding an engine is as easy as adding a jar on classpath with the descriptor file included.
     */
    private fun loadEngines() {
        try {
            val cl = ModelExtractors::class.java.getClassLoader()
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
    ): Type? {
        if (extractors.containsKey(key)) {
            val extractedValue = extractors[key]!!.get(db, map, key)
            return adapter.adapt(key, extractedValue!!)
        } else {
            throw NoModelExtractorException("no model extractor for key \"$key\"")
        }
    }

    /**
     * @see java.util.Map.containsKey
     * @param key A key a [ModelExtractor] is registered with
     * @return true if key is registered
     */
    fun containsKey(key: Any?): Boolean {
        return extractors.containsKey(key)
    }

    /**
     * @return  A @[Set] of all known keys a @[ModelExtractor] is registered with
     * @see java.util.Map.keySet
     */
    fun keySet(): MutableSet<String> {
        return extractors.keys
    }

    fun registerExtractorsForCustomTypes(docType: String) {
        val pluralizedDoctype = DocumentTypeUtils.pluralize(docType)
        if (!containsKey(pluralizedDoctype)) {
            log.info("register new extractors for document type: {}", docType)
            registerEngine(pluralizedDoctype, TypedDocumentsExtractor())
            registerEngine("published_$pluralizedDoctype", PublishedCustomExtractor(docType))
        }
    }

    companion object {
        private const val PROPERTIES = "META-INF/org.jbake.template.ModelExtractors.properties"

        private val log: Logger = LoggerFactory.getLogger(ModelExtractors::class.java)

        val instance: ModelExtractors = ModelExtractors()

        /**
         * This method is used to search for a specific class, telling if loading the engine would succeed. This is
         * typically used to avoid loading optional modules.
         *
         * @param engineClassName engine class, used both as a hint to find it and to create the engine itself.  @return null if the engine is not available, an instance of the engine otherwise
         */
        private fun tryLoadEngine(engineClassName: String?): ModelExtractor<*>? {
            try {
                val engineClass = Class.forName(engineClassName, false, ModelExtractors::class.java.getClassLoader())
                    as Class<out ModelExtractor<*>>
                return engineClass.newInstance()
            }
            catch (e: ClassNotFoundException) { return null }
            catch (e: InstantiationException) { return null }
            catch (e: IllegalAccessException) { return null }
            catch (e: NoClassDefFoundError) { /* a dependency of the engine may not be found on classpath */ return null }
        }
    }
}
