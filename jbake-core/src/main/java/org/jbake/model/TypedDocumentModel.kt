package org.jbake.model

import org.jbake.app.DbUtils
import java.util.*

/**
 * Type-safe document model that replaces the HashMap-based DocumentModel.
 * Uses composition instead of inheritance for better type safety.
 */
class TypedDocumentModel : TypedBaseModel() {

    var body: String
        get() = get(ModelAttributes.BODY) as? String ?: ""
        set(value) { set(ModelAttributes.BODY, value) }

    var date: Date?
        get() = get(ModelAttributes.DATE) as? Date
        set(value) { putOrRemoveIfNull(ModelAttributes.DATE, value) }

    var status: String?
        get() = get(ModelAttributes.STATUS) as? String ?: ""
        set(value) { putOrRemoveIfNull(ModelAttributes.STATUS, value) }

    var type: String
        get() = get(ModelAttributes.TYPE) as? String ?: ""
        set(value) { set(ModelAttributes.TYPE, value) }

    var tags: Array<String>
        get() {
            val entry = get(ModelAttributes.TAGS) ?: return emptyArray()
            return DbUtils.toStringArray(entry)
        }
        set(value) { set(ModelAttributes.TAGS, value) }

    var sha1: String?
        get() = get(ModelAttributes.SHA1) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.SHA1, value) }

    var sourceUri: String?
        get() = get(ModelAttributes.SOURCE_URI) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.SOURCE_URI, value) }

    var rootPath: String
        get() = get(ModelAttributes.ROOTPATH) as? String ?: ""
        set(value) { set(ModelAttributes.ROOTPATH, value) }

    var rendered: Boolean
        get() = get(ModelAttributes.RENDERED) as? Boolean ?: false
        set(value) { set(ModelAttributes.RENDERED, value) }

    var file: String?
        get() = get(ModelAttributes.FILE) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.FILE, value) }

    var noExtensionUri: String?
        get() = get(ModelAttributes.NO_EXTENSION_URI) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.NO_EXTENSION_URI, value) }

    var title: String?
        get() = get(ModelAttributes.TITLE) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.TITLE, value) }

    var cached: Boolean?
        get() {
            val value = get(ModelAttributes.CACHED)
            return when (value) {
                is String -> value.toBoolean()
                is Boolean -> value
                else -> null
            }
        }
        set(value) { putOrRemoveIfNull(ModelAttributes.CACHED, value) }

    /**
     * Create a copy of this document model.
     */
    fun copy(): TypedDocumentModel {
        val copy = TypedDocumentModel()
        copy.putAll(toMap())
        return copy
    }

    companion object {
        /**
         * Create a TypedDocumentModel from an existing DocumentModel.
         * This is a migration helper.
         */
        fun fromLegacy(legacy: DocumentModel): TypedDocumentModel {
            val typed = TypedDocumentModel()
            // Copy all properties from the HashMap
            for ((key, value) in legacy) {
                typed[key] = value
            }
            return typed
        }

        /**
         * Convert TypedDocumentModel to legacy DocumentModel.
         * This is a temporary bridge during migration.
         */
        fun toLegacy(typed: TypedDocumentModel): DocumentModel {
            val legacy = DocumentModel()
            legacy.putAll(typed.toMap())
            return legacy
        }
    }
}

