package org.jbake.model

object ModelAttributes {
    const val ALLTAGS: String = "alltags"
    const val BODY: String = "body"
    const val CACHED: String = "cached"
    const val CONFIG: String = "config"
    const val CONTENT: String = "content"
    const val CURRENT_PAGE_NUMBERS: String = "currentPageNumber"
    const val DATA: String = "data"
    const val DATE: String = "date"
    // Accessing the database directly sounds like a bad idea. `db.deleteContent()` anyone?
    // TBD: Replace it with a sub-interface of ContentStore with limited capabilities.
    const val DB: String = "db"
    const val FILE: String = "file"
    const val NAME: String = "name"
    const val NEXT_CONTENT: String = "nextContent"
    const val NEXT_FILENAME: String = "nextFileName"
    const val NO_EXTENSION_URI: String = "noExtensionUri"
    const val NUMBER_OF_PAGES: String = "numberOfPages"
    const val OUT: String = "out"
    const val PREVIOUS_CONTENT: String = "previousContent"
    const val PREVIOUS_FILENAME: String = "previousFileName"
    const val PUBLISHED_DATE: String = "published_date"
    const val RENDERED: String = "rendered"
    const val RENDERER: String = "renderer"
    const val ROOTPATH: String = "rootpath"
    const val SHA1: String = "sha1"
    const val SOURCE_URI: String = "sourceuri"
    const val STATUS: String = "status"
    const val TAG: String = "tag"
    const val TAGGED_DOCUMENTS: String = "tagged_documents"
    const val TAGGED_POSTS: String = "tagged_posts"
    const val TAGS: String = "tags"
    const val TITLE: String = "title"
    const val TYPE: String = "type"
    const val URI: String = "uri"
    const val VERSION: String = "version"

    /**
     * Possible values of the [ModelAttributes.STATUS] property
     */
    object Status {
        const val PUBLISHED_DATE: String = "published-date"
        const val PUBLISHED: String = "published"
        const val DRAFT: String = "draft"
    }
}
