package org.jbake.parser

import org.apache.commons.io.IOUtils
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes.DOC_DATE
import org.jbake.model.ModelAttributes.DOC_TAGS
import org.jbake.model.ModelAttributes.FS_DOC_IS_CACHED_IN_DB
import org.jbake.util.Logging.logger
import org.jbake.util.trace
import org.json.simple.JSONValue
import org.slf4j.Logger
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant


/**
 * Base class for markup engine wrappers. A markup engine is responsible for rendering markup in a source file
 * and exporting the result into the [contents][ParserContext.documentModel] map.
 */
abstract class MarkupEngine : ParserEngine {

    private var configuration: JBakeConfiguration? = null

    /**
     * @return true if this markup engine has enough context to process this document. false otherwise
     */
    fun validate(context: ParserContext) = true

    /**
     * Processes the document header. Usually subclasses will parse the document body and look for
     * specific header metadata and export it into [contents][ParserContext.documentModel] map.
     */
    open fun processHeader(context: ParserContext) {}

    /**
     * Processes the body of the document. Usually, subclasses will parse the document body and render it,
     * exporting the result as [ParserContext.body].
     */
    open fun processBody(parserContext: ParserContext) {}

    /**
     * Parse the given file to extract as many infos as possible.
     * @return A map containing all infos. null indicates an error. TODO: Exception would be better.
     */
    override fun parse(config: JBakeConfiguration, file: File): DocumentModel? {
        this.configuration = config
        val fileContent = getFileContent(file, config.renderEncoding!!)

        if (fileContent.isEmpty()) return null

        val hasHeader = hasDocumentHeader(fileContent)
        val context = ParserContext(file, fileContent, config, hasHeader)

        // Read header from file.
        processDefaultHeader(context)
        // Then read engine specific headers.
        processHeader(context)

        setModelDefaultsIfNotSetInHeader(context)
        sanitizeTags(context)

        if (context.type.isEmpty() || context.status.isEmpty()) {
            log.warn("Parsing skipped - missing type or status value in header meta data for file: ${file.path}")
            return null
        }

        // Generate default body.
        processDefaultBody(context)

        if (!validate(context)) {
            log.error("Incomplete source file (${file.path}) for markup engine: ${javaClass.simpleName}")
            return null
        }
        // Eventually process body using specific engine.
        processBody(context)

        // TODO: post-parsing plugins to hook in here?
        return context.documentModel
    }

    private fun getFileContent(file: File, encoding: String): MutableList<String> {
        try {
            FileInputStream(file).use { `is` ->
                log.trace { "Read file '$file' with encoding '$encoding'" }
                val lines = IOUtils.readLines(`is`, encoding)
                if (!lines.isEmpty() && isUtf8WithBOM(encoding, lines[0])) {
                    log.warn("Removed BOM from file '$file' read with encoding '$encoding'")
                    lines[0] = lines[0].replace(UTF_8_BOM, "")
                }
                return lines
            }
        } catch (e: IOException) {
            log.error("Error while opening file $file", e)
            return mutableListOf()
        }
    }

    private fun isUtf8WithBOM(encoding: String, line: String): Boolean {
        return encoding == "UTF-8" && line.startsWith(UTF_8_BOM)
    }

    private fun sanitizeTags(context: ParserContext) {
        val tags = context.tags
            .map { sanitize(it) }
            .let { if (context.config.sanitizeTag) it.map { it.replace(" ", "-")  } else it }
        context.setTags(tags)
    }

    @OptIn(ExperimentalTime::class)
    private fun setModelDefaultsIfNotSetInHeader(context: ParserContext) {
        if (context.date == null)
            context.date =
                Instant.fromEpochMilliseconds(context.file.lastModified()).toJavaInstant()
                    .atZone(context.config.freemarkerTimeZone.toZoneId()).toOffsetDateTime()

        if (context.config.defaultStatus != null && context.status.isEmpty())
            context.setDefaultStatus()

        if (context.config.defaultType != null && context.type.isEmpty())
            context.setDefaultType()

        if (context.config.defaultAuthor != null)
            context.setDefaultAuthor()
    }

    /**
     * Checks if the file has a meta-data header.
     */
    private fun hasDocumentHeader(contents: MutableList<String>): Boolean {
        var headerValid = true
        var statusFound = false
        var typeFound = false

        if (!headerSeparatorDemarcatesHeader(contents)) return false

        for (line in contents) {
            if (hasHeaderSeparator(line)) { log.trace("Header separator found"); break }
            if (isTypeProperty(line)) { log.trace("Type property found"); typeFound = true }
            if (isStatusProperty(line)) { log.trace("Status property found"); statusFound = true }
            if (!line.isEmpty() && !line.contains("=")) {
                log.error("Property found without assignment [$line]")
                headerValid = false
            }
        }
        return headerValid && (statusFound || hasDefaultStatus()) && (typeFound || hasDefaultType())
    }

