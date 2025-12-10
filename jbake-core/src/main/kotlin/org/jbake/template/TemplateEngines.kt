package org.jbake.template

import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.util.Logging.logger
import org.jbake.util.debug
import org.slf4j.Logger
import java.io.IOException
import java.lang.reflect.Constructor
import java.util.*

/**
 * A singleton class giving access to rendering engines. Rendering engines are loaded based on classpath.
 * New rendering may be registered either at runtime (not recommanded) or by putting a descriptor file on classpath (recommanded).
 *
 * The descriptor file must be found in *META-INF* directory and named `org.jbake.parser.TemplateEngines.properties`. The format is:
 *
 *   ```
 *   org.jbake.parser.FreeMarkerRenderer=ftl
 *   org.jbake.parser.GroovyRenderer=groovy,gsp
 *   ```
 *
 * ...where
 *   - the key is the class of the engine (must extend [AbstractTemplateEngine] and have a 4-arg constructor,
 *   - the value is a comma-separated list of file extensions that this engine is capable of proceeding.
 *
 * Rendering engines are singletons, so are typically used to initialize the underlying template engines.
 *
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered.
 * This allows JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better fit for embedding.
 */
class TemplateEngines(config: JBakeConfiguration, db: ContentStore) {
    private val engines: MutableMap<String, AbstractTemplateEngine> = HashMap<String, AbstractTemplateEngine>()

    val recognizedExtensions: MutableSet<String>
        get() = Collections.unmodifiableSet(engines.keys)

    init {
        loadEngines(config, db)
    }

    private fun registerEngine(fileExtension: String, templateEngine: AbstractTemplateEngine) {
        val old = engines.put(fileExtension, templateEngine)
        if (old != null)
            log.warn("Registered a template engine for extension '.$fileExtension', but another one was already defined: $old")
    }

    fun getEngine(fileExtension: String): AbstractTemplateEngine? = engines[fileExtension]

    /**
     * This method is used internally to load markup engines. Markup engines are found using descriptor files on
     * classpath, so adding an engine is as easy as adding a jar on classpath with the descriptor file included.
     */
    private fun loadEngines(config: JBakeConfiguration, db: ContentStore) {
        try {
            val cl = TemplateEngines::class.java.getClassLoader()
            val resources = cl.getResources("META-INF/org.jbake.parser.TemplateEngines.properties")
            while (resources.hasMoreElements()) {
                val url = resources.nextElement()
                val props = Properties().apply { load(url.openStream()) }
                for (entry in props.entries) {
                    val className = entry.key as String
                    val extensions = (entry.value as String).split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    log.info("Registering template engine: $className for extensions: $extensions")
                    registerEngine(config, db, className, extensions)
                }
            }
        } catch (e: IOException) {
            log.error("Error loading engines", e)
        }
    }

    private fun registerEngine(config: JBakeConfiguration, db: ContentStore, className: String, extensions: List<String>) {

        val engine: AbstractTemplateEngine = tryLoadEngine(config, db, className) ?: return

        for (extension in extensions)
            registerEngine(extension, engine)
    }

    companion object {

        /**
         * This method is used to search for a specific class, telling if loading the engine would succeed. This is typically used to avoid loading optional modules.
         *
         * @param engineClassName engine class, used both as a hint to find it and to create the engine itself.  @return null if the engine is not available, an instance of the engine otherwise
         */
        private fun tryLoadEngine(config: JBakeConfiguration, db: ContentStore, engineClassName: String): AbstractTemplateEngine? {
            try {
                @Suppress("UNCHECKED_CAST")
                val engineClass = Class.forName(engineClassName, false, TemplateEngines::class.java.getClassLoader()) as Class<out AbstractTemplateEngine>
                val ctor: Constructor<out AbstractTemplateEngine> = engineClass.getConstructor(JBakeConfiguration::class.java, ContentStore::class.java)
                return ctor.newInstance(config, db)
            }
            catch (e: Throwable) {
                // Not all engines might be necessary, therefore only emit class loading issue with level warn.
                log.debug { "Template engine not available: $engineClassName" }
                return null
            }
        }

        private val log: Logger by logger()
    }
}
