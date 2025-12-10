package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.configuration.PropertyList.DATA_FILE_DOCTYPE
import org.jbake.template.ModelExtractor
import org.jbake.template.TypedModelExtractor
import org.jbake.util.DataFileUtil
import java.time.OffsetDateTime

/**
 * Collection of simple extractors that are small enough to be grouped together.
 * Each extractor provides a specific piece of data to the template rendering context.
 */

/** Extracts all tags from the database. */
class AllTagsExtractor : TypedModelExtractor<MutableSet<String>> {
    override fun extract(context: RenderContext, key: String): MutableSet<String>
        = context.db.allTags
}

/** Extracts the current date as the published date. */
class PublishedDateExtractor : TypedModelExtractor<OffsetDateTime> {
    override fun extract(context: RenderContext, key: String): OffsetDateTime = OffsetDateTime.now()
}

/** Extracts the database/content store itself. */
class DBExtractor : TypedModelExtractor<ContentStore> {
    override fun extract(context: RenderContext, key: String): ContentStore = context.db
}

/** Extracts published pages from the context. */
class PublishedPagesExtractor : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String): DocumentList<*> = context.publishedPages
}

/** Extracts published posts from the database. TODO: Convert to TypedModelExtractor. */
class PublishedPostsExtractor : ModelExtractor<DocumentList<*>> {
    override fun get(db: ContentStore, model: MutableMap<String, Any>, key: String): DocumentList<*> {
        val posts = db.getPublishedPosts(model.containsKey("numberOfPages"))
        /// Convert date fields to java.util.Date for Freemarker compatibility
        /// TODO Remove when Freemarker adapter stable.
        /*posts.forEach { post ->
            val date = post["date"]
            if (date is OffsetDateTime)
                post["date"] = java.util.Date.from(date.toInstant())
        }*/
        return posts
    }
}

/** Extracts published posts filtered by tag. */
class TagPostsExtractor : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        val tag = context.tag?.name ?: context["tag"] as? String
        return context.db.getPublishedPostsByTag(tag)
    }
}

/** Extracts published content for a custom document type. */
class PublishedCustomExtractor(private val customDocumentType: String) : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String): DocumentList<*>
        = context.db.getPublishedContent(customDocumentType)
}

/** Extracts data file utilities for accessing data files. */
class DataExtractor : TypedModelExtractor<DataFileUtil> {
    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    override fun extract(context: RenderContext, key: String): DataFileUtil {
        val model = context.toLegacyMap()
        val config = model["config"] as? Map<String, Any>
        val defaultDocType: String = config?.get(DATA_FILE_DOCTYPE.key.replace(".", "_"))?.toString() ?: ""
        return DataFileUtil(context.db, defaultDocType)
    }
}
