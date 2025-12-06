package org.jbake.app

import org.jbake.model.DocumentModel
import java.io.File

/**
 * Repository interface for content storage operations.
 * Abstracts the underlying storage implementation (OrientDB, HSQLDB, etc.)
 */
interface ContentRepository {

    /** Initialize the repository and ensure schema is ready */
    fun startup()

    /** Close all connections and cleanup resources */
    fun close()

    /** Shutdown the repository */
    fun shutdown()

    /** Drop the entire database */
    fun drop()

    /** Update/create the database schema */
    fun updateSchema()

    /** Check if the repository is active on current thread */
    val isActive: Boolean

    // Document CRUD operations

    /** Add or update a document */
    fun addDocument(document: DocumentModel)

    /** Get a document by its source URI */
    fun getDocumentByUri(uri: String?): DocumentList<DocumentModel>

    /** Get document status (sha1, rendered) by URI */
    fun getDocumentStatus(uri: String?): DocumentList<DocumentModel>

    /** Delete content by source URI */
    fun deleteContent(uri: String)

    /** Delete all documents of a specific type */
    fun deleteAllByDocType(docType: String)

    /** Mark content as rendered */
    fun markContentAsRendered(document: DocumentModel)

    // Query operations

    /** Get count of documents by type */
    fun getDocumentCount(docType: String): Long

    /** Get count of published documents by type */
    fun getPublishedCount(docType: String): Long

    /** Get all content by document type */
    fun getAllContent(docType: String): DocumentList<DocumentModel>

    /** Get all content by document type with optional pagination */
    fun getAllContent(docType: String, applyPaging: Boolean): DocumentList<DocumentModel>

    /** Get published content by document type */
    fun getPublishedContent(docType: String): DocumentList<DocumentModel>

    /** Get published content by document type with optional pagination */
    fun getPublishedContent(docType: String, applyPaging: Boolean): DocumentList<DocumentModel>

    /** Get published posts */
    val publishedPosts: DocumentList<DocumentModel>

    /** Get published posts with optional pagination */
    fun getPublishedPosts(applyPaging: Boolean): DocumentList<DocumentModel>

    /** Get published posts by tag */
    fun getPublishedPostsByTag(tag: String?): DocumentList<DocumentModel>

    /** Get published documents of any type by tag */
    fun getPublishedDocumentsByTag(tag: String?): DocumentList<DocumentModel>

    /** Get published pages */
    val publishedPages: DocumentList<DocumentModel>

    /** Get unrendered content */
    val unrenderedContent: DocumentList<DocumentModel>

    /** Get all tags from published posts */
    val tags: MutableSet<String>

    /** Get all tags from all published documents */
    val allTags: MutableSet<String>

    // Cache and signature management

    /** Update cache if needed based on template changes */
    fun updateAndClearCacheIfNeeded(needed: Boolean, templateDir: File)

    // Pagination support

    var paginationOffset: Int
    var paginationLimit: Int

    /** Reset pagination settings */
    fun resetPagination()
}

