package org.jbake.model

object ModelAttributes {
    // Template model keys - these are the keys used in FreeMarker templates

    // ===== DOCUMENT METADATA (from AsciidoctorEngine/MarkupEngine parsing) =====
    const val DOC_BODY_RENDERED: String = "body"                              // Rendered HTML content of the document.
    const val DOC_DATE: String = "date"                                       // Publication date of the document (Date object).
    const val DOC_NAME: String = "name"                                       // Document name derived from filename.
    const val DOC_STATUS: String = "status"                                   // Publication status (draft/published).
    const val DOC_TAGS: String = "tags"                                       // List of tags for current document.
    const val DOC_TITLE: String = "title"                                     // Document title from header or filename.
    const val DOC_TYPE: String = "type"                                       // Document type (post/page).

    // ===== FILE SYSTEM INFO (from Crawler processing) =====
    const val FS_DOC_SHA1: String = "sha1"                                    // SHA1 hash of source file for change detection.
    const val FS_DOC_SOURCE_PATH_ABS: String = "file"                         // Absolute path to source file on disk.
    const val FS_DOC_OUTPUT_URI_NOEXT: String = "noExtensionUri"              // URI without .html extension.
    const val FS_DOC_IS_CACHED_IN_DB: String = "cached"                       // Boolean indicating if document is cached in database.
    const val FS_DOC_WAS_RENDERED: String = "rendered"                        // Boolean indicating if document has been rendered.
    const val FS_DOC_OUTPUT_URI: String = "uri"                               // Final output URI for the document.
    const val FS_DOC_SOURCE_REL_URI: String = "sourceuri"                     // URI relative to content directory.
    const val FS_REL_FROM_DOC_TO_SITEROOT: String = "rootpath"                // Relative path from document to site root.

    // ===== TEMPLATE CONTEXT (from Renderer) =====
    const val TMPL_CONTENT_MODEL: String = "content"                          // Current document model being rendered.
    const val TMPL_DB_ACCESS: String = "db"                                   // ContentStore instance for database queries in templates.
    const val TMPL_OUT_WRITER: String = "out"                                 // Writer for template output.
    const val TMPL_JBAKE_CONFIG: String = "config"                            // JBake configuration map for templates.
    const val TMPL_ENGINE: String = "renderer"                                // Template engine instance.

    // ===== NAVIGATION (from pagination) =====
    const val PAGI_CUR_PAGE_NUMBER: String = "currentPageNumber"              // Current page number in pagination.
    const val PAGI_NEXT_CONTENT: String = "nextContent"                       // Next document in chronological order.
    const val PAGI_NEXT_FILENAME: String = "nextFileName"                     // Filename of next page in pagination.
    const val PAGI_PREV_CONTENT: String = "previousContent"                   // Previous document in chronological order.
    const val PAGI_PREV_FILENAME: String = "previousFileName"                 // Filename of previous page in pagination.
    const val PAGI_TOTAL_PAGES_COUNT: String = "numberOfPages"                // Total number of pages in pagination.

    // ===== TAG FILTERING =====
    const val TAGS_ALL: String = "alltags"                                    // List of all unique tags across all documents.
    const val TAGS_CURRENT_TAG: String = "tag"                                // Current tag being filtered on.
    const val TAGS_DOCS_TAGGED_CUR: String = "tagged_documents"               // Documents tagged with current tag.
    const val TAGS_POSTS_TAGGED_CUR: String = "tagged_posts"                  // Posts tagged with current tag.

    // ===== DATA ACCESS =====
    const val DATA_FILES: String = "data"                                     // Data files loaded from data directory.

    // ===== GLOBAL =====
    const val GLOB_PUBLISHING_DATE_FORMATTED: String = "published_date"       // Formatted publication date string.
    const val GLOB_JBAKE_VERSION: String = "version"                          // JBake version string.


    /**
     * Possible values of the [ModelAttributes.DOC_STATUS] property
     */
    object Status {
        const val PUBLISHED_DATE: String = "published-date"
        const val PUBLISHED: String = "published"
        const val DRAFT: String = "draft"
    }
}
