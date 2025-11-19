package org.jbake.app

import com.orientechnologies.orient.core.sql.executor.OResultSet
import org.jbake.model.DocumentModel
import java.util.*

/**
 * Wraps an OrientDB document iterator into a model usable by
 * template engines.
 *
 * @author Cédric Champeau
 */
class DocumentList<T> : LinkedList<T>() {

    companion object {
        fun wrap(docs: OResultSet): DocumentList<DocumentModel> {
            val list = DocumentList<DocumentModel>()
            while (docs.hasNext()) {
                val next = docs.next()
                list.add(DBUtil.documentToModel(next))
            }
            docs.close()
            return list
        }
    }
}
