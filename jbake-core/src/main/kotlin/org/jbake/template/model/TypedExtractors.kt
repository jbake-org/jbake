package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.template.TypedModelExtractor

/**
 * Type-safe extractor for published posts.
 * Replaces the old PublishedPostsExtractor with proper typing.
 */
class TypedPublishedPostsExtractor : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String) = context.publishedPosts
}

/**
 * Type-safe extractor for published pages.
 */
class TypedPublishedPagesExtractor : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String) = context.publishedPages
}

/**
 * Type-safe extractor for all tags.
 */
class TypedAllTagsExtractor : TypedModelExtractor<Set<String>> {
    override fun extract(context: RenderContext, key: String) = context.allTags
}

/**
 * Type-safe extractor for tagged documents.
 */
class TypedTaggedDocumentsExtractor : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String) =
        context.tag?.name?.let { context.db.getPublishedDocumentsByTag(it) }
}

/**
 * Type-safe extractor for published content by type.
 */
class TypedPublishedContentExtractor(private val docType: String) : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String) = context.db.getPublishedContent(docType)
}

/**
 * Type-safe extractor for all content by type.
 */
class TypedAllContentExtractor(private val docType: String) : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String) = context.db.getAllContent(docType)
}

