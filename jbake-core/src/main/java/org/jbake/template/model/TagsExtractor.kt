package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.FileUtil
import org.jbake.template.ModelExtractor

class TagsExtractor : ModelExtractor<DocumentList<*>?> {
    override fun get(db: ContentStore, model: MutableMap<*, *>?, key: String?): DocumentList<*> {
        val dl = DocumentList<TemplateModel?>()
        val templateModel = TemplateModel()
        templateModel.putAll(model)
        val config: MutableMap<*, *> = templateModel.getConfig()

        val tagPath: String? = config.get(TAG_PATH.key.replace(".", "_")).toString()

        for (tag in db.getAllTags()) {
            val newTag = TemplateModel()
            val tagName = tag
            newTag.setName(tagName)

            val uri = tagPath + FileUtil.URI_SEPARATOR_CHAR + tag + config.get(OUTPUT_EXTENSION.key.replace(".", "_"))
                .toString()

            newTag.setUri(uri)
            newTag.setTaggedPosts(db.getPublishedPostsByTag(tagName))
            newTag.setTaggedDocuments(db.getPublishedDocumentsByTag(tagName))
            dl.push(newTag)
        }
        return dl
    }
}
