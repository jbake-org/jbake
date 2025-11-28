package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.parser.Engines
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class Parser(private val config: JBakeConfiguration) {

    fun processFile(inputFile: File): DocumentModel? {
        val engine = Engines.get(FileUtil.fileExt(inputFile)) ?: run {
            log.error("Unable to find suitable markup engine for {}", inputFile)
            return null
        }
        return engine.parse(config, inputFile)
    }

    private val log: Logger = LoggerFactory.getLogger(Parser::class.java)
}
