package org.jbake.parser

import org.asciidoctor.*
import org.asciidoctor.jruby.AsciidoctorJRuby
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * Renders documents in the asciidoc format using the Asciidoctor engine.
 *
 * TODO: Migrate to AsciidoctorJ 3.x and remove deprecated API usage.
 */
@Suppress("UNCHECKED_CAST")
class AsciidoctorEngine : MarkupEngine() {

    @Volatile
    private var engine: Asciidoctor? = null
    private val engineLock = Any()

    private fun getEngine(options: Options): Asciidoctor {
        engine?.let { return it }

        synchronized(engineLock) {
            engine?.let { return it }
            log.info("Initializing Asciidoctor engine...")

            val newEngine =
                // AsciidoctorJ deprecated Options.map(), but there is no alternative as of 2025. See: https://github.com/asciidoctor/asciidoctorj/issues/728
                @Suppress("DEPRECATION")
                if (options.map().containsKey(OPT_GEM_PATH))
                    AsciidoctorJRuby.Factory.create(options.map()[OPT_GEM_PATH].toString())
                else
                    Asciidoctor.Factory.create()

            @Suppress("DEPRECATION")
            if (options.map().containsKey(OPT_REQUIRES)) {
                val requires: Array<String> =
                    options.map()[OPT_REQUIRES].toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }.toTypedArray()
                requires.forEach { newEngine.requireLibrary(it) }
            }

            log.info("Asciidoctor engine initialized.")
            engine = newEngine
            return newEngine
        }
    }

    override fun processHeader(context: ParserContext) {
        val options = getAsciiDocOptionsAndAttributes(context)
        val asciidoctor = getEngine(options)
        @Suppress("DEPRECATION")
        val header = asciidoctor.readDocumentHeader(context.file)
        val documentModel = context.documentModel

        @Suppress("DEPRECATION")
        if (header.documentTitle != null) {
            documentModel.title = (header.documentTitle.combined)
        }

        @Suppress("DEPRECATION")
        val attributes = header.attributes
        for (attribute in attributes.entries) {
            val key: String = attribute.key!!
            val value = attribute.value

            if (hasJBakePrefix(key)) {
                val pKey = key.substring(6)
                if (canCastToString(value))
                    storeHeaderValue(pKey, value as String, documentModel)
                else documentModel[pKey] = value
            }

            if (hasRevdate(key) && canCastToString(value)) {
                val dateFormat: String = context.config.dateFormat!!
                val df: DateFormat = SimpleDateFormat(dateFormat)
                try {
                    context.date = (df.parse(value as String))
                } catch (e: ParseException) {
                    log.error("Unable to parse revdate. Expected {}", dateFormat, e)
                }
            }

            if (key == "jbake-tags") {
                if (canCastToString(value))
                    context.setTags((value as String).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                else log.error("Wrong value of 'jbake-tags'. Expected a String got '{}'", getValueClassName(value))
            }
            else documentModel[key] = attributes[key]!!
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
        @Suppress("DEPRECATION")
        val attributes = AttributesBuilder.attributes(asciidoctorAttributes.toTypedArray<String>())
        if (config.exportAsciidoctorAttributes) {
            val prefix = config.attributesExportPrefixForAsciidoctor

            val it: MutableIterator<String> = config.keys
            while (it.hasNext()) {
                val key = it.next()
                if (!key.startsWith("asciidoctor")) {
                    @Suppress("DEPRECATION")
                    attributes.attribute(prefix + key.replace(".", "_"), config.get(key))
                }
            }
        }

        val optionsSubset: MutableList<String> = config.asciidoctorOptionKeys
        @Suppress("DEPRECATION")
        val options = OptionsBuilder.options().attributes(attributes.get()).get()
        for (optionKey in optionsSubset) {
            val optionValue = config.getAsciidoctorOption(optionKey)
            if (optionKey == Options.TEMPLATE_DIRS) {
                val dirs = getAsList(optionValue)
                if (!dirs.isEmpty()) {
                    options.setTemplateDirs(dirs.toString())
                }
            }
            else options.setOption(optionKey, optionValue)
        }
        options.setBaseDir(context.file.getParentFile().absolutePath)
        options.setSafe(SafeMode.UNSAFE)
        return options
    }

    @Suppress("UNCHECKED_CAST")
    private fun getAsList(asciidoctorOption: Any?): MutableList<String> {
        val values: MutableList<String> = ArrayList()

        when (asciidoctorOption) {
            is MutableList<*> -> values.addAll(asciidoctorOption as MutableList<String>)
            is String -> values.add(asciidoctorOption)
        }
        return values
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AsciidoctorEngine::class.java)
        const val JBAKE_PREFIX: String = "jbake-"
        const val REVDATE_KEY: String = "revdate"

        /** Comma-separated file paths to additional gems */
        private const val OPT_GEM_PATH = "gemPath"

        /** Comma-separated gem names */
        private const val OPT_REQUIRES = "requires"
    }
}
