package org.jbake.render

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.jbake.template.RenderingException
import java.io.File
import java.util.*

class DocumentsRenderer : RenderingTool {
    @Throws(RenderingException::class)
    override fun render(renderer: Renderer, db: ContentStore, config: JBakeConfiguration?): Int {
        var renderedCount = 0
        val errors: MutableList<String?> = LinkedList<String?>()

        val documentList = db.getUnrenderedContent()
        for (document in documentList) {
            try {
                val typedDocList = db.getAllContent(document.getType())
                val prev = getPrevDoc(typedDocList, document)
                val next = getNextDoc(typedDocList, document)
                document.setPreviousContent(prev)
                document.setNextContent(next)

                renderer.render(document)
                db.markContentAsRendered(document)
                renderedCount++
            } catch (e: Exception) {
                errors.add(e.message)
            }
        }

        if (!errors.isEmpty()) {
            val sb = StringBuilder()
            sb.append("Failed to render documents. Cause(s):")
            for (error in errors) {
                sb.append("\n").append(error)
            }
            throw RenderingException(sb.toString())
        } else {
            return renderedCount
        }
    }

    private fun getNextDoc(typedList: DocumentList<DocumentModel>, doc: DocumentModel?): DocumentModel? {
        var typedListIndex = typedList.indexOf(doc)
        if (typedList.getFirst() == doc) {
            // initial doc in typed list so there is no next
            return null
        } else {
            while (true) {
                try {
                    val nextDoc = typedList.get(typedListIndex - 1)
                    if (isPublished(nextDoc)) {
                        return getContentForNav(nextDoc)
                    } else {
                        typedListIndex--
                    }
                } catch (ex: IndexOutOfBoundsException) {
                    return null
                }
            }
        }
    }

    private fun getPrevDoc(typedList: DocumentList<DocumentModel>, doc: DocumentModel?): DocumentModel? {
        var typedListIndex = typedList.indexOf(doc)
        if (typedList.getLast() == doc) {
            // last doc in typed list so there is no previous
            return null
        } else {
            while (true) {
                try {
                    val prevDoc = typedList.get(typedListIndex + 1)
                    if (isPublished(prevDoc)) {
                        return getContentForNav(prevDoc)
                    } else {
                        typedListIndex++
                    }
                } catch (ex: IndexOutOfBoundsException) {
                    return null
                }
            }
        }
    }

    private fun isPublished(document: DocumentModel): Boolean {
        // Attributes.Status.PUBLISHED_DATE cannot occur here
        // because it's converted TO either PUBLISHED or DRAFT in the Crawler.
        return ModelAttributes.Status.PUBLISHED == document.getStatus()
    }

    /**
     * Creates a simple content model to use in individual post navigations.
     *
     * @param document original
     * @return navigation model for the 'document'
     */
    private fun getContentForNav(document: DocumentModel): DocumentModel {
        val navDocument = DocumentModel()
        navDocument.setNoExtensionUri(document.getNoExtensionUri())
        navDocument.setUri(document.getUri())
        navDocument.setTitle(document.getTitle())
        return navDocument
    }

    @Throws(RenderingException::class)
    override fun render(
        renderer: Renderer,
        db: ContentStore,
        destination: File?,
        templatesPath: File?,
        config: CompositeConfiguration?
    ): Int {
        return render(renderer, db, null)
    }
}
