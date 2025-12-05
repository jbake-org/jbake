package org.jbake.model

import org.jbake.app.DbUtils
import java.time.OffsetDateTime

/**
 * Type-safe document model that replaces the HashMap-based DocumentModel.
 * Uses composition instead of inheritance for better type safety.
 */
class TypedDocumentModel : TypedBaseModel() {

    var body: String
        get() = get(ModelAttributes.DOC_BODY_RENDERED) as? String ?: ""
        set(value) { set(ModelAttributes.DOC_BODY_RENDERED, value) }

    var date: OffsetDateTime?
        get() = get(ModelAttributes.DOC_DATE) as? OffsetDateTime
        set(value) { putOrRemoveIfNull(ModelAttributes.DOC_DATE, value) }

    var status: String?
        get() = get(ModelAttributes.DOC_STATUS) as? String ?: ""
        set(value) { putOrRemoveIfNull(ModelAttributes.DOC_STATUS, value) }

    var type: String
        get() = get(ModelAttributes.DOC_TYPE) as? String ?: ""
        set(value) { set(ModelAttributes.DOC_TYPE, value) }

    var tags: List<String>
        get() {
            val entry = get(ModelAttributes.DOC_TAGS) ?: return emptyList()
            return DbUtils.toStringList(entry)
        }
        set(value) { set(ModelAttributes.DOC_TAGS, value) }

    var sha1: String?
        get() = get(ModelAttributes.FS_DOC_SHA1) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.FS_DOC_SHA1, value) }

    var sourceUri: String?
        get() = get(ModelAttributes.FS_DOC_SOURCE_REL_URI) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.FS_DOC_SOURCE_REL_URI, value) }

    var rootPath: String
        get() = get(ModelAttributes.FS_REL_FROM_DOC_TO_SITEROOT) as? String ?: ""
        set(value) { set(ModelAttributes.FS_REL_FROM_DOC_TO_SITEROOT, value) }

    var rendered: Boolean
        get() = get(ModelAttributes.FS_DOC_WAS_RENDERED) as? Boolean ?: false
        set(value) { set(ModelAttributes.FS_DOC_WAS_RENDERED, value) }

    var file: String?
        get() = get(ModelAttributes.FS_DOC_SOURCE_PATH_ABS) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.FS_DOC_SOURCE_PATH_ABS, value) }

    var noExtensionUri: String?
        get() = get(ModelAttributes.FS_DOC_OUTPUT_URI_NOEXT) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.FS_DOC_OUTPUT_URI_NOEXT, value) }

    var title: String?
        get() = get(ModelAttributes.DOC_TITLE) as? String
        set(value) { putOrRemoveIfNull(ModelAttributes.DOC_TITLE, value) }

    var cached: Boolean?
        get() {
            val value = get(ModelAttributes.FS_DOC_IS_CACHED_IN_DB)
            return when (value) {
                is String -> value.toBoolean()
                is Boolean -> value
                else -> null
            }
        }
        set(value) { putOrRemoveIfNull(ModelAttributes.FS_DOC_IS_CACHED_IN_DB, value) }

    /**
     * Create a copy of this document model.
     */
    fun copy(): TypedDocumentModel {
        val copy = TypedDocumentModel()
        @Suppress("DEPRECATION")
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
            for ((key, value) in legacy)
                typed[key] = value
            return typed
        }

        /**
         * Convert TypedDocumentModel to legacy DocumentModel.
         * This is a temporary bridge during migration.
         */
        fun toLegacy(typed: TypedDocumentModel): DocumentModel {
            val legacy = DocumentModel()
            @Suppress("DEPRECATION")
            legacy.putAll(typed.toMap())
            return legacy
        }
    }
}

