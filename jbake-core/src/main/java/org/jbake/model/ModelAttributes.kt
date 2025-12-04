package org.jbake.model

object ModelAttributes {
    // Template model keys - these are the keys used in FreeMarker templates

    // ===== DOCUMENT METADATA (from AsciidoctorEngine/MarkupEngine parsing) =====
    // BODY_RENDERED_HTML_CONTENT
    const val BODY: String = "body"                            // Rendered HTML content of the document.
    // DOC_DATE
    const val DATE: String = "date"                            // Publication date of the document (Date object).
    // NAME_DOCUMENT_NAME
    const val NAME: String = "name"                            // Document name derived from filename.
    // DOC_STATUS
    const val STATUS: String = "status"                        // Publication status (draft/published).
    // DOC_TAGS
    const val TAGS: String = "tags"                            // List of tags for current document.
    // DOC_TITLE
    const val TITLE: String = "title"                          // Document title from header or filename.
    // DOC_TYPE
    const val TYPE: String = "type"                            // Document type (post/page).

    // ===== FILE SYSTEM INFO (from Crawler processing) =====
    // FS_FILE_HASH
    const val SHA1: String = "sha1"                            // SHA1 hash of source file for change detection.
    // FS_FILE_PATH
    const val FILE: String = "file"                            // Absolute path to source file on disk.
    // FS_EXTENSIONLESS_URI
    const val NO_EXTENSION_URI: String = "noExtensionUri"      // URI without .html extension.
    // FS_IS_CACHED
    const val CACHED: String = "cached"                        // Boolean indicating if document is cached in database.
    // FS_IS_RENDERED
    const val RENDERED: String = "rendered"                    // Boolean indicating if document has been rendered.
    // FS_OUTPUT_URI
    const val URI: String = "uri"                              // Final output URI for the document.
    // FS_RELATIVE_URI
    const val SOURCE_URI: String = "sourceuri"                 // URI relative to content directory.
    // FS_ROOT_RELATIVE_PATH
    const val ROOTPATH: String = "rootpath"                    // Relative path from document to site root.

    // ===== TEMPLATE CONTEXT (from Renderer) =====
    // TMPL_CURRENT_DOCUMENT
    const val CONTENT: String = "content"                      // Current document model being rendered.
    // TMPL_DATABASE_ACCESS
    const val DB: String = "db"                                // ContentStore instance for database queries in templates.
    // TMPL_OUTPUT_WRITER
    const val OUT: String = "out"                              // Writer for template output.
    // TMPL_SITE_CONFIG
    const val CONFIG: String = "config"                        // JBake configuration map for templates.
    // TMPL_TEMPLATE_ENGINE
    const val RENDERER: String = "renderer"                    // Template engine instance.

    // ===== NAVIGATION (from pagination) =====
    // NAV_CURRENT_PAGE_INDEX
    const val CURRENT_PAGE_NUMBERS: String = "currentPageNumber" // Current page number in pagination.
    // NAV_NEXT_DOCUMENT
    const val NEXT_CONTENT: String = "nextContent"             // Next document in chronological order.
    // NAV_NEXT_PAGE_FILENAME
    const val NEXT_FILENAME: String = "nextFileName"           // Filename of next page in pagination.
    // NAV_PREVIOUS_DOCUMENT
    const val PREVIOUS_CONTENT: String = "previousContent"     // Previous document in chronological order.
    // NAV_PREVIOUS_PAGE_FILENAME
    const val PREVIOUS_FILENAME: String = "previousFileName"   // Filename of previous page in pagination.
    // NAV_TOTAL_PAGES
    const val NUMBER_OF_PAGES: String = "numberOfPages"        // Total number of pages in pagination.

    // ===== TAG FILTERING =====
    // TAG_ALL_UNIQUE_TAGS
    const val ALLTAGS: String = "alltags"                      // List of all unique tags across all documents.
    // TAG_CURRENT_FILTER
    const val TAG: String = "tag"                              // Current tag being filtered on.
    // TAG_DOCUMENTS_BY_TAG
    const val TAGGED_DOCUMENTS: String = "tagged_documents"    // Documents tagged with current tag.
    // TAG_POSTS_BY_TAG
    const val TAGGED_POSTS: String = "tagged_posts"            // Posts tagged with current tag.

    // ===== DATA ACCESS =====
    // DATA_FILES
    const val DATA: String = "data"                            // Data files loaded from data directory.

    // ===== FORMATTING =====
    // FMT_FORMATTED_PUBLISH_DATE
    const val PUBLISHED_DATE: String = "published_date"        // Formatted publication date string.

    // ===== VERSION INFO =====
    // VER_JBAKE_VERSION
    const val VERSION: String = "version"                      // JBake version string.


    /**
     * Possible values of the [ModelAttributes.STATUS] property
     */
    object Status {
        const val PUBLISHED_DATE: String = "published-date"
        const val PUBLISHED: String = "published"
        const val DRAFT: String = "draft"
    }
}
