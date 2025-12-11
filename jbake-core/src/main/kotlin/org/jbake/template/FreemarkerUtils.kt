package org.jbake.template

import freemarker.template.*
import freemarker.template.TemplateDateModel.DATETIME
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*


// Wrappers to convert Java 8 date/time types to Freemarker TemplateDateModels.
// Kept for potential special-case usage but are not required when using Java8ObjectWrapper.
// TBD Currently not used; freemarker-java8 instead -> remove when stable.

class OffsetDateTimeModel(private val dateTime: OffsetDateTime) : TemplateDateModel {
    override fun getDateType() = DATETIME
    override fun getAsDate(): Date = Date.from(dateTime.toInstant())
}

class InstantModel(private val instant: Instant) : TemplateDateModel {
    override fun getDateType() = DATETIME
    override fun getAsDate(): Date = Date.from(instant)
}


/**
 * Custom ObjectWrapper that wraps all Maps with NullSafeMapModel.
 */
class TemporalsObjectWrapper(incompatibleImprovements: Version)
    : DefaultObjectWrapper(incompatibleImprovements) {

    override fun wrap(obj: Any?): TemplateModel? {
        return when(obj) {
            is OffsetDateTime -> OffsetDateTimeModel(obj)
            is Instant        -> InstantModel(obj)
            null -> null
            else -> super.wrap(obj)
        }
    }
}

/**
 * Custom ObjectWrapper that wraps all OffsetDateTime in OffsetDateTimeModel.
 */
private class NullSafeObjectWrapper(incompatibleImprovements: Version)
    : DefaultObjectWrapper(incompatibleImprovements) {

    override fun wrap(obj: Any?): freemarker.template.TemplateModel {
        if (obj is Map<*, *>) {
            // Wrap maps with our null-safe wrapper
            val simpleHash = super.wrap(obj) as? SimpleHash
            return if (simpleHash != null) NullSafeMapModel(simpleHash, this) else super.wrap(obj)
        }
        return super.wrap(obj)
    }
}

/**
 * Recursive wrapper for SimpleHash that returns null for missing keys instead of throwing exceptions.
 * This allows FreeMarker's classic_compatible mode to work correctly with ${content.author} when the author key is missing.
 */
private class NullSafeMapModel(
    private val delegate: SimpleHash,
    private val wrapper: ObjectWrapper
) : TemplateHashModel {

    override fun get(key: String): freemarker.template.TemplateModel? {
        // If the value is another map, wrap it too.
        return try {
            delegate.get(key).let { if (it is SimpleHash) NullSafeMapModel(it, wrapper) else it }
        }
        // Return null for missing keys instead of throwing.
        catch (_: TemplateModelException) { null }
    }

    override fun isEmpty(): Boolean = delegate.isEmpty
}
