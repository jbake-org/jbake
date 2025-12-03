package org.jbake.app

import org.jbake.model.DocumentModel
import java.io.File

/**
 * Facade for content storage operations.
 * Delegates to the underlying ContentRepository implementation.
 * Currently uses HSQLDB for better Java 17+ compatibility.
 */
class ContentStore(type: String, name: String) {

    private val repository: ContentRepository = HsqldbContentRepository(type, name)

    var paginationOffset: Int
        get() = repository.paginationOffset
        set(value) { repository.paginationOffset = value }

    var paginationLimit: Int
        get() = repository.paginationLimit
        set(value) { repository.paginationLimit = value }


    fun startup() = repository.startup()
    fun resetPagination() = repository.resetPagination()
    fun updateSchema() = repository.updateSchema()
    fun close() = repository.close()
    fun shutdown() = repository.shutdown()
    fun drop() = repository.drop()

    fun getDocumentCount(docType: String) = repository.getDocumentCount(docType)
    fun getPublishedCount(docType: String) = repository.getPublishedCount(docType)
    fun getDocumentByUri(uri: String?) = repository.getDocumentByUri(uri)
    fun getDocumentStatus(uri: String?) = repository.getDocumentStatus(uri)

    val publishedPosts get() = repository.publishedPosts
    fun getPublishedPosts(applyPaging: Boolean) = repository.getPublishedPosts(applyPaging)
    fun getPublishedPostsByTag(tag: String?) = repository.getPublishedPostsByTag(tag)
    fun getPublishedDocumentsByTag(tag: String?) = repository.getPublishedDocumentsByTag(tag)

    val publishedPages get() = repository.publishedPages
    fun getPublishedContent(docType: String) = repository.getPublishedContent(docType)
    fun getPublishedContent(docType: String, applyPaging: Boolean) = repository.getPublishedContent(docType, applyPaging)

    fun getAllContent(docType: String) = repository.getAllContent(docType)
    fun getAllContent(docType: String, applyPaging: Boolean) = repository.getAllContent(docType, applyPaging)

    val unrenderedContent get() = repository.unrenderedContent
    fun deleteContent(uri: String) = repository.deleteContent(uri)
    fun markContentAsRendered(document: DocumentModel) = repository.markContentAsRendered(document)
    fun deleteAllByDocType(docType: String) = repository.deleteAllByDocType(docType)

    val tags get() = repository.tags
    val allTags get() = repository.allTags

    fun updateAndClearCacheIfNeeded(needed: Boolean, templateDir: File) = repository.updateAndClearCacheIfNeeded(needed, templateDir)

    val isActive get() = repository.isActive
    fun addDocument(document: DocumentModel) = repository.addDocument(document)
}
