package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.app.FileUtil
import org.jbake.template.TypedModelExtractor

class TagsExtractor : TypedModelExtractor<DocumentList<*>> {

    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        val cfg = context.config
        val tagPath = cfg.tagPathName?.removeSuffix(FileUtil.URI_SEPARATOR_CHAR) ?: ""

        val dl = DocumentList<TemplateModel>()

        for (tag in context.db.allTags) {
            val newTag = TemplateModel()
            newTag.name = tag

            val uri = if (tagPath.isNotEmpty()) {
                tagPath + FileUtil.URI_SEPARATOR_CHAR + tag + (cfg.outputExtension ?: "")
            } else {
                tag + (cfg.outputExtension ?: "")
            }

            newTag.uri = uri
            newTag.taggedPosts = context.db.getPublishedPostsByTag(tag)
            newTag.taggedDocuments = context.db.getPublishedDocumentsByTag(tag)
            dl.push(newTag)
        }
        return dl
    }
}
