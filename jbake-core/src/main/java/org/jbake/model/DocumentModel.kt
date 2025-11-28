package org.jbake.model

import org.jbake.app.DbUtils
import java.util.*

class DocumentModel : BaseModel() {

    var body: String
        get() = get(ModelAttributes.BODY) as String
        set(body) { put(ModelAttributes.BODY, body) }

    var date: Date?
        get() = get(ModelAttributes.DATE) as Date?
        set(date) = putOrRemoveIfNull(ModelAttributes.DATE, date)

    var status: String?
        get() = if (containsKey(ModelAttributes.STATUS)) get(ModelAttributes.STATUS) as String? else ""
        set(status) = putOrRemoveIfNull(ModelAttributes.STATUS, status)

    var type: String
        get() = if (containsKey(ModelAttributes.TYPE)) get(ModelAttributes.TYPE) as String else ""
        set(type) { put(ModelAttributes.TYPE, type) }

    var tags: Array<String>
        get() = DbUtils.toStringArray(get(ModelAttributes.TAGS) ?: emptyArray<String>())
        set(tags) { put(ModelAttributes.TAGS, tags) }

    var sha1: String?
        get() = get(ModelAttributes.SHA1) as String?
        set(sha1) { putOrRemoveIfNull(ModelAttributes.SHA1, sha1) }

    var sourceUri: String?
        get() = get(ModelAttributes.SOURCE_URI) as String?
        set(uri) { putOrRemoveIfNull(ModelAttributes.SOURCE_URI, uri) }

    var rootPath: String
        get() = get(ModelAttributes.ROOTPATH) as String
        set(pathToRoot) { put(ModelAttributes.ROOTPATH, pathToRoot) }

    var rendered: Boolean
        get() = getOrDefault(ModelAttributes.RENDERED, false) as Boolean
        set(rendered) { put(ModelAttributes.RENDERED, rendered) }

    var file: String?
        get() = get(ModelAttributes.FILE) as String?
        set(path) { putOrRemoveIfNull(ModelAttributes.FILE, path) }

    var noExtensionUri: String?
        get() = get(ModelAttributes.NO_EXTENSION_URI) as String?
        set(noExtensionUri) { putOrRemoveIfNull(ModelAttributes.NO_EXTENSION_URI, noExtensionUri) }

    var title: String?
        get() = get(ModelAttributes.TITLE) as String?
        set(title) { putOrRemoveIfNull(ModelAttributes.TITLE, title) }

    var cached: Boolean?
        get() {
            val value = get(ModelAttributes.CACHED)
            return if (value is String) value.toBoolean() else value as Boolean?
        }
        set(cached) { putOrRemoveIfNull(ModelAttributes.CACHED, cached) }

    var nextContent: DocumentModel?
        get() = get(ModelAttributes.NEXT_CONTENT) as DocumentModel?
        set(nextDocumentModel) { putOrRemoveIfNull(ModelAttributes.NEXT_CONTENT, nextDocumentModel) }

    var previousContent: DocumentModel?
        get() = get(ModelAttributes.PREVIOUS_CONTENT) as DocumentModel?
        set(previousDocumentModel) { putOrRemoveIfNull(ModelAttributes.PREVIOUS_CONTENT, previousDocumentModel) }


    companion object {
        @JvmStatic
        fun createDefaultDocumentModel(): DocumentModel = DocumentModel().apply {
            cached = true
            rendered = false
        }
    }
}
