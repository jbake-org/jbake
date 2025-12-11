package org.jbake.parser

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.asciidoctor.jruby.AsciidoctorJRuby
import org.jbake.model.DocumentModel
import org.jbake.util.Logging.logger
import org.jbake.util.ValueTracer
import org.jbake.util.error
import org.jbake.util.trace
import org.slf4j.Logger
import java.io.File
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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

    private fun getOrCreateAsciidoctorEngine(options: Options): Asciidoctor {
        engine?.let { return it }

        synchronized(engineLock) {
            engine?.let { return it }
            log.info("Initializing Asciidoctor engine...")

            val newEngine = if (gemPath != null) {
                log.info("Creating AsciidoctorJRuby engine with gem path: $gemPath")
                AsciidoctorJRuby.Factory.create(gemPath)
            } else {
                log.info("Creating Asciidoctor engine")
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
        val asciidoctor = getOrCreateAsciidoctorEngine(buildOptions(context, attributes))
        val dateFormat: DateTimeFormatter = context.config.dateFormat?.let { DateTimeFormatter.ofPattern(it) } ?: DateTimeFormatter.ISO_LOCAL_DATE_TIME

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

        val authorName = document.getAttribute(AUTHOR_KEY)?.toString()?.takeIf { it.isNotBlank() }
        val authorEmail = document.getAttribute(AUTHOR_EMAIL_KEY)?.toString()?.takeIf { it.isNotBlank() }
        if (!authorName.isNullOrBlank()) {
            // Warn if author looks like a date (common mistake: date on line 2 instead of author)
            if (authorName.matches(DATE_PATTERN)) {
                log.warn("Author '$authorName' in '${context.file.name}' looks like a date. " +
                    "In Asciidoc, line 2 is the author line. If you meant to specify a date, use ':revdate: $authorName' attribute instead.")
            }
            documentModel[AUTHOR_KEY] = authorName
            authorEmail?.let {
                documentModel[AUTHOR_EMAIL_KEY] = it
                documentModel[EMAIL_KEY] = it  // For backwards compatibility
            }
        }
        val fallbackEmail = document.getAttribute(EMAIL_KEY)?.toString()?.takeIf { it.isNotBlank() }
        if (fallbackEmail != null && !documentModel.containsKey(EMAIL_KEY)) {
            // Use email as fallback for missing author_email.
            documentModel[EMAIL_KEY] = fallbackEmail
            if (!documentModel.containsKey(AUTHOR_EMAIL_KEY))
                documentModel[AUTHOR_EMAIL_KEY] = fallbackEmail
        }

        // Get title from document
        val title = document.doctitle
        if (title != null && title.isNotEmpty()) {
            documentModel.title = title
        }

        // Build set of JBake config keys (normalized with underscores) to identify exported attributes
        val exportedConfigKeys = collectExportedKeys(context)

        // Get attributes from document
        log.trace { "=== Parsing attributes for document: ${context.file.name} ===" }

        val relevantAttribs = document.attributes.filter { it.key !in SKIPPED_ATTRIBUTES }.toSortedMap()
        for ((key, value) in relevantAttribs) {

            val keyStr = key.toString()
            log.trace { "    ${keyStr.padEnd(32)} = '$value' (class: ${value?.javaClass?.name?.removePrefix("java.lang.")})" }

            processAttribute(keyStr, value, documentModel, context, dateFormat, exportedConfigKeys)
        }
        ValueTracer.trace("asciidoctor-header", documentModel, context.file.name)
    }

    private fun processAttribute(
        keyStr: String,
        value: Any?,
        documentModel: DocumentModel,
        context: ParserContext,
        dateFormat: DateTimeFormatter,
        exportedConfigKeys: Set<String>,
    ) {
        when {
            // JBake-specific attributes: store without prefix
            keyStr.startsWith(JBAKE_PREFIX) -> {
                val pKey = keyStr.substring(JBAKE_PREFIX.length)
                val convertedValue = convertRubyToJava(value)
                if (convertedValue is String)
                    storeHeaderValue(pKey, convertedValue, documentModel)
                else if (convertedValue != null)
                    documentModel[pKey] = convertedValue
            }
            // Revision date: special handling for date parsing
            keyStr == REVDATE_KEY -> {
                val convertedValue = convertRubyToJava(value)
                if (convertedValue is String) {
                    runCatching { context.date = OffsetDateTime.parse(convertedValue, dateFormat) }
                        .recoverCatching {
                            val localDate = LocalDate.parse(convertedValue, dateFormat)
                            context.date = localDate.atStartOfDay().atOffset(ZoneOffset.UTC)
                        }
                        .onFailure { log.error("Unable to parse revdate '$convertedValue'. Expected format: $dateFormat", it) }
                }
            }

            // JBake tags special case (may appear without jbake- prefix in some configs)
            keyStr == "jbake-tags" -> {
                val convertedValue = convertRubyToJava(value)
                if (convertedValue is String)
                    context.setTags(convertedValue.split(",".toRegex()).dropLastWhile { it.isEmpty() })
                else log.error { "Wrong value of 'jbake-tags'. Expected a String got '${getValueClassName(value)}'" }
            }

            // Skip author/email - already handled above with special logic
            keyStr in AUTHOR_ATTRIBUTES -> {}

            // Skip attributes exported from JBake config (already available via config)
            keyStr in exportedConfigKeys -> {}

            // Built-in Asciidoctor attributes: prefix with "asciidoc."
            keyStr in ASCIIDOCTOR_BUILTIN_ATTRIBUTES || keyStr.matches(ASCIIDOCTOR_BUILTIN_PATTERN) -> {
                val convertedValue = convertRubyToJava(value) ?: return
                documentModel["$EXPORT_PREFIX_ASCIIDOC$keyStr"] = convertedValue
            }

            // Custom document attributes (user-defined in the .adoc file): prefix with "doc_attr."
            else -> {
                val convertedValue = convertRubyToJava(value) ?: return
                documentModel["$EXPORT_PREFIX_DOC_ATTR$keyStr"] = convertedValue
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
        val asciidoctor = getOrCreateAsciidoctorEngine(options)
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
        private const val AUTHOR_KEY = "author"
        private const val AUTHOR_EMAIL_KEY = "author_email"
        private const val EMAIL_KEY = "email"

        /** Author-related attributes that get special handling (stored unprefixed) */
        private val AUTHOR_ATTRIBUTES = setOf(AUTHOR_KEY, AUTHOR_EMAIL_KEY, EMAIL_KEY)

        /** Comma-separated file paths to additional gems */
        private const val OPT_GEM_PATH = "gemPath"

        /** Comma-separated gem names */
        private const val OPT_REQUIRES = "requires"

        /** Prefix for built-in Asciidoctor attributes stored in the document model */
        private const val EXPORT_PREFIX_ASCIIDOC = "asciidoc."

        /** Prefix for custom document attributes (user-defined in the .adoc file) */
        private const val EXPORT_PREFIX_DOC_ATTR = "doc_attr."

        /** Attributes to skip entirely (too large, sensitive, or irrelevant) */
        private val SKIPPED_ATTRIBUTES = setOf("java_class_path", "sun_java_command", "surefire_test_class_path", "surefire_real_class_path")

        /** Regex pattern for dynamic Asciidoctor attributes (e.g., backend-html5, doctype-article, etc.) */
        private val ASCIIDOCTOR_BUILTIN_PATTERN = Regex("^(backend-|basebackend-|doctype-|filetype-|safe-mode-|author_|authorinitials_|email_|firstname_|lastname_|middlename_).*")

        /** Regex pattern to detect if author value looks like a date (common mistake: date on line 2) */
        private val DATE_PATTERN = Regex("^\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}.*")

        /** Known built-in Asciidoctor attributes (not user-defined, not JBake config) */
        private val ASCIIDOCTOR_BUILTIN_ATTRIBUTES = setOf(
            // Document info
            "docdate", "docdatetime", "docdir", "docfile", "docfilesuffix", "docname", "doctime", "doctitle", "doctype", "docyear",
            // Processing
            "asciidoctor", "asciidoctor-version", "backend", "basebackend", "embedded",
            "filetype", "htmlsyntax", "outfilesuffix", "safe-mode-level", "safe-mode-name",
            // Locale/time
            "localdate", "localdatetime", "localtime", "localyear",
            // Authoring
            "author", "authorinitials", "authorcount", /* "email", */ "firstname", "lastname", "middlename",
            "revdate", "revnumber", "revremark",
            // Section/TOC
            "idprefix", "idseparator", "sectids", "sectanchors", "sectlinks", "sectnums", "sectnumlevels",
            "toc", "toc-placement", "toc-title", "toclevels",
            // Captions and labels
            "appendix-caption", "appendix-refsig", "caution-caption", "chapter-refsig",
            "example-caption", "figure-caption", "important-caption", "last-update-label",
            "note-caption", "part-refsig", "section-refsig", "table-caption", "tip-caption",
            "untitled-label", "version-label", "warning-caption",
            // Misc built-ins
            "attribute-missing", "attribute-undefined", "iconsdir", "imagesdir", "stylesdir",
            "max-include-depth", "notitle", "prewrap", "source-highlighter", "stem", "user-home"
        )
    }

    private val log: Logger by logger()
}

private fun collectExportedKeys(context: ParserContext): Set<String> = buildSet {
    val prefix = context.config.attributesExportPrefixForAsciidoctor ?: ""
    val it = context.config.keys
    while (it.hasNext()) {
        val key = it.next()
        if (!key.startsWith("asciidoctor")) add(prefix + key.replace(".", "_"))
    }
}
