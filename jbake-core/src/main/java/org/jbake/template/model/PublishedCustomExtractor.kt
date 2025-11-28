package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.template.TypedModelExtractor

class PublishedCustomExtractor(private val customDocumentType: String) : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        return context.db.getPublishedContent(customDocumentType)
    }
}
