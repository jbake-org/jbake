package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes
import org.jbake.template.ModelExtractor

class AllContentExtractor : ModelExtractor<DocumentList<*>?> {
    override fun get(db: ContentStore, model: MutableMap<*, *>, key: String?): DocumentList<*> {
        val config = model.get("config") as MutableMap<String?, Any?>
        val dataFileDocType: String? = config.get(DATA_FILE_DOCTYPE.key.replace(".", "_")).toString()
        val allContent = DocumentList<DocumentModel>()
        val documentTypes = DocumentTypes.documentTypes
        for (docType in documentTypes) {
            if (docType != dataFileDocType) {
                val query = db.getAllContent(docType)
                allContent.addAll(query!!)
            }
        }
        return allContent
    }
}
