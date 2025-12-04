package org.jbake.parser

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import java.io.File
import java.util.*

class ParserContext(
    val file: File,
    val fileLines: MutableList<String>,
    val config: JBakeConfiguration,
    private val hasHeader: Boolean
) {
    val documentModel: DocumentModel = DocumentModel.createDefaultDocumentModel()

    fun hasHeader(): Boolean = hasHeader

    var body: String
        get() = documentModel.body
        set(str) {
            documentModel.body = str
        }

    var date: Date?
        get() = this.documentModel.date
        set(date) { this.documentModel.date = date}

    val status: String
        get() = this.documentModel.status ?: ""

    fun setDefaultStatus() {
        this.documentModel.status = this.config.defaultStatus ?: ""
    }

    val type: String
        get() = this.documentModel.type

    fun setDefaultType() {
        this.documentModel.type = this.config.defaultType ?: ""
    }

    fun setDefaultAuthor() {
        if (this.documentModel.containsKey("author")) return

        val defaultAuthor = this.config.defaultAuthor
        if (!defaultAuthor.isNullOrEmpty())
            this.documentModel["author"] = defaultAuthor
    }

    // TODO: Possibly nullable
    val tags: List<String>
        get() = this.documentModel.tags

    fun setTags(tags: List<String>) {
        this.documentModel.tags = tags
    }
}
