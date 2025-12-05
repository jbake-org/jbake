package org.jbake.model

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Base model class for JBake models.
 * Uses delegation instead of inheritance for better design.
 * TODO: Refactor not to delegate all MutableMap, but the individual selected methods. See below.
 */
abstract class BaseModel(
    val delegate: MutableMap<String, Any> = HashMap()
)
    : MutableMap<String, Any> by delegate
{
    // Override get to return null for missing keys instead of relying on Map's default behavior.
    // This allows FreeMarker's classic_compatible mode to handle missing properties gracefully.
    override fun get(key: String): Any? = delegate[key]

    var uri: String
        get() = (get(ModelAttributes.FS_DOC_OUTPUT_URI) as String?) ?: ""
        set(uri) { put(ModelAttributes.FS_DOC_OUTPUT_URI, uri) }

    var name: String
        get() = (get(ModelAttributes.DOC_NAME) as String?) ?: ""
        set(name) { put(ModelAttributes.DOC_NAME, name) }

    var test: String by mapDelegate("doc_name")

    fun putOrRemoveIfNull(key: String, value: Any?) {
        if (value == null) remove(key)
        else put(key, value)
    }
}


private class MapPropertyDelegate<T>(
    private val map: MutableMap<String, Any>,
    private val key: String,
    private val kClass: KClass<*>, // Will be KClass<T>
    private val isNullable: Boolean // Flag to handle null checks
) : ReadWriteProperty<Any?, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {

        val value = map[key] ?:
            if (isNullable) return null as T
            else throw IllegalStateException("Non-nullable property '$key' is null in the map.")

        // Value is present, perform type check
        return value as? T ?: throw Exception("Property '$key' is not of expected type ${kClass.simpleName}. Actual value: $value of type ${value.javaClass.simpleName}")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (value != null)
            map[key] = value
        else if (isNullable)
            map.remove(key)
        else
            throw IllegalArgumentException("Cannot set null to non-nullable property '$key'")
    }
}

// 💥 The inline reified factory function
private inline fun <reified T> BaseModel.mapDelegate(key: String): MapPropertyDelegate<T> {
    // 1. Get the KClass using reified T
    val kClass = T::class

    // 2. Check nullability using Kotlin Reflection on the reified type
    val isNullable = null is T // True if T is String?, false if T is String

    // 3. Create and return the delegate
    return MapPropertyDelegate(this.delegate, key, kClass, isNullable)
}
