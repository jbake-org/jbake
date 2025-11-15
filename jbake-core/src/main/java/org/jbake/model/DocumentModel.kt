package org.jbake.model

import org.jbake.app.DBUtil
import java.util.*

class DocumentModel : BaseModel() {
    var body: String?
        get() = get(ModelAttributes.BODY) as String?
        set(body) {
            put(ModelAttributes.BODY, body)
        }

    var date: Date?
        get() = get(ModelAttributes.DATE) as Date?
        set(date) {
            put(ModelAttributes.DATE, date)
        }

    var status: String?
        get() {
            if (containsKey(ModelAttributes.STATUS)) {
                return get(ModelAttributes.STATUS) as String?
            }
            return ""
        }
        set(status) {
            put(ModelAttributes.STATUS, status)
        }

    var type: String
        get() {
            if (containsKey(ModelAttributes.TYPE)) {
                return get(ModelAttributes.TYPE) as String
            }
            return ""
        }
        set(type) {
            put(ModelAttributes.TYPE, type)
        }

    var tags: Array<String>
        get() = DBUtil.toStringArray(get(ModelAttributes.TAGS))
        set(tags) {
            put(ModelAttributes.TAGS, tags)
        }

    var sha1: String?
        get() = get(ModelAttributes.SHA1) as String?
        set(sha1) {
            put(ModelAttributes.SHA1, sha1)
        }

    val sourceuri: String?
        get() = get(ModelAttributes.SOURCE_URI) as String?

    fun setSourceUri(uri: String?) {
        put(ModelAttributes.SOURCE_URI, uri)
    }

    var rootPath: String?
        get() = get(ModelAttributes.ROOTPATH) as String?
        set(pathToRoot) {
            put(ModelAttributes.ROOTPATH, pathToRoot)
        }

    var rendered: Boolean?
        get() = getOrDefault(ModelAttributes.RENDERED, false) as Boolean?
        set(rendered) {
            put(ModelAttributes.RENDERED, rendered)
        }

    var file: String?
        get() = get(ModelAttributes.FILE) as String?
        set(path) {
            put(ModelAttributes.FILE, path)
        }

    var noExtensionUri: String?
        get() = get(ModelAttributes.NO_EXTENSION_URI) as String?
        set(noExtensionUri) {
            put(ModelAttributes.NO_EXTENSION_URI, noExtensionUri)
        }

    var title: String?
        get() = get(ModelAttributes.TITLE) as String?
        set(title) {
            put(ModelAttributes.TITLE, title)
        }

    var cached: Boolean?
        get() {
            val value = get(ModelAttributes.CACHED)
            if (value is String) {
                return value.toBoolean()
            } else {
                return value as Boolean?
            }
        }
        set(cached) {
            put(ModelAttributes.CACHED, cached)
        }

    fun setNextContent(nextDocumentModel: DocumentModel?) {
        put(ModelAttributes.NEXT_CONTENT, nextDocumentModel)
    }

    fun setPreviousContent(previousDocumentModel: DocumentModel?) {
        put(ModelAttributes.PREVIOUS_CONTENT, previousDocumentModel)
    }

    companion object {
        @JvmStatic
        fun createDefaultDocumentModel(): DocumentModel {
            val documentModel = DocumentModel()
            documentModel.cached = true
            documentModel.rendered = false
            return documentModel
        }
    }
}
