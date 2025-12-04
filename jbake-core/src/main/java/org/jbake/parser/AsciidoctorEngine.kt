package org.jbake.parser

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.asciidoctor.jruby.AsciidoctorJRuby
import org.jbake.util.Logging.logger
import org.jbake.util.error
import org.slf4j.Logger
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Renders documents in the asciidoc format using the Asciidoctor engine.
 * Migrated to AsciidoctorJ 3.x API.
 */
@Suppress("UNCHECKED_CAST")
class AsciidoctorEngine : MarkupEngine() {

    @Volatile
    private var engine: Asciidoctor? = null
    private val engineLock = Any()

    // Cache for gemPath and requires options
    private var gemPath: String? = null
    private var requires: List<String>? = null

    /**
     * Convert Ruby objects from Asciidoctor to Java types.
     * This prevents serialization issues when storing documents.
     */
    private fun convertRubyToJava(value: Any?): Any? {
        if (value == null) return null

        // For Ruby objects, convert to string to avoid serialization issues
        val className = value::class.java.name
        if (className.startsWith("org.jruby.Ruby")) {
            // Skip certain types that can't be meaningfully converted
            if (className.contains("Symbol") ||
                className.contains("Class") ||
                className.contains("Module")) {
                return null
            }
            // Convert other Ruby objects to strings
            return runCatching { value.toString() }.getOrNull()
        }

        // Handle Java collections that might contain Ruby objects
        return when (value) {
            is Map<*, *> -> value.mapValues { convertRubyToJava(it.value) }.filter { it.value != null }
            is List<*> -> value.mapNotNull { convertRubyToJava(it) }
            else -> value
        }
    }

    private fun getEngine(options: Options): Asciidoctor {
        engine?.let { return it }

        synchronized(engineLock) {
            engine?.let { return it }
            log.info("Initializing Asciidoctor engine...")

            val newEngine = if (gemPath != null) {
                AsciidoctorJRuby.Factory.create(gemPath)
            } else {
                Asciidoctor.Factory.create()
            }

            requires?.forEach { newEngine.requireLibrary(it) }

            log.info("Asciidoctor engine initialized.")
            engine = newEngine
            return newEngine
        }
    }

    override fun processHeader(context: ParserContext) {
        val attributes = buildAttributes(context)
        val asciidoctor = getEngine(buildOptions(context, attributes))

        // In AsciidoctorJ 3.x, use loadFile with header_only option to get document header
        val headerOptions = Options.builder()
            .backend("html5")
            .safe(SafeMode.UNSAFE)
            .option("parse_header_only", true)
            .attributes(attributes)
            .baseDir(context.file.parentFile)
            .build()

        val document = asciidoctor.loadFile(context.file, headerOptions)
        val documentModel = context.documentModel

        // Get title from document
        val title = document.doctitle
        if (title != null && title.isNotEmpty()) {
            documentModel.title = title
        }

        // Get attributes from document
        log.info("=== Parsing attributes for document: {} ===", context.file.name)
        for ((key, value) in document.attributes) {
            val keyStr = key.toString()
            val valueClass = value?.javaClass?.name ?: "null"
            val valueStr = value?.toString() ?: "null"
            log.info("Attribute: {} = {} (class: {})", keyStr, valueStr, valueClass)

            when {
                keyStr.startsWith(JBAKE_PREFIX) -> {
                    val pKey = keyStr.substring(6)
                    val convertedValue = convertRubyToJava(value)
                    if (convertedValue is String)
                        storeHeaderValue(pKey, convertedValue, documentModel)
                    else if (convertedValue != null)
                        documentModel[pKey] = convertedValue
                }
                keyStr == REVDATE_KEY -> {
                    val convertedValue = convertRubyToJava(value)
                    if (convertedValue is String) {
                        val dateFormat: String = context.config.dateFormat!!
                        val df: DateFormat = SimpleDateFormat(dateFormat)
                        runCatching { context.date = (df.parse(convertedValue)) }
                            .onFailure { log.error("Unable to parse revdate. Expected $dateFormat", it) }
                    }
                }
                keyStr == "jbake-tags" -> {
                    val convertedValue = convertRubyToJava(value)
                    if (convertedValue is String)
                        context.setTags(convertedValue.split(",".toRegex()).dropLastWhile { it.isEmpty() })
                    else log.error { "Wrong value of 'jbake-tags'. Expected a String got '${getValueClassName(value)}'" }
                }
                else -> {
                    val convertedValue = convertRubyToJava(value)
                    if (convertedValue != null)
                        documentModel[keyStr] = convertedValue
                }
            }
        }
    }

    private fun getValueClassName(value: Any?) = value?.javaClass?.getCanonicalName() ?: "null"

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
        val attributes = buildAttributes(context)
        val options = buildOptions(context, attributes)
        val asciidoctor = getEngine(options)
        context.body = (asciidoctor.convert(context.body, options))
    }

    private fun buildAttributes(context: ParserContext): Attributes {
        val config = context.config
        val asciidoctorAttributes: MutableList<String> = config.asciidoctorAttributes

        // Build attributes using new API
        val attributesBuilder = Attributes.builder()

        // Add configured attributes
        for (attr in asciidoctorAttributes) {
            val parts = attr.split("=", limit = 2)
            if (parts.size == 2) {
                attributesBuilder.attribute(parts[0].trim(), parts[1].trim())
            } else {
                attributesBuilder.attribute(attr.trim(), "")
            }
        }

        if (config.exportAsciidoctorAttributes) {
            val prefix = config.attributesExportPrefixForAsciidoctor ?: ""

            val it: MutableIterator<String> = config.keys
            while (it.hasNext()) {
                val key = it.next()
                if (!key.startsWith("asciidoctor")) {
                    attributesBuilder.attribute(prefix + key.replace(".", "_"), config.get(key))
                }
            }
        }

        return attributesBuilder.build()
    }

    private fun buildOptions(context: ParserContext, attributes: Attributes): Options {
        val config = context.config

        // Build options using new API
        val optionsBuilder = Options.builder()
            .attributes(attributes)
            .backend("html5")
            .safe(SafeMode.UNSAFE)
            .baseDir(context.file.parentFile)

        for (optionKey in config.asciidoctorOptionKeys) {
            val optionValue = config.getAsciidoctorOption(optionKey)

            // Handle special gem path and requires options
            when {
                optionKey == OPT_GEM_PATH -> gemPath = optionValue.toString()
                optionKey == OPT_REQUIRES -> requires = optionValue.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                optionKey == "template_dirs" -> {
                    if (getAsList(optionValue).isNotEmpty())
                        optionsBuilder.templateDirs(*getAsList(optionValue).map { File(it) }.toTypedArray())
                }
                else -> optionValue.let { optionsBuilder.option(optionKey, it) }
            }
        }

        return optionsBuilder.build()
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
        const val JBAKE_PREFIX: String = "jbake-"
        const val REVDATE_KEY: String = "revdate"

        /** Comma-separated file paths to additional gems */
        private const val OPT_GEM_PATH = "gemPath"

        /** Comma-separated gem names */
        private const val OPT_REQUIRES = "requires"
    }

    private val log: Logger by logger()
}
