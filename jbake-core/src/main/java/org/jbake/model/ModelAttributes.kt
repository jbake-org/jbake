package org.jbake.model

object ModelAttributes {
    const val SHA1: String = "sha1"
    const val SOURCE_URI: String = "sourceuri"
    const val RENDERED: String = "rendered"
    const val CACHED: String = "cached"
    const val STATUS: String = "status"
    const val NAME: String = "name"
    const val BODY: String = "body"
    const val DATE: String = "date"
    const val TYPE: String = "type"
    const val TAGS: String = "tags"
    const val URI: String = "uri"
    const val ROOTPATH: String = "rootpath"
    const val FILE: String = "file"
    const val NO_EXTENSION_URI: String = "noExtensionUri"
    const val TITLE: String = "title"
    const val TAGGED_POSTS: String = "tagged_posts"
    const val TAGGED_DOCUMENTS: String = "tagged_documents"
    const val NEXT_CONTENT: String = "nextContent"
    const val PREVIOUS_CONTENT: String = "previousContent"
    const val CONFIG: String = "config"
    const val CONTENT: String = "content"
    const val RENDERER: String = "renderer"
    const val NUMBER_OF_PAGES: String = "numberOfPages"
    const val CURRENT_PAGE_NUMBERS: String = "currentPageNumber"
    const val PREVIOUS_FILENAME: String = "previousFileName"
    const val NEXT_FILENAME: String = "nextFileName"
    const val TAG: String = "tag"
    const val VERSION: String = "version"
    const val OUT: String = "out"
    const val ALLTAGS: String = "alltags"
    const val PUBLISHED_DATE: String = "published_date"
    const val DB: String = "db"
    const val DATA: String = "data"

    /**
     * Possible values of the [ModelAttributes.STATUS] property
     *
     * @author ndx
     */
    object Status {
        const val PUBLISHED_DATE: String = "published-date"
        const val PUBLISHED: String = "published"
        const val DRAFT: String = "draft"
    }
}
