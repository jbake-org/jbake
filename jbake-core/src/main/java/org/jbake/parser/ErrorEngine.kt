package org.jbake.parser

import java.util.*

/**
 * An internal rendering engine used to notify the user that the markup format he used requires an engine that couldn't
 * be loaded.
 *
 * @author Cédric Champeau
 */
class ErrorEngine @JvmOverloads constructor(private val engineName: String? = "unknown") : MarkupEngine() {
    override fun processHeader(context: ParserContext) {
        val documentModel = context.getDocumentModel()
        documentModel.setType("post")
        documentModel.setStatus("published")
        documentModel.setTitle("Rendering engine missing")
        documentModel.setDate(Date())
        documentModel.setTags(arrayOfNulls<String>(0))
    }

    override fun processBody(context: ParserContext) {
        context.setBody("The markup engine [" + engineName + "] for [" + context.getFile() + "] couldn't be loaded")
    }
}
