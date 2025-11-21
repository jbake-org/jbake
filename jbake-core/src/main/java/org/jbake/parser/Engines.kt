package org.jbake.parser

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 *
 * A singleton class giving access to markup engines. Markup engines are loaded based on classpath.
 * New engines may be registered either at runtime (not recommanded) or by putting a descriptor file
 * on classpath (recommanded).
 *
 *
 * The descriptor file must be found in *META-INF* directory and named
 * *org.jbake.parser.MarkupEngines.properties*. The format of the file is easy:
 * `
 * org.jbake.parser.RawMarkupEngine=html<br></br>
 * org.jbake.parser.AsciidoctorEngine=ad,adoc,asciidoc<br></br>
 * org.jbake.parser.MarkdownEngine=md<br></br>
` *
 *
 * where the key is the class of the engine (must extend [MarkupEngine] and have a no-arg
 * constructor and the value is a comma-separated list of file extensions that this engine is capable of proceeding.
 *
 *
 * Markup engines are singletons, so are typically used to initialize the underlying renderning engines. They
 * **must not** store specific information of a currently processed file (use [the parser context][ParserContext]
 * for that).
 *
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered. This allows
 * JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better
 * fit for embedding.
 *
 * @author Cédric Champeau
 */
class Engines private constructor() {
    private val parsers: MutableMap<String, ParserEngine?>


    init {
        parsers = HashMap<String, ParserEngine?>()
    }

    private fun registerEngine(fileExtension: String, markupEngine: ParserEngine) {
        val old = parsers.put(fileExtension, markupEngine)
        if (old != null) {
            log.warn(
                "Registered a markup engine for extension [.{}] but another one was already defined: {}",
                fileExtension,
                old
            )
        }
    }

    private fun getEngine(fileExtension: String): ParserEngine? {
        return parsers.get(fileExtension)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(Engines::class.java)
        private val INSTANCE: Engines

        init {
            INSTANCE = Engines()
            loadEngines()
        }

        fun get(fileExtension: String): ParserEngine? {
            return INSTANCE.getEngine(fileExtension)
        }

        fun register(fileExtension: String, engine: ParserEngine) {
            INSTANCE.registerEngine(fileExtension, engine)
        }

        val recognizedExtensions: MutableSet<String>
            get() = Collections.unmodifiableSet<String>(INSTANCE.parsers.keys)

        /**
         * This method is used to search for a specific class, telling if loading the engine would succeed. This is
         * typically used to avoid loading optional modules.
         *
         * @param engineClassName engine class, used both as a hint to find it and to create the engine itself.
         * @return null if the engine is not available, an instance of the engine otherwise
         */
        private fun tryLoadEngine(engineClassName: String?): ParserEngine? {
            try {
                val engineClass = Class.forName(
                    engineClassName,
                    false,
                    Engines::class.java.getClassLoader()
                ) as Class<out ParserEngine?>
                return engineClass.getDeclaredConstructor().newInstance()
            } catch (e: ClassNotFoundException) {
                return ErrorEngine(engineClassName)
            } catch (e: NoClassDefFoundError) {
                return ErrorEngine(engineClassName)
            } catch (e: IllegalAccessException) {
                return ErrorEngine(engineClassName)
            } catch (e: InstantiationException) {
                return ErrorEngine(engineClassName)
            } catch (e: NoSuchMethodException) {
                log.error("unable to instantiate ParserEngine {}", engineClassName)
            } catch (e: InvocationTargetException) {
                log.error("unable to instantiate ParserEngine {}", engineClassName)
            }
            return null
        }

        /**
         * This method is used internally to load markup engines. Markup engines are found using descriptor files on classpath, so
         * adding an engine is as easy as adding a jar on classpath with the descriptor file included.
         */
        private fun loadEngines() {
            try {
                val cl = Engines::class.java.getClassLoader()
                val resources = cl.getResources("META-INF/org.jbake.parser.MarkupEngines.properties")
                while (resources.hasMoreElements()) {
                    val url = resources.nextElement()
                    val props = Properties()
                    props.load(url.openStream())
                    for (entry in props.entries) {
                        val className = entry.key as String?
                        val extensions: Array<String> =
                            (entry.value as String).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        registerEngine(className, *extensions)
                    }
                }
            } catch (e: IOException) {
                log.error("Error loading Engines", e)
            }
        }

        private fun registerEngine(className: String?, vararg extensions: String?) {
            val engine: ParserEngine? = tryLoadEngine(className)
            if (engine != null) {
                for (extension in extensions) {
                    if (extension != null) {
                        register(extension, engine)
                    }
                }
                if (engine is ErrorEngine) {
                    log.warn("Unable to load a suitable rendering engine for extensions {}", extensions as Any)
                }
            }
        }
    }
}
