package org.jbake.parser

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import java.io.File

class ParserContext(
    val file: File?,
    val fileLines: MutableList<String?>?,
    val config: JBakeConfiguration?,
    private val hasHeader: Boolean
) {
    val documentModel: DocumentModel

    init {
        this.documentModel = DocumentModel.Companion.createDefaultDocumentModel()
    }

    fun hasHeader(): Boolean {
        return hasHeader
    }

    var body: String?
        // short methods for common use
        get() = documentModel.getBody()
        set(str) {
            documentModel.setBody(str)
        }

    var date: Date?
        get() = this.documentModel.getDate()
        set(date) {
            this.documentModel.setDate(date)
        }

    val status: String?
        get() {
            if (this.documentModel.getStatus() != null) {
                return this.documentModel.getStatus()
            }
            return ""
        }

    fun setDefaultStatus() {
        this.documentModel.setStatus(this.config!!.defaultStatus)
    }

    val type: String?
        get() {
            if (this.documentModel.getType() != null) {
                return this.documentModel.getType()
            }
            return ""
        }

    fun setDefaultType() {
        this.documentModel.setType(this.config!!.defaultType)
    }

    val tags: Any?
        get() = this.documentModel.getTags()

    fun setTags(tags: Array<String?>?) {
        this.documentModel.setTags(tags)
    }
}
