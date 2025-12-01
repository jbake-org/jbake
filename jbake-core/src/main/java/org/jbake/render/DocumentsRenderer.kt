package org.jbake.render

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.jbake.template.RenderingException
import org.jbake.util.Logging.logger
import org.slf4j.Logger
import java.util.*

class DocumentsRenderer : RenderingTool {
    private val log: Logger by logger()

    @Throws(RenderingException::class)
    @Deprecated("Overrides deprecated member")
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration): Int {
        var renderedCount = 0
        val errors: MutableList<String> = LinkedList<String>()

        val documentList = db.unrenderedContent
        for (document in documentList) {
            try {
                val typedDocList = db.getAllContent(document.type)
                val prev = getPrevDoc(typedDocList, document)
                val next = getNextDoc(typedDocList, document)
                document.previousContent = prev
                document.nextContent = next

                renderer.render(document)
                db.markContentAsRendered(document)
                renderedCount++
            } catch (e: Exception) {
                // Log full stacktrace to help debugging in tests
                log.error("Error rendering document {} (type={})", document.name, document.type, e)
                errors.add(e.message ?: (e.toString() + " " + e.stackTrace.first().toString()))
            }
        }

        if (!errors.isEmpty()) {
            val sb = StringBuilder()
            sb.append("Failed to render documents. Cause(s):")
            for (error in errors) {
                sb.append("\n").append(error)
            }
            throw RenderingException(sb.toString())
        }
        return renderedCount
    }

    private fun getNextDoc(typedList: DocumentList<DocumentModel>, doc: DocumentModel?): DocumentModel? {
        var typedListIndex = typedList.indexOf(doc)
        if (typedList.first() == doc) {
            // initial doc in typed list so there is no next
            return null
        }
        while (true) {
            try {
                val nextDoc = typedList[typedListIndex - 1]
                if (isPublished(nextDoc)) {
                    return getContentForNav(nextDoc)
                }
                typedListIndex--
            } catch (ex: IndexOutOfBoundsException) {
                return null
            }
        }
    }

    private fun getPrevDoc(typedList: DocumentList<DocumentModel>, doc: DocumentModel?): DocumentModel? {
        var typedListIndex = typedList.indexOf(doc)
        if (typedList.last() == doc) {
            // last doc in typed list so there is no previous
            return null
        }
        while (true) {
            try {
                val prevDoc = typedList[typedListIndex + 1]
                if (isPublished(prevDoc)) {
                    return getContentForNav(prevDoc)
                }
                typedListIndex++
            } catch (ex: IndexOutOfBoundsException) {
                return null
            }
        }
    }

    private fun isPublished(document: DocumentModel): Boolean {
        // Attributes.Status.PUBLISHED_DATE cannot occur here
        // because it's converted TO either PUBLISHED or DRAFT in the Crawler.
        return ModelAttributes.Status.PUBLISHED == document.status
    }

    /**
     * Creates a simple content model to use in individual post navigations.
     *
     * @param document original
     * @return navigation model for the 'document'
     */
    private fun getContentForNav(document: DocumentModel): DocumentModel {
        val navDocument = DocumentModel()
        navDocument.noExtensionUri = document.noExtensionUri
        navDocument.uri = document.uri
        navDocument.title = document.title
        return navDocument
    }
}
