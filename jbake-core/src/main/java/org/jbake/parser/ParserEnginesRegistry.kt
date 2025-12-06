package org.jbake.parser
import org.jbake.app.FileUtil
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.util.Logging
import org.jbake.util.Logging.logger
import org.jbake.util.warn
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.*

/*
 * TBD: Add support for more markup languages:
 *  - reStructuredText (reST) - The de facto standard for the Python ecosystem
 *  - Wiki Markup (MediaWiki/GitHub Wiki)
 *  - Org-mode - popular within the Emacs text editor community
 *  - Textile - An older, more complex lightweight markup language
 */

interface ParserEngine {
    /**
     * Parse a given file and transform to a model used by [MarkdownEngine] to render the file content.
     *
     * @return A model representation of the given file. NULLABLE. TBD: Have a NullDocumentModel to avoid nulls?
     */
    fun parse(config: JBakeConfiguration, file: File): DocumentModel?
}


class Parser(private val config: JBakeConfiguration) {

    fun processFile(inputFile: File): DocumentModel? {
        val fileExtension = FileUtil.fileExt(inputFile)
        val engine = ParserEnginesRegistry.get(fileExtension)
            ?: run {
                log.error("Unable to find suitable markup engine for $inputFile")
                return null
            }
        return engine.parse(config, inputFile)
    }

    private val log: Logger by logger()
}


/**
 * A singleton class giving access to markup engines. Markup engines are loaded based on classpath.
 * New engines may be registered either at runtime (not recommended) or by putting a descriptor file on classpath (recommended).
 *
 * The descriptor file must be found in *META-INF* directory and named
 * *org.jbake.parser.MarkupEngines.properties*. The format of the file:
 * ```
 * org.jbake.parser.RawMarkupEngine=html<br></br>
 * org.jbake.parser.AsciidoctorEngine=ad,adoc,asciidoc<br></br>
 * org.jbake.parser.MarkdownEngine=md<br></br>
 * ```
  * ...where the key is the class of the engine (must extend [MarkupEngine] and have a no-arg constructor)
 * and the value is a comma-separated list of file extensions that this engine is capable of processing.
 *
 * Markup engines are singletons, so are typically used to initialize the underlying rendering engines.
 * They **must not** store specific information of a currently processed file (use [the parser context][ParserContext] for that).
 *
 * This class loads and registers the engines only if they are found on classpath.
 */
class ParserEnginesRegistry private constructor() {

    private val parsers: MutableMap<String, ParserEngine?> = HashMap()

    private fun registerEngine(fileExtension: String, markupEngine: ParserEngine) {
        val old = parsers.put(fileExtension, markupEngine)
        if (old != null)
            log.warn { "Registered a markup engine for extension [.$fileExtension] but another one was already defined: $old" }
    }

    private fun getEngine(fileExtension: String): ParserEngine? = parsers[fileExtension]

    companion object {
        // Logger must be declared first, before any code that uses it
        private val log: Logger by Logging.logger()

        private val INSTANCE: ParserEnginesRegistry = ParserEnginesRegistry()

        init {
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
         * This method is used to search for a specific class, telling if loading the engine would succeed.
         * This is typically used to avoid loading optional modules.
         *
         * @param engineClassName engine class, used both as a hint to find it and to create the engine itself.
         * @return null if the engine is not available, an instance of the engine otherwise
         */
        private fun tryLoadEngine(engineClassName: String): ParserEngine? {
            try {
                val engineClass = Class.forName(engineClassName, false, ParserEnginesRegistry::class.java.getClassLoader()) as Class<out ParserEngine>
                return engineClass.getDeclaredConstructor().newInstance()
            }
            catch (e: Exception) {
                when (e) {
                    is ClassNotFoundException,
                    is NoClassDefFoundError,
                    is IllegalAccessException,
                    is InstantiationException -> return ErrorEngine(engineClassName)
                    is NoSuchMethodException,
                    is InvocationTargetException -> { log.error("unable to instantiate ParserEngine $engineClassName") }
                }
                return null
            }
        }

        /**
         * This method is used internally to load markup engines. Markup engines are found using descriptor files on classpath, so
         * adding an engine is as easy as adding a jar on classpath with the descriptor file included.
         */
        private fun loadEngines() {
            try {
                val cl = ParserEnginesRegistry::class.java.getClassLoader()
                val resources = cl.getResources("META-INF/org.jbake.parser.MarkupEngines.properties")
                while (resources.hasMoreElements()) {
                    val url = resources.nextElement()
                    val props = Properties()
                    props.load(url.openStream())
                    for (entry in props.entries) {
                        val className = entry.key as String
                        val extensions: Array<String> =
                            (entry.value as String).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        registerEngine(className, *extensions)
                    }
                }
            }
            catch (e: IOException) { log.error("Error loading Engines: ${e.message}", e) }
        }

        private fun registerEngine(className: String, vararg extensions: String) {
            val engine = tryLoadEngine(className) ?: return

            for (extension in extensions)
                register(extension, engine)

            if (engine is ErrorEngine)
                log.warn { "Unable to load a suitable rendering engine for extensions $extensions" }
        }
    }
}

