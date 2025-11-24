package org.jbake.template.model

import org.jbake.app.DocumentList

/**
 * New type-safe template model that wraps RenderContext.
 * This replaces the old HashMap-based approach with proper typed properties.
 */
class TypedTemplateModel(
    val context: RenderContext
) {
    // Direct accessors to context properties
    val config get() = context.config
    val db get() = context.db
    val content get() = context.content
    val renderer get() = context.renderer

    // Pagination properties
    val numberOfPages: Int get() = context.pagination?.totalPages ?: 0
    val currentPageNumber: Int get() = context.pagination?.currentPage ?: 0
    val previousFilename: String? get() = context.pagination?.previousFilename
    val nextFileName: String? get() = context.pagination?.nextFilename

    // Tag properties
    val tag: String? get() = context.tag?.name
    val taggedPosts: DocumentList<*>? get() = context.tag?.taggedPosts
    val taggedDocuments: DocumentList<*>? get() = context.tag?.taggedDocuments

    // Version
    val version: String? get() = context.version

    // Lazy-loaded collections
    val publishedPosts: DocumentList<*> get() = context.publishedPosts
    val publishedPages: DocumentList<*> get() = context.publishedPages
    val publishedContent: DocumentList<*> get() = context.publishedContent
    val allTags: Set<String> get() = context.allTags

    // Custom data access
    operator fun get(key: String): Any? = context[key]
}


