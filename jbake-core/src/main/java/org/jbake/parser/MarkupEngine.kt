package org.jbake.parser

import org.apache.commons.io.IOUtils
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.json.simple.JSONValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Base class for markup engine wrappers. A markup engine is responsible for rendering markup in a source file
 * and exporting the result into the [contents][ParserContext.documentModel] map.
 *
 * This specific engine does nothing, meaning that the body is rendered as raw contents.
 */
abstract class MarkupEngine : ParserEngine {

    private var configuration: JBakeConfiguration? = null

    /**
     * Tests if this markup engine can process the document.
     *
     * @return true if this markup engine has enough context to process this document. false otherwise
     */
    fun validate(context: ParserContext) = true

    /**
     * Processes the document header. Usually subclasses will parse the document body and look for
     * specific header metadata and export it into [contents][ParserContext.getDocumentModel] map.
     */
    open fun processHeader(context: ParserContext) {}

    /**
     * Processes the body of the document. Usually subclasses will parse the document body and render it,
     * exporting the result using the [ParserContext.setBody] method.
     */
    open fun processBody(parserContext: ParserContext) {}

    /**
     * Parse given file to extract as much infos as possible
     *
     * @param file file to process
     * @return a map containing all infos. Returning null indicates an error, even if an exception would be better.
     */
    override fun parse(config: JBakeConfiguration, file: File): DocumentModel? {
        this.configuration = config
        val fileContent = getFileContent(file, config.renderEncoding!!)

        if (fileContent.isEmpty()) return null

        val hasHeader = hasDocumentHeader(fileContent)
        val context = ParserContext(file, fileContent, config, hasHeader)

        // read header from file
        processDefaultHeader(context)
        // then read engine specific headers
        processHeader(context)

        setModelDefaultsIfNotSetInHeader(context)
        sanitizeTags(context)

        if (context.type.isEmpty() || context.status.isEmpty()) {
            // output error
            log.warn("Parsing skipped (missing type or status value in header meta data) for file {}!", file)
            return null
        }

        // generate default body
        processDefaultBody(context)

        // eventually process body using specific engine
        if (validate(context)) {
            processBody(context)
        } else {
            log.error("Incomplete source file ({}) for markup engine: {}", file, javaClass.getSimpleName())
            return null
        }

        // TODO: post parsing plugins to hook in here?
        return context.documentModel
    }

    private fun getFileContent(file: File, encoding: String): MutableList<String> {
        try {
            FileInputStream(file).use { `is` ->
                log.debug("read file '{}' with encoding '{}'", file, encoding)
                val lines = IOUtils.readLines(`is`, encoding)
                if (!lines.isEmpty() && isUtf8WithBOM(encoding, lines[0])) {
                    log.warn("remove BOM from file '{}' read with encoding '{}'", file, encoding)
                    lines[0] = lines[0].replace(UTF_8_BOM, "")
                }
                return lines
            }
        } catch (e: IOException) {
            log.error("Error while opening file {}", file, e)
            return mutableListOf()
        }
    }

    private fun isUtf8WithBOM(encoding: String, line: String): Boolean {
        return encoding == "UTF-8" && line.startsWith(UTF_8_BOM)
    }

    private fun sanitizeTags(context: ParserContext) {
        val tags = context.tags
        for (i in tags.indices) {
            tags[i] = sanitize(tags[i])
            if (context.config.sanitizeTag) {
                tags[i] = tags[i].replace(" ", "-")
            }
        }
        context.setTags(tags)
    }

    private fun setModelDefaultsIfNotSetInHeader(context: ParserContext) {
        if (context.date == null) {
            context.date = (Date(context.file.lastModified()))
        }

        // default status has been set
        if (context.config.defaultStatus != null && context.status.isEmpty()) {
            // file hasn't got status so use default
            context.setDefaultStatus()
        }

        // default type has been set
        if (context.config.defaultType != null && context.type.isEmpty()) {
            // file hasn't got type so use default
            context.setDefaultType()
        }
    }

    /**
     * Checks if the file has a meta-data header.
     */
    private fun hasDocumentHeader(contents: MutableList<String>): Boolean {
        var headerValid = true
        var statusFound = false
        var typeFound = false

        if (!headerSeparatorDemarcatesHeader(contents)) {
            return false
        }

        for (line in contents) {
            if (hasHeaderSeparator(line)) { log.trace("Header separator found"); break }
            if (isTypeProperty(line)) { log.trace("Type property found"); typeFound = true }
            if (isStatusProperty(line)) { log.trace("Status property found"); statusFound = true }
            if (!line.isEmpty() && !line.contains("=")) {
                log.error("Property found without assignment [{}]", line)
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

    fun storeHeaderValue(inputKey: String, inputValue: String, content: DocumentModel) {
        val key = sanitize(inputKey)
        val value = sanitize(inputValue)

        when {
            key.equals(ModelAttributes.DATE, ignoreCase = true) -> {
                val df: DateFormat = SimpleDateFormat(configuration?.dateFormat ?: "yyyy-MM-dd")
                try {
                    content.date = (df.parse(value))
                }
                catch (e: ParseException) { log.error("unable to parse date {}", value) }
            }
            key.equals(ModelAttributes.TAGS, ignoreCase = true) -> content.tags = (getTags(value))
            key.equals(ModelAttributes.CACHED, ignoreCase = true) -> content.cached = (value.toBoolean())
            isJson(value) ->
                content[key] = JSONValue.parse(value)
            else ->
                content[key] = value
        }
    }

    private fun sanitize(part: String)= part.trim { it <= ' ' }

    private fun getTags(tagsPart: String): Array<String>
        = tagsPart.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    private fun isJson(part: String)
        = part.startsWith("{") && part.endsWith("}")


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
    private val log: Logger = LoggerFactory.getLogger(MarkupEngine::class.java)
}
