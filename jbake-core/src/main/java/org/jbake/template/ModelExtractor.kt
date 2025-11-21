package org.jbake.template

import org.jbake.app.ContentStore

/**
 * @param T  The type of data returned by this model extractor.
 * @author ndx
 */
interface ModelExtractor<T> {
    fun get(db: ContentStore, model: MutableMap<String, Any>, key: String): T?
}
