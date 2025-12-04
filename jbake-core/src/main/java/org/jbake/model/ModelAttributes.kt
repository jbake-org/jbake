package org.jbake.model

object ModelAttributes {
    // Template model keys - these are the keys used in FreeMarker templates

    // Document content and metadata
    const val ALLTAGS: String = "alltags"                      // List of all unique tags across all documents.
    const val BODY: String = "body"                            // Rendered HTML content of the document.
    const val CACHED: String = "cached"                        // Boolean indicating if document is cached in database.
    const val CONFIG: String = "config"                        // JBake configuration map for templates.
    const val CONTENT: String = "content"                      // Current document model being rendered.
    const val CURRENT_PAGE_NUMBERS: String = "currentPageNumber" // Current page number in pagination.
    const val DATA: String = "data"                            // Data files loaded from data directory.
    const val DATE: String = "date"                            // Publication date of the document (Date object).
    // Accessing the database directly sounds like a bad idea. `db.deleteContent()` anyone?
    // TBD: Replace it with a sub-interface of ContentStore with limited capabilities.
    const val DB: String = "db"                                // ContentStore instance for database queries in templates.
    const val FILE: String = "file"                            // Absolute path to source file on disk.
    const val NAME: String = "name"                            // Document name derived from filename.
    const val NEXT_CONTENT: String = "nextContent"             // Next document in chronological order.
    const val NEXT_FILENAME: String = "nextFileName"           // Filename of next page in pagination.
    const val NO_EXTENSION_URI: String = "noExtensionUri"      // URI without .html extension.
    const val NUMBER_OF_PAGES: String = "numberOfPages"        // Total number of pages in pagination.
    const val OUT: String = "out"                              // Writer for template output.
    const val PREVIOUS_CONTENT: String = "previousContent"     // Previous document in chronological order.
    const val PREVIOUS_FILENAME: String = "previousFileName"   // Filename of previous page in pagination.
    const val PUBLISHED_DATE: String = "published_date"        // Formatted publication date string.
    const val RENDERED: String = "rendered"                    // Boolean indicating if document has been rendered.
    const val RENDERER: String = "renderer"                    // Template engine instance.
    const val ROOTPATH: String = "rootpath"                    // Relative path from document to site root.
    const val SHA1: String = "sha1"                            // SHA1 hash of source file for change detection.
    const val SOURCE_URI: String = "sourceuri"                 // URI relative to content directory.
    const val STATUS: String = "status"                        // Publication status (draft/published).
    const val TAG: String = "tag"                              // Current tag being filtered on.
    const val TAGGED_DOCUMENTS: String = "tagged_documents"    // Documents tagged with current tag.
    const val TAGGED_POSTS: String = "tagged_posts"            // Posts tagged with current tag.
    const val TAGS: String = "tags"                            // List of tags for current document.
    const val TITLE: String = "title"                          // Document title from header or filename.
    const val TYPE: String = "type"                            // Document type (post/page).
    const val URI: String = "uri"                              // Final output URI for the document.
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
