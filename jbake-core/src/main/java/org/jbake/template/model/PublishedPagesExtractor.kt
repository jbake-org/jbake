package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.template.TypedModelExtractor

class PublishedPagesExtractor : TypedModelExtractor<DocumentList<*>> {

    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        return context.publishedPages
    }
}
