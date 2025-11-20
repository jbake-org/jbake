package org.jbake.parser

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.Configuration
import org.apache.commons.io.IOUtils
import org.jbake.app.configuration.DefaultJBakeConfiguration
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
 * Base class for markup engine wrappers. A markup engine is responsible for rendering
 * markup in a source file and exporting the result into the [contents][ParserContext.getDocumentModel] map.
 *
 *
 * This specific engine does nothing, meaning that the body is rendered as raw contents.
 *
 * @author Cédric Champeau
 */
abstract class MarkupEngine : ParserEngine {

    private var configuration: JBakeConfiguration? = null

    /**
     * Tests if this markup engine can process the document.
     *
     * @param context the parser context
     * @return true if this markup engine has enough context to process this document. false otherwise
     */
    fun validate(context: ParserContext): Boolean {
        return true
    }

    /**
     * Processes the document header. Usually subclasses will parse the document body and look for
     * specific header metadata and export it into [contents][ParserContext.getDocumentModel] map.
     *
     * @param context the parser context
     */
    open fun processHeader(context: ParserContext) {
    }

    /**
     * Processes the body of the document. Usually subclasses will parse the document body and render
     * it, exporting the result using the [ParserContext.setBody] method.
     *
     * @param context the parser context
     */
    open fun processBody(context: ParserContext) {
    }

    override fun parse(config: Configuration, file: File, contentPath: String): MutableMap<String, Any> {
        return parse(DefaultJBakeConfiguration((config as CompositeConfiguration?)!!), file)
    }

    /**
     * Parse given file to extract as much infos as possible
     *
     * @param file file to process
     * @return a map containing all infos. Returning null indicates an error, even if an exception would be better.
     */
    override fun parse(config: JBakeConfiguration, file: File): DocumentModel? {
        this.configuration = config
        val fileContent = getFileContent(file, config.renderEncoding!!)

        if (fileContent.isEmpty()) {
            return null
        }

        val hasHeader = hasHeader(fileContent)
        val context = ParserContext(file, fileContent, config, hasHeader)

        // read header from file
        processDefaultHeader(context)
        // then read engine specific headers
        processHeader(context)

        setModelDefaultsIfNotSetInHeader(context)
        sanitizeTags(context)

        if (context.getType().isEmpty() || context.getStatus().isEmpty()) {
            // output error
            LOGGER.warn("Parsing skipped (missing type or status value in header meta data) for file {}!", file)
            return null
        }

        // generate default body
        processDefaultBody(context)

        // eventually process body using specific engine
        if (validate(context)) {
            processBody(context)
        } else {
            LOGGER.error("Incomplete source file ({}) for markup engine: {}", file, javaClass.getSimpleName())
            return null
        }

        // TODO: post parsing plugins to hook in here?
        return context.getDocumentModel()
    }

    private fun getFileContent(file: File, encoding: String): MutableList<String> {
        try {
            FileInputStream(file).use { `is` ->
                LOGGER.debug("read file '{}' with encoding '{}'", file, encoding)
                val lines = IOUtils.readLines(`is`, encoding)
                if (!lines.isEmpty() && isUtf8WithBOM(encoding, lines.get(0))) {
                    LOGGER.warn("remove BOM from file '{}' read with encoding '{}'", file, encoding)
                    lines.set(0, lines.get(0).replace(UTF_8_BOM, ""))
                }
                return lines
            }
        } catch (e: IOException) {
            LOGGER.error("Error while opening file {}", file, e)
            return mutableListOf<String>()
        }
    }

    private fun isUtf8WithBOM(encoding: String, line: String): Boolean {
        return encoding == "UTF-8" && line.startsWith(UTF_8_BOM)
    }

    private fun sanitizeTags(context: ParserContext) {
        if (context.getTags() != null) {
            val tags = context.getTags() as Array<String>
            for (i in tags.indices) {
                tags[i] = sanitize(tags[i]!!)
                if (context.getConfig().sanitizeTag) {
                    tags[i] = tags[i]!!.replace(" ", "-")
                }
            }
            context.setTags(tags)
        }
    }

    private fun setModelDefaultsIfNotSetInHeader(context: ParserContext) {
        if (context.getDate() == null) {
            context.setDate(Date(context.getFile().lastModified()))
        }

        // default status has been set
        if (context.getConfig().defaultStatus != null && context.getStatus().isEmpty()) {
            // file hasn't got status so use default
            context.setDefaultStatus()
        }

        // default type has been set
        if (context.getConfig().defaultType != null && context.getType().isEmpty()) {
            // file hasn't got type so use default
            context.setDefaultType()
        }
    }

