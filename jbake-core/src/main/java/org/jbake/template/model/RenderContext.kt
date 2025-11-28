package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.template.DelegatingTemplateEngine

/**
 * Type-safe rendering context that replaces the untyped Map<String, Any> pattern.
 * This represents all data available during template rendering.
 */
data class RenderContext(
    val config: JBakeConfiguration,
    val db: ContentStore,
    val content: DocumentModel? = null,
    val renderer: DelegatingTemplateEngine? = null,
    val pagination: PaginationContext? = null,
    val tag: TagContext? = null,
    val version: String? = null,
    val customData: Map<String, Any> = emptyMap()
) {
    /**
     * Get published posts from the database.
     * Lazily evaluated to avoid unnecessary queries.
     */
    val publishedPosts: DocumentList<*> by lazy {
        db.getPublishedPosts(false)
    }

    /** Get published pages from the database. */
    val publishedPages: DocumentList<*> by lazy { db.publishedPages }

    /** Get all published content from the database. */
    val publishedContent: DocumentList<*> by lazy { db.getPublishedContent("post") }

    /** Get all tags from the database. */
    val allTags: Set<String> by lazy { db.allTags }

    /** Access custom data by key for backward compatibility. Prefer using typed properties when possible. */
    operator fun get(key: String): Any? = customData[key]

    /** Create a new context with additional custom data. */
    fun withCustomData(key: String, value: Any): RenderContext {
        return copy(customData = customData + (key to value))
    }

    /** Create a new context with updated content. */
    fun withContent(content: DocumentModel) = copy(content = content)

    /** Create a new context with pagination. */
    fun withPagination(pagination: PaginationContext) = copy(pagination = pagination)

    /** Create a new context with tag information. */
    fun withTag(tag: TagContext) = copy(tag = tag)

    /**
     * Convert to legacy map format for backward compatibility with existing templates.
     * This should be removed once all templates are migrated.
     */
    @Deprecated("Use typed properties instead of map access")
    fun toLegacyMap(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()

        // Add configuration (for backward compatibility, some templates expect this)
        map["config"] = config

        content?.let { map["content"] = it }
        renderer?.let { map["renderer"] = it }
        version?.let { map["version"] = it }

        pagination?.let {
            map["numberOfPages"] = it.totalPages
            map["currentPageNumber"] = it.currentPage
            it.previousFilename?.let { prev -> map["previousFilename"] = prev }
            it.nextFilename?.let { next -> map["nextFilename"] = next }
        }

        tag?.let {
            map["tag"] = it.name
            map["taggedPosts"] = it.taggedPosts
            map["taggedDocuments"] = it.taggedDocuments
        }

        map.putAll(customData)

        return map
    }
}

/** Pagination information for paginated content. */
data class PaginationContext(val currentPage: Int, val totalPages: Int, val previousFilename: String?, val nextFilename: String?)

/** Tag-related context for tag pages. */
data class TagContext(val name: String, val taggedPosts: DocumentList<*>, val taggedDocuments: DocumentList<*>)


