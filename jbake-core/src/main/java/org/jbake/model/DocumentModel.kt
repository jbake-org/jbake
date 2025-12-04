package org.jbake.model

import org.jbake.app.DbUtils
import java.util.*

class DocumentModel : BaseModel() {

    var body: String
        get() = get(ModelAttributes.DOC_BODY_RENDERED) as String
        set(body) { put(ModelAttributes.DOC_BODY_RENDERED, body) }

    var date: Date?
        get() = get(ModelAttributes.DOC_DATE) as Date?
        set(date) = putOrRemoveIfNull(ModelAttributes.DOC_DATE, date)

    var status: String?
        get() = if (containsKey(ModelAttributes.DOC_STATUS)) get(ModelAttributes.DOC_STATUS) as String? else ""
        set(status) = putOrRemoveIfNull(ModelAttributes.DOC_STATUS, status)

    var type: String
        get() = if (containsKey(ModelAttributes.DOC_TYPE)) get(ModelAttributes.DOC_TYPE) as String else ""
        set(type) { put(ModelAttributes.DOC_TYPE, type) }

    var tags: List<String>
        get() = DbUtils.toStringList(get(ModelAttributes.DOC_TAGS) ?: emptyList<String>())
        set(tags) { put(ModelAttributes.DOC_TAGS, tags) }

    var sha1: String?
        get() = get(ModelAttributes.FS_DOC_SHA1) as String?
        set(sha1) { putOrRemoveIfNull(ModelAttributes.FS_DOC_SHA1, sha1) }

    var sourceUri: String?
        get() = get(ModelAttributes.FS_DOC_SOURCE_REL_URI) as String?
        set(uri) { putOrRemoveIfNull(ModelAttributes.FS_DOC_SOURCE_REL_URI, uri) }

    var rootPath: String
        get() = get(ModelAttributes.FS_REL_FROM_DOC_TO_SITEROOT) as String
        set(pathToRoot) { put(ModelAttributes.FS_REL_FROM_DOC_TO_SITEROOT, pathToRoot) }

    var rendered: Boolean
        get() = getOrDefault(ModelAttributes.FS_DOC_WAS_RENDERED, false) as Boolean
        set(rendered) { put(ModelAttributes.FS_DOC_WAS_RENDERED, rendered) }

    var file: String?
        get() = get(ModelAttributes.FS_DOC_SOURCE_PATH_ABS) as String?
        set(path) { putOrRemoveIfNull(ModelAttributes.FS_DOC_SOURCE_PATH_ABS, path) }

    var noExtensionUri: String?
        get() = get(ModelAttributes.FS_DOC_OUTPUT_URI_NOEXT) as String?
        set(noExtensionUri) { putOrRemoveIfNull(ModelAttributes.FS_DOC_OUTPUT_URI_NOEXT, noExtensionUri) }

    var title: String?
        get() = get(ModelAttributes.DOC_TITLE) as String?
        set(title) { putOrRemoveIfNull(ModelAttributes.DOC_TITLE, title) }

    var cached: Boolean?
        get() {
            val value = get(ModelAttributes.FS_DOC_IS_CACHED_IN_DB)
            return if (value is String) value.toBoolean() else value as Boolean?
        }
        set(cached) { putOrRemoveIfNull(ModelAttributes.FS_DOC_IS_CACHED_IN_DB, cached) }

    var nextContent: DocumentModel?
        get() = get(ModelAttributes.PAGI_NEXT_CONTENT) as DocumentModel?
        set(nextDocumentModel) { putOrRemoveIfNull(ModelAttributes.PAGI_NEXT_CONTENT, nextDocumentModel) }

    var previousContent: DocumentModel?
        get() = get(ModelAttributes.PAGI_PREV_CONTENT) as DocumentModel?
        set(previousDocumentModel) { putOrRemoveIfNull(ModelAttributes.PAGI_PREV_CONTENT, previousDocumentModel) }


    companion object {
        @JvmStatic
        fun createDefaultDocumentModel(): DocumentModel = DocumentModel().apply {
            cached = true
            rendered = false
        }
    }
}