    /**
     * Checks if the file has a meta-data header.
     *
     * @param contents Contents of file
     * @return true if header exists, false if not
     */
    private fun hasHeader(contents: MutableList<String>): Boolean {
        var headerValid = true
        var statusFound = false
        var typeFound = false

        if (!headerSeparatorDemarcatesHeader(contents)) {
            return false
        }

        for (line in contents) {
            if (hasHeaderSeparator(line)) {
                LOGGER.debug("Header separator found")
                break
            }
            if (isTypeProperty(line)) {
                LOGGER.debug("Type property found")
                typeFound = true
            }

            if (isStatusProperty(line)) {
                LOGGER.debug("Status property found")
                statusFound = true
            }
            if (!line.isEmpty() && !line.contains("=")) {
                LOGGER.error("Property found without assignment [{}]", line)
                headerValid = false
            }
        }
        return headerValid && (statusFound || hasDefaultStatus()) && (typeFound || hasDefaultType())
    }

    private fun hasDefaultType(): Boolean {
        return !configuration!!.defaultType!!.isEmpty()
    }

    private fun hasDefaultStatus(): Boolean {
        return !configuration!!.defaultStatus!!.isEmpty()
    }

    /**
     * Checks if header separator demarcates end of metadata header.
     * @return true if header separator resides at end of metadata header, false if not
     */
    private fun headerSeparatorDemarcatesHeader(contents: MutableList<String>): Boolean {
        val index = contents.indexOf(configuration!!.headerSeparator)
        if (index == -1)
            return false

        // Get every line above header separator.
        val subContents = contents.subList(0, index)

        for (line in subContents) {
            // header should only contain empty lines or lines with '=' in
            if (!line.contains("=") && !line.isEmpty()) {
                return false
            }
        }
        return true
    }

    private fun hasHeaderSeparator(line: String): Boolean {
        return sanitize(line) == configuration!!.headerSeparator
    }

    private fun isStatusProperty(line: String): Boolean {
        return sanitize(line).startsWith("status=")
    }

    private fun isTypeProperty(line: String): Boolean {
        return sanitize(line).startsWith("type=")
    }

    /**
     * Process the header of the file.
     *
     * @param context the parser context
     */
    private fun processDefaultHeader(context: ParserContext) {
        if (context.hasHeader()) {
            for (line in context.getFileLines()) {
                if (hasHeaderSeparator(line)) {
                    break
                }
                processHeaderLine(line, context.getDocumentModel())
            }
        }
    }

    private fun processHeaderLine(line: String, content: DocumentModel) {
        val parts: Array<String> = line.split("=".toRegex(), limit = 2).toTypedArray()
        if (!line.isEmpty() && parts.size == 2) {
            storeHeaderValue(parts[0]!!, parts[1]!!, content)
        }
    }

    fun storeHeaderValue(inputKey: String, inputValue: String, content: DocumentModel) {
        val key = sanitize(inputKey)
        val value = sanitize(inputValue)

        if (key.equals(ModelAttributes.DATE, ignoreCase = true)) {
            val df: DateFormat = SimpleDateFormat(configuration!!.dateFormat)
            try {
                val date = df.parse(value)
                content.setDate(date)
            } catch (e: ParseException) {
                LOGGER.error("unable to parse date {}", value)
            }
        } else if (key.equals(ModelAttributes.TAGS, ignoreCase = true)) {
            content.setTags(getTags(value))
        } else if (key.equals(ModelAttributes.CACHED, ignoreCase = true)) {
            content.setCached(value.toBoolean())
        } else if (isJson(value)) {
            content.put(key, JSONValue.parse(value))
        } else {
            content.put(key, value)
        }
    }

    private fun sanitize(part: String): String {
        return part.trim { it <= ' ' }
    }

    private fun getTags(tagsPart: String): Array<String> {
        return tagsPart.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    private fun isJson(part: String): Boolean {
        return part.startsWith("{") && part.endsWith("}")
    }

    /**
     * Process the body of the file.
     *
     * @param context the parser context
     */
    private fun processDefaultBody(context: ParserContext) {
        val body = StringBuilder()
        var inBody = false
        for (line in context.getFileLines()) {
            if (inBody) {
                body.append(line).append("\n")
            }
            if (line == configuration!!.headerSeparator) {
                inBody = true
            }
        }

        if (body.length == 0) {
            for (line in context.fileLines) {
                body.append(line).append("\n")
            }
        }
        context.setBody(body.toString())
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MarkupEngine::class.java)
        private const val UTF_8_BOM = "\uFEFF"
    }
}
