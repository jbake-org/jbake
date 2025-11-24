package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.template.TypedModelExtractor
import org.jbake.template.model.RenderContext

class TagPostsExtractor : TypedModelExtractor<DocumentList<*>> {

    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        val tag = context.tag?.name ?: context["tag"] as? String
        return context.db.getPublishedPostsByTag(tag)
    }
}
