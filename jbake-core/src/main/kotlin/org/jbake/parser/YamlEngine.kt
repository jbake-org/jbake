package org.jbake.parser

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.util.Logging.logger
import org.jbake.util.warn
import org.slf4j.Logger
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class YamlEngine : MarkupEngine() {

    /**
     * Parses the YAML file and ensures the output is always a Map.
     */
    private fun parseFile(file: File): DocumentModel {
        val model = DocumentModel()
        try {
            FileInputStream(file).use { inputStream ->
                when (val result = Yaml().load<Any>(inputStream)) {
                    is MutableList<*> -> model.put("data", result)
                    is MutableMap<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val map = result as? Map<String, Any> ?: emptyMap()
                        model.putAll(map)
                    }
                    else -> log.warn { "Unexpected result [${result.javaClass}] while parsing YAML file $file" }
                }
            }
        }
        catch (e: IOException) { log.error("Error while parsing YAML file $file", e) }
        return model
    }

    override fun parse(config: JBakeConfiguration, file: File): DocumentModel = parseFile(file)

    /**
     * Implements the contract allowing use of Yaml files as content files
     */
    override fun processHeader(context: ParserContext) {
        val fileContents = parseFile(context.file)

        for (key in fileContents.keys) {
            val key_ = if (hasJBakePrefix(key)) key.substring(6) else key
            context.documentModel[key_] = fileContents.get(key) ?: continue
        }
    }

    /**
     * Implements the contract allowing use of Yaml files as content files.
     * As such there is no body for Yaml files, it just sets an empty String as the body.
     */
    override fun processBody(parserContext: ParserContext) {
        parserContext.body = ""
    }

    private fun hasJBakePrefix(key: String): Boolean = key.startsWith(JBAKE_PREFIX)

    private val JBAKE_PREFIX: String = "jbake-"
    private val log: Logger by logger()
}
