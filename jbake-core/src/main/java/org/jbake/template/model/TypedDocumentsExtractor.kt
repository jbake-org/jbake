package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.model.DocumentTypeUtils
import org.jbake.template.ModelExtractor

class TypedDocumentsExtractor : ModelExtractor<DocumentList<*>> {

    override fun get(db: ContentStore, model: MutableMap<*, *>, key: String): DocumentList<*> {
        // Document types are pluralized in model, so unpluralize.
        try {
            val type = DocumentTypeUtils.unpluralize(key)
            return db.getAllContent(type)
        } catch (e: UnsupportedOperationException) {
            return DocumentList<Any>()
        }
    }
}
