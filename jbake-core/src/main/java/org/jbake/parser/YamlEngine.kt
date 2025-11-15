package org.jbake.parser

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class YamlEngine : MarkupEngine() {
    /**
     * Parses the YAML file and ensures the output is always a Map.
     *
     * @param file
     * @return
     */
    private fun parseFile(file: File): DocumentModel {
        val model = DocumentModel()
        val yaml = Yaml()
        try {
            FileInputStream(file).use { `is` ->
                val result = yaml.load<Any>(`is`)
                if (result is MutableList<*>) {
                    model.put("data", result)
                } else if (result is MutableMap<*, *>) {
                    model.putAll(result)
                } else {
                    LOGGER.warn("Unexpected result [{}] while parsing YAML file {}", result.javaClass, file)
                }
            }
        } catch (e: IOException) {
            LOGGER.error("Error while parsing YAML file {}", file, e)
        }
        return model
    }

    override fun parse(config: JBakeConfiguration?, file: File): DocumentModel {
        return parseFile(file)
    }

    /**
     * This method implements the contract allowing use of Yaml files as content files
     *
     * @param context the parser context
     */
    override fun processHeader(context: ParserContext) {
        val fileContents = parseFile(context.getFile())
        val documentModel = context.getDocumentModel()

        for (key in fileContents.keys) {
            if (hasJBakePrefix(key)) {
                val pKey = key.substring(6)
                documentModel.put(pKey, fileContents.get(key))
            } else {
                documentModel.put(key, fileContents.get(key))
            }
        }
    }

    /**
     * This method implements the contract allowing use of Yaml files as content files. As such there is
     * no body for Yaml files so this method just sets an empty String as the body.
     *
     * @param context the parser context
     */
    override fun processBody(context: ParserContext) {
        context.setBody("")
    }

    private fun hasJBakePrefix(key: String): Boolean {
        return key.startsWith(JBAKE_PREFIX)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(YamlEngine::class.java)

        const val JBAKE_PREFIX: String = "jbake-"
    }
}
