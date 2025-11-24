package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.model.DocumentTypeUtils
import org.jbake.template.TypedModelExtractor

class TypedDocumentsExtractor : TypedModelExtractor<DocumentList<*>> {

    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        try {
            val type = DocumentTypeUtils.unpluralize(key)
            return context.db.getAllContent(type)
        } catch (e: UnsupportedOperationException) {
            return DocumentList<Any>()
        }
    }
}
