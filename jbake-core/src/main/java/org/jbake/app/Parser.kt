package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.parser.Engines
import org.jbake.parser.ParserEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Parses a File for content.
 *
 * @author Jonathan Bullock [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
 */
class Parser
/**
 * Creates a new instance of Parser.
 *
 * @param config Project configuration
 */(private val config: JBakeConfiguration?) {
    /**
     * Process the file by parsing the contents.
     *
     * @param file File input for parsing
     * @return The contents of the file
     */
    fun processFile(file: File): DocumentModel? {
        val engine: ParserEngine? = Engines.Companion.get(FileUtil.fileExt(file))
        if (engine == null) {
            LOGGER.error("Unable to find suitable markup engine for {}", file)
            return null
        }

        return engine.parse(config, file)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Parser::class.java)
    }
}
