package org.jbake.template.model

import org.jbake.template.TypedModelExtractor

class AllTagsExtractor : TypedModelExtractor<MutableSet<String>> {

    override fun extract(context: RenderContext, key: String): MutableSet<String> {
        return context.db.allTags
    }
}
