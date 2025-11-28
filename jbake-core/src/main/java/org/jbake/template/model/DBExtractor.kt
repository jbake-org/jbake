package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.template.TypedModelExtractor

class DBExtractor : TypedModelExtractor<ContentStore> {

    override fun extract(context: RenderContext, key: String): ContentStore {
        return context.db
    }
}
