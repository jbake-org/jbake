package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes
import org.jbake.template.TypedModelExtractor

class AllContentExtractor : TypedModelExtractor<DocumentList<*>> {

    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        val db = context.db
        // Use typed configuration access
        val dataFileDocType: String = context.config.dataFileDocType

        val allContent = DocumentList<DocumentModel>()

        val documentTypes = DocumentTypes.documentTypes
        for (docType in documentTypes) {
            if (docType != dataFileDocType) {
                val query = db.getAllContent(docType)
                allContent.addAll(query)
            }
        }
        return allContent
    }
}
