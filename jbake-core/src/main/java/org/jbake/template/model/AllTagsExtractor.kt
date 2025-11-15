package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.template.ModelExtractor

class AllTagsExtractor : ModelExtractor<MutableSet<String?>?> {
    override fun get(db: ContentStore, model: MutableMap<*, *>?, key: String?): MutableSet<String?>? {
        return db.getAllTags()
    }
}
