package org.jbake.parser

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.slf4j.Logger
import org.jbake.util.Logging.logger
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
        val yaml = Yaml()
        try {
            FileInputStream(file).use { inputStream ->

                when (val result = yaml.load<Any>(inputStream)) {
                    is MutableList<*> -> model.put("data", result)
                    is MutableMap<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val map = result as? Map<String, Any> ?: emptyMap()
                        model.putAll(map)
                    }
                    else -> log.warn("Unexpected result [{}] while parsing YAML file {}", result.javaClass, file)
                }
            }
        } catch (e: IOException) {
            log.error("Error while parsing YAML file {}", file, e)
        }
        return model
    }

    override fun parse(config: JBakeConfiguration, file: File): DocumentModel {
        return parseFile(file)
    }

    /**
     * This method implements the contract allowing use of Yaml files as content files
     *
     */
    override fun processHeader(context: ParserContext) {
        val fileContents = parseFile(context.file)
        val documentModel = context.documentModel

        for (key in fileContents.keys) {
            if (hasJBakePrefix(key)) {
                val pKey = key.substring(6)
                documentModel[pKey] = fileContents.get(key) ?: continue
            } else {
                documentModel[key] = fileContents.get(key) ?: continue
            }
        }
    }

    /**
     * This method implements the contract allowing use of Yaml files as content files.
     * As such there is no body for Yaml files so this method just sets an empty String as the body.
     */
    override fun processBody(parserContext: ParserContext) {
        parserContext.body = ""
    }

    private fun hasJBakePrefix(key: String): Boolean {
        return key.startsWith(JBAKE_PREFIX)
    }

    val JBAKE_PREFIX: String = "jbake-"
    private val log: Logger by logger()
}
