package org.jbake.app

import org.jbake.model.DocumentModel
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Facade for content storage operations.
 * Delegates to the underlying ContentRepository implementation.
 * Supports HSQLDB, Neo4j, and OrientDB backends.
 */
class ContentStore(type: String, name: String) {

    private val log: org.slf4j.Logger = LoggerFactory.getLogger(ContentStore::class.java)

    private val repository: ContentRepository = createRepository(type, name)


    private fun createRepository(type: String, name: String): ContentRepository {
        log.info("Using Content Repository type: $type with name: $name")

        // Check if name contains database type hint
        return when {
            name.contains("-neo4j-") -> Neo4jContentRepository(type, name)
            name.contains("-orientdb-") -> OrientDBContentRepository(type, name)
            else -> HsqldbContentRepository(type, name) // Default
        }
    }

    // TODO: This must be in a context, not in the DAO.
    var paginationOffset: Int
        get() = repository.paginationOffset
        set(value) { repository.paginationOffset = value }

    // TODO: This must be in a context, not in the DAO.
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
    // TODO: Make non-nullable
    fun getDocumentByUri(uri: String?) = repository.getDocumentByUri(uri)
    fun getDocumentStatus(uri: String?) = repository.getDocumentStatus(uri)

    val publishedPosts get() = repository.publishedPosts
    fun getPublishedPosts(applyPaging: Boolean) = repository.getPublishedPosts(applyPaging)
    // TODO: Make non-nullable
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