    private fun hasDefaultType() = !configuration!!.defaultType!!.isEmpty()

    private fun hasDefaultStatus() = !configuration!!.defaultStatus!!.isEmpty()


    /**
     * Checks if header separator demarcates end of metadata header.
     * @return true if header separator resides at end of metadata header, false if not
     */
    private fun headerSeparatorDemarcatesHeader(contents: MutableList<String>): Boolean {
        val index = contents.indexOf(configuration!!.headerSeparator)
        if (index == -1)
            return false

        // Get every line above header separator.
        return contents.subList(0, index).all { it.contains("=") || it.isEmpty() }
    }

    private fun hasHeaderSeparator(line: String)
        = sanitize(line) == configuration!!.headerSeparator

    private fun isStatusProperty(line: String)
        = sanitize(line).startsWith("status=")

    private fun isTypeProperty(line: String)
        = sanitize(line).startsWith("type=")

    /** Process the header of the file. */
    private fun processDefaultHeader(context: ParserContext) {
        if (!context.hasHeader()) return

        for (line in context.fileLines) {
            if (hasHeaderSeparator(line)) break
            processHeaderLine(line, context.documentModel)
        }
    }

    private fun processHeaderLine(line: String, content: DocumentModel) {
        val parts = line.split("=".toRegex(), limit = 2).toTypedArray()
        if (!line.isEmpty() && parts.size == 2)
            storeHeaderValue(parts[0], parts[1], content)
    }

    val formatter: DateTimeFormatter by lazy { configuration?.dateFormat?.let { DateTimeFormatter.ofPattern(it) } ?: DateTimeFormatter.ISO_LOCAL_DATE }

    fun storeHeaderValue(inputKey: String, inputValue: String, content: DocumentModel) {
        val key = sanitize(inputKey).lowercase()
        val value = sanitize(inputValue)

        when {
            key == DOC_DATE -> {
                runCatching {
                    // Try parsing as OffsetDateTime first (for full date-time with offset)
                    content.date = OffsetDateTime.parse(value, formatter)
                }
                .recoverCatching {
                    // If that fails, try parsing as LocalDate and convert to OffsetDateTime at start of day in system timezone
                    val localDate = java.time.LocalDate.parse(value, formatter)
                    content.date = localDate.atTime(0,0,0).atOffset(java.time.ZoneOffset.UTC)
                    // TODO: This was .atStartOfDay(), but that doesn't give seconds, so parsing failed. Investigate how to treat that later in processing.
                }
                .onFailure { e -> log.error("Unable to parse date $value with format '${configuration?.dateFormat}': ${e.message}") }
            }
            key == DOC_TAGS            -> content.tags = getTags(value)
            key == FS_DOC_IS_CACHED_IN_DB          -> content.cached = (value.toBoolean())
            isJson(value)    -> content[key] = JSONValue.parse(value)
            else                   -> content[key] = value
        }
    }

    private fun sanitize(part: String) = part.trim { it <= ' ' }

    private fun getTags(tagsPart: String): List<String>
        = tagsPart.split(",".toRegex()).dropLastWhile { it.isEmpty() }

    private fun isJson(part: String)
        = part.trim().let { it.startsWith("{") && it.endsWith("}") }


    /** Process the body of the file. */
    private fun processDefaultBody(context: ParserContext) {
        val headerSeparator = configuration!!.headerSeparator
        val body = context.fileLines
            .dropWhile { it != headerSeparator }
            .drop(1) // Skip the separator itself
            .joinToString("\n")
            .ifEmpty { context.fileLines.joinToString("\n") }

        context.body = body
    }

    private val UTF_8_BOM = "\uFEFF"
    private val log: Logger by logger()
}


/** This specific engine does nothing, meaning that the body is rendered as raw contents. */
class RawMarkupEngine : MarkupEngine()

/**
 * An internal rendering engine used to notify the user that the markup format he used requires an engine that couldn't be loaded.
 */
class ErrorEngine @JvmOverloads constructor(private val engineName: String = "unknown")
    : MarkupEngine()
{
    override fun processHeader(context: ParserContext) {
        val documentModel = context.documentModel
        documentModel.type = "post"
        documentModel.status = "published"
        documentModel.title = "Rendering engine missing"
        documentModel.date = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        documentModel.tags = emptyList()
    }

    override fun processBody(parserContext: ParserContext) {
        parserContext.body = "The markup engine [" + engineName + "] for [" + parserContext.file + "] couldn't be loaded"
    }
}
