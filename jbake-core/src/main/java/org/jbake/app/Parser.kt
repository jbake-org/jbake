package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.parser.ParserEnginesRegistry
import org.jbake.util.Logging.logger
import org.jruby.RubyObject
import org.slf4j.Logger
import java.io.File

class Parser(private val config: JBakeConfiguration) {

    fun processFile(inputFile: File): DocumentModel? {
        val engine = ParserEnginesRegistry.get(FileUtil.fileExt(inputFile))
            ?: run {
                log.error("Unable to find suitable markup engine for $inputFile")
                return null
            }
        return engine.parse(config, inputFile)
    }

    private val log: Logger by logger()
}

fun rejectUnparsableTypes(e: Map.Entry<String, Any>): Boolean =
    when (e.value) {
        is RubyObject -> false
        /*is RubySymbol -> false
        is RubyClass -> false
        is RubyModule -> false*/
        else -> true
    }
