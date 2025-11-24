package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.template.TypedModelExtractor

class TaggedDocumentsExtractor : TypedModelExtractor<DocumentList<*>> {

    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        val tag = context.tag?.name ?: context["tag"] as? String
        return context.db.getPublishedDocumentsByTag(tag)
    }
}
