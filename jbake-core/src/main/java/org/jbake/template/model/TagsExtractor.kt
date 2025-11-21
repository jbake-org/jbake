package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.FileUtil
import org.jbake.app.configuration.PropertyList
import org.jbake.template.ModelExtractor

class TagsExtractor : ModelExtractor<DocumentList<*>> {

    override fun get(db: ContentStore, model: MutableMap<*, *>, key: String): DocumentList<*> {
        val dl = DocumentList<TemplateModel?>()
        val templateModel = TemplateModel()
        templateModel.putAll(model)
        val config: MutableMap<*, *> = templateModel.config

        val tagPath: String? = config.get(PropertyList.TAG_PATH.key.replace(".", "_")).toString()

        for (tag in db.allTags) {
            val newTag = TemplateModel()
            val tagName = tag
            newTag.name = tagName

            val uri = tagPath + FileUtil.URI_SEPARATOR_CHAR + tag + config.get(PropertyList.OUTPUT_EXTENSION.key.replace(".", "_"))
                .toString()

            newTag.uri = uri
            newTag.taggedPosts = db.getPublishedPostsByTag(tagName)
            newTag.taggedDocuments = db.getPublishedDocumentsByTag(tagName)
            dl.push(newTag)
        }
        return dl
    }
}
