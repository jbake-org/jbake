package org.jbake.parser

import org.asciidoctor.*
import org.asciidoctor.jruby.AsciidoctorJRuby
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Renders documents in the asciidoc format using the Asciidoctor engine.
 *
 * @author Cédric Champeau
 */
class AsciidoctorEngine : MarkupEngine() {
    private val lock = ReentrantReadWriteLock()

    private var engine: Asciidoctor? = null

    init {
        val engineClass: Class<*> = checkNotNull(Asciidoctor::class.java)
    }

    private fun getEngine(options: Options): Asciidoctor {
        try {
            lock.readLock().lock()
            if (engine == null) {
                lock.readLock().unlock()
                try {
                    lock.writeLock().lock()
                    if (engine == null) {
                        log.info("Initializing Asciidoctor engine...")
                        if (options.map().containsKey(OPT_GEM_PATH)) {
                            engine = AsciidoctorJRuby.Factory.create(options.map().get(OPT_GEM_PATH).toString())
                        } else {
                            engine = Asciidoctor.Factory.create()
                        }

                        if (options.map().containsKey(OPT_REQUIRES)) {
                            val requires: Array<String> =
                                options.map().get(OPT_REQUIRES).toString().split(",".toRegex())
                                    .dropLastWhile { it.isEmpty() }.toTypedArray()
                            if (requires.size != 0) {
                                for (require in requires) {
                                    engine!!.requireLibrary(require)
                                }
                            }
                        }

                        log.info("Asciidoctor engine initialized.")
                    }
                } finally {
                    lock.readLock().lock()
                    lock.writeLock().unlock()
                }
            }
        } finally {
            lock.readLock().unlock()
        }
        return engine!!
    }

    override fun processHeader(context: ParserContext) {
        val options = getAsciiDocOptionsAndAttributes(context)
        val asciidoctor = getEngine(options)
        val header = asciidoctor.readDocumentHeader(context.file)
        val documentModel = context.documentModel
        if (header.documentTitle != null) {
            documentModel.title = (header.documentTitle.combined)
        }
        val attributes = header.attributes
        for (attribute in attributes.entries) {
            val key: String = attribute.key!!
            val value = attribute.value

            if (hasJBakePrefix(key)) {
                val pKey = key.substring(6)
                if (canCastToString(value)) {
                    storeHeaderValue(pKey, value as String, documentModel)
                } else {
                    documentModel.put(pKey, value)
                }
            }
            if (hasRevdate(key) && canCastToString(value)) {
                val dateFormat: String = context.config.dateFormat!!
                val df: DateFormat = SimpleDateFormat(dateFormat)
                try {
                    val date = df.parse(value as String)
                    context.date = (date)
                } catch (e: ParseException) {
                    log.error("Unable to parse revdate. Expected {}", dateFormat, e)
                }
            }
            if (key == "jbake-tags") {
                if (canCastToString(value)) {
                    context.setTags((value as String).split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray())
                } else {
                    log.error("Wrong value of 'jbake-tags'. Expected a String got '{}'", getValueClassName(value))
                }
            } else {
                documentModel.put(key, attributes.get(key)!!)
            }
        }
    }

    private fun canCastToString(value: Any?): Boolean {
        return value is String
    }

    private fun getValueClassName(value: Any?): String? {
        return if (value == null) "null" else value.javaClass.getCanonicalName()
    }

    private fun hasRevdate(key: String): Boolean {
        return key == REVDATE_KEY
    }

    private fun hasJBakePrefix(key: String): Boolean {
        return key.startsWith(JBAKE_PREFIX)
    }

    // TODO: write tests with options and attributes
    override fun processBody(parserContext: ParserContext) {
        val body = StringBuilder(parserContext.body.length)
        if (!parserContext.hasHeader()) {
            for (line in parserContext.fileLines) {
                body.append(line).append("\n")
            }
            parserContext.body = (body.toString())
        }
        processAsciiDoc(parserContext)
    }

    private fun processAsciiDoc(context: ParserContext) {
        val options = getAsciiDocOptionsAndAttributes(context)
        val asciidoctor = getEngine(options)
        context.body = (asciidoctor.convert(context.body, options))
    }

    private fun getAsciiDocOptionsAndAttributes(context: ParserContext): Options {
        val config = context.config
        val asciidoctorAttributes: MutableList<String> = config.asciidoctorAttributes
        val attributes = AttributesBuilder.attributes(asciidoctorAttributes.toTypedArray<String>())
        if (config.exportAsciidoctorAttributes) {
            val prefix = config.attributesExportPrefixForAsciidoctor

            val it: MutableIterator<String> = config.keys
            while (it.hasNext()) {
                val key = it.next()
                if (!key.startsWith("asciidoctor")) {
                    attributes.attribute(prefix + key.replace(".", "_"), config.get(key))
                }
            }
        }

        val optionsSubset: MutableList<String> = config.asciidoctorOptionKeys
        val options = OptionsBuilder.options().attributes(attributes.get()).get()
        for (optionKey in optionsSubset) {
            val optionValue = config.getAsciidoctorOption(optionKey)
            if (optionKey == Options.TEMPLATE_DIRS) {
                val dirs = getAsList(optionValue)
                if (!dirs.isEmpty()) {
                    options.setTemplateDirs(dirs.toString())
                }
            } else {
                options.setOption(optionKey, optionValue)
            }
        }
        options.setBaseDir(context.file.getParentFile().getAbsolutePath())
        options.setSafe(SafeMode.UNSAFE)
        return options
    }

    private fun getAsList(asciidoctorOption: Any?): MutableList<String> {
        val values: MutableList<String> = ArrayList<String>()

        if (asciidoctorOption is MutableList<*>) {
            values.addAll(asciidoctorOption as MutableList<String>)
        } else if (asciidoctorOption is String) {
            values.add(asciidoctorOption.toString())
        }
        return values
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AsciidoctorEngine::class.java)
        const val JBAKE_PREFIX: String = "jbake-"
        const val REVDATE_KEY: String = "revdate"

        /* comma separated file paths to additional gems */
        private const val OPT_GEM_PATH = "gemPath"

        /* comma separated gem names */
        private const val OPT_REQUIRES = "requires"
    }
}
