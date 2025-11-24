package org.jbake.parser

import java.util.*

/**
 * An internal rendering engine used to notify the user that the markup format he used requires an engine that couldn't
 * be loaded.
 */
class ErrorEngine @JvmOverloads constructor(private val engineName: String = "unknown")
    : MarkupEngine()
{
    override fun processHeader(context: ParserContext) {
        val documentModel = context.documentModel
        documentModel.type = "post"
        documentModel.status = "published"
        documentModel.title = "Rendering engine missing"
        documentModel.date = Date()
        documentModel.tags = emptyArray<String>()
    }

    override fun processBody(parserContext: ParserContext) {
        parserContext.body = "The markup engine [" + engineName + "] for [" + parserContext.file + "] couldn't be loaded"
    }
}
