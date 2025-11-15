package org.jbake.template

import org.jbake.app.ContentStore

/**
 * @param <T> the type of data returned by this model extractor
 * @author ndx
</T> */
interface ModelExtractor<T> {
    fun get(db: ContentStore?, model: MutableMap<*, *>?, key: String?): T?
}
