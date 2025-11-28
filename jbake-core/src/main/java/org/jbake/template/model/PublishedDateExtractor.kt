package org.jbake.template.model

import org.jbake.template.TypedModelExtractor
import java.util.*

class PublishedDateExtractor : TypedModelExtractor<Date> {

    override fun extract(context: RenderContext, key: String): Date {
        return Date()
    }
}
