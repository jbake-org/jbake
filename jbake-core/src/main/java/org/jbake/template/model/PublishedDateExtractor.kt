package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.template.ModelExtractor
import java.util.*

class PublishedDateExtractor : ModelExtractor<Date?> {

    override fun get(db: ContentStore, model: MutableMap<*, *>, key: String): Date {
        return Date()
    }
}
