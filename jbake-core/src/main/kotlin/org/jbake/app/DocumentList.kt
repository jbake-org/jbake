package org.jbake.app

import com.orientechnologies.orient.core.sql.executor.OResultSet
import org.jbake.model.DocumentModel
import java.util.*

/**
 * A specialized list for document models usable by template engines.
 *
 * Uses delegation instead of inheritance for better design.
 */
class DocumentList<T>(
    private val delegate: MutableList<T> = LinkedList()
) : MutableList<T> by delegate {

    fun push(element: T) = add(0, element)

    companion object {

        /** Wraps an OrientDB document iterator into a model usable by template engines. TODO: OrientDB-specific. */
        fun wrapOrientDbResultToDocumentList(docs: OResultSet): DocumentList<DocumentModel> {
            val list = DocumentList<DocumentModel>()
            while (docs.hasNext()) {
                val next = docs.next()
                list.add(DbUtils.orientdbDocumentToModel(next))
            }
            docs.close()
            return list
        }
    }
}
