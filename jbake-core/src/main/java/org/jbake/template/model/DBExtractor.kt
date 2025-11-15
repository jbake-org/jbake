package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.template.ModelExtractor

class DBExtractor : ModelExtractor<ContentStore?> {
    override fun get(db: ContentStore?, model: MutableMap<*, *>?, key: String?): ContentStore? {
        return db
    }
}
