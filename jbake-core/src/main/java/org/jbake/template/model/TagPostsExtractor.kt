package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.template.ModelExtractor

class TagPostsExtractor : ModelExtractor<DocumentList<*>?> {
    override fun get(db: ContentStore, model: MutableMap<*, *>?, key: String?): DocumentList<*>? {
        var tag: String? = null
        val templateModel = TemplateModel()
        templateModel.putAll(model)
        if (templateModel.getTag() != null) {
            tag = templateModel.getTag()
        }
        // fetch the tag posts from db
        return db.getPublishedPostsByTag(tag)
    }
}
