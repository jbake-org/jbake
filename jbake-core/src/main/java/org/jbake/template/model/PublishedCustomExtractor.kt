package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.template.ModelExtractor

class PublishedCustomExtractor(var customDocumentType: String?) : ModelExtractor<DocumentList<*>?> {
    override fun get(db: ContentStore, model: MutableMap<*, *>?, key: String?): DocumentList<*>? {
        return db.getPublishedContent(customDocumentType)
    }
}
