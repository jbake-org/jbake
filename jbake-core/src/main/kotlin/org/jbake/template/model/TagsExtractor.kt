package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.app.FileUtil
import org.jbake.template.TypedModelExtractor
import org.slf4j.Logger
import org.jbake.util.Logging.logger

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
        // If tagPath is empty (either explicitly set to empty or absent) we should not add a prefix.
        val effectiveTagPath = tagPath // may be empty string meaning no prefix
        // If outputExtension is null, treat it as empty (no extension). Preserve empty string if explicitly set.
        val effectiveOutputExt = cfg.outputExtension ?: ""

        val dl = DocumentList<JbakeTemplateModel>()

        for (tag in context.db.allTags) {
            val newTag = JbakeTemplateModel()
            newTag.name = tag

            val uri = if (effectiveTagPath.isNotEmpty())
                effectiveTagPath + FileUtil.URI_SEPARATOR_CHAR + tag + effectiveOutputExt
            else tag + effectiveOutputExt

            newTag.uri = uri
            newTag.taggedPosts = context.db.getPublishedPostsByTag(tag)
            newTag.taggedDocuments = context.db.getPublishedDocumentsByTag(tag)
            dl.push(newTag)
        }
        return dl
    }

    private val log: Logger by logger()
}
