package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypeUtils
import org.jbake.model.DocumentTypes
import org.jbake.template.TypedModelExtractor

/**
 * Collection of extractors that deal with documents and content.
 */

/**
 * Extracts documents by type based on the key.
 * Handles pluralized document type keys (e.g., "posts" -> "post").
 */
class TypedDocumentsExtractor : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        try {
            val type = DocumentTypeUtils.unpluralize(key)
            return context.db.getAllContent(type)
        }
        catch (e: UnsupportedOperationException) { return DocumentList<Any>() }
    }
}

/**
 * Extracts documents filtered by tag.
 */
class TaggedDocumentsExtractor : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        val tag = context.tag?.name ?: context["tag"] as? String
        return context.db.getPublishedDocumentsByTag(tag)
    }
}

/**
 * Extracts all published content across all document types.
 */
class PublishedContentExtractor : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        val publishedContent = DocumentList<DocumentModel>()
        for (docType in DocumentTypes.documentTypes) {
            val query = context.db.getPublishedContent(docType)
            publishedContent.addAll(query)
        }
        return publishedContent
    }
}

/**
 * Extracts all content across all document types (excluding data files).
 */
class AllContentExtractor : TypedModelExtractor<DocumentList<*>> {
    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        // Use typed configuration access
        val dataFileDocType: String = context.config.dataFileDocType

        val allContent = DocumentList<DocumentModel>()

        for (docType in DocumentTypes.documentTypes) {
            if (docType != dataFileDocType) {
                val query = context.db.getAllContent(docType)
                allContent.addAll(query)
            }
        }
        return allContent
    }
}

