package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.template.ModelExtractor

class TaggedDocumentsExtractor : ModelExtractor<DocumentList<*>> {

    override fun get(db: ContentStore, model: MutableMap<String, Any>, key: String): DocumentList<*> {

/*
        var tag: String? = null
        val templateModel = TemplateModel()
        templateModel.putAll(model)
        if (templateModel.tag != null)
            tag = templateModel.tag
*/
        val tag = model["tag"] as? String

        // Fetch the tagged documents from the database.
        return db.getPublishedDocumentsByTag(tag)
    }
}
