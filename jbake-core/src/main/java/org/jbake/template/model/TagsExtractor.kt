package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.FileUtil
import org.jbake.app.configuration.PropertyList
import org.jbake.template.ModelExtractor

class TagsExtractor : ModelExtractor<DocumentList<*>> {

    override fun get(db: ContentStore, model: MutableMap<String, Any>, key: String): DocumentList<*> {

        @Suppress("UNCHECKED_CAST")
        val config = model["config"] as MutableMap<String, Any>

        val tagPath = config[PropertyList.TAG_PATH.key.replace(".", "_")].toString()

        val dl = DocumentList<TemplateModel>()

        for (tag in db.allTags) {
            val newTag = TemplateModel()
            newTag.name = tag

            val uri = tagPath + FileUtil.URI_SEPARATOR_CHAR + tag + config[PropertyList.OUTPUT_EXTENSION.key.replace(".", "_")].toString()

            newTag.uri = uri
            newTag.taggedPosts = db.getPublishedPostsByTag(tag)
            newTag.taggedDocuments = db.getPublishedDocumentsByTag(tag)
            dl.push(newTag)
        }
        return dl
    }
}
