package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.template.ModelExtractor

class PublishedPostsExtractor : ModelExtractor<DocumentList<*>> {

    override fun get(db: ContentStore, model: MutableMap<String, Any>, key: String): DocumentList<*> {
        return db.getPublishedPosts(model.containsKey("numberOfPages"))
    }
}
