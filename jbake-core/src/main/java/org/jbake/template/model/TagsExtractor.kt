package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.app.FileUtil
import org.jbake.template.TypedModelExtractor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TagsExtractor : TypedModelExtractor<DocumentList<*>> {

    override fun extract(context: RenderContext, key: String): DocumentList<*> {
        val cfg = context.config
        val rawTagPath = cfg.tagPathName ?: ""
        // Normalize tagPath: remove any trailing slashes to avoid // when concatenated
        var tagPath = rawTagPath.trimEnd(FileUtil.URI_SEPARATOR_CHAR[0])
        // If configuration reconstruction failed and tagPath is empty, look into customData for a raw config map
        if (tagPath.isEmpty()) {
            val rawMap = context.customData["__raw_config_map"] as? Map<*, *>
            if (rawMap != null) {
                val candidate = (rawMap["tag.path"] ?: rawMap["tag_path"]) as? String
                if (!candidate.isNullOrEmpty()) {
                    tagPath = candidate.trimEnd(FileUtil.URI_SEPARATOR_CHAR[0])
                }
            }
        }
        // Fallback to sensible defaults if configuration reconstruction failed
        val effectiveTagPath = if (tagPath.isNotEmpty()) tagPath else ("tags")
        val effectiveOutputExt = if (!cfg.outputExtension.isNullOrEmpty()) cfg.outputExtension!! else ".html"

        val dl = DocumentList<TemplateModel>()

        for (tag in context.db.allTags) {
            val newTag = TemplateModel()
            newTag.name = tag

            val uri = if (effectiveTagPath.isNotEmpty()) {
                effectiveTagPath + FileUtil.URI_SEPARATOR_CHAR + tag + effectiveOutputExt
            } else {
                tag + effectiveOutputExt
            }

            newTag.uri = uri
            newTag.taggedPosts = context.db.getPublishedPostsByTag(tag)
            newTag.taggedDocuments = context.db.getPublishedDocumentsByTag(tag)
            dl.push(newTag)

            if (logger.isDebugEnabled()) {
                logger.debug("TagsExtractor: rawTagPath='{}', normalized='{}', tag='{}', uri='{}'", rawTagPath, tagPath, tag, uri)
            }
        }
        return dl
    }

    private val logger: Logger = LoggerFactory.getLogger(TagsExtractor::class.java)
}
