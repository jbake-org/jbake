package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes
import org.jbake.template.TypedModelExtractor

class PublishedContentExtractor : TypedModelExtractor<DocumentList<*>> {

    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        val publishedContent = DocumentList<DocumentModel>()
        for (docType in DocumentTypes.documentTypes) {
            val query = context.db.getPublishedContent(docType)
            publishedContent.addAll(query)
        }
        return publishedContent
    }
}
