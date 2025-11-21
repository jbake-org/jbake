package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.template.ModelExtractor

class TagPostsExtractor : ModelExtractor<DocumentList<*>> {

    override fun get(db: ContentStore, model: MutableMap<String, Any>, key: String): DocumentList<*> {
        //val tag = model["tag"] as? String

        var tag: String? = null
        val templateModel = TemplateModel().apply { putAll(model) }
        if (templateModel.tag != null) {
            tag = templateModel.tag
        }
        // fetch the tag posts from db
        return db.getPublishedPostsByTag(tag)
    }
}
