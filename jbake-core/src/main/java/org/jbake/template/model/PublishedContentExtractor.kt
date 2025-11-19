package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes
import org.jbake.template.ModelExtractor

class PublishedContentExtractor : ModelExtractor<DocumentList<*>?> {
    override fun get(db: ContentStore, model: MutableMap<*, *>?, key: String?): DocumentList<*> {
        val publishedContent = DocumentList<DocumentModel>()
        val documentTypes = DocumentTypes.documentTypes
        for (docType in documentTypes) {
            val query = db.getPublishedContent(docType)
            publishedContent.addAll(query!!)
        }
        return publishedContent
    }
}
