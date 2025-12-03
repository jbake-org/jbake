package org.jbake.model

/**
 * Base model class for JBake models.
 * Uses delegation instead of inheritance for better design.
 */
abstract class BaseModel(
    private val delegate: MutableMap<String, Any> = HashMap()
)
    : MutableMap<String, Any> by delegate
{
    // Override get to return null for missing keys instead of relying on Map's default behavior.
    // This allows FreeMarker's classic_compatible mode to handle missing properties gracefully.
    override fun get(key: String): Any? = delegate[key]

    var uri: String
        get() = (get(ModelAttributes.URI) as String?) ?: ""
        set(uri) { put(ModelAttributes.URI, uri) }

    var name: String
        get() = (get(ModelAttributes.NAME) as String?) ?: ""
        set(name) { put(ModelAttributes.NAME, name) }

    fun putOrRemoveIfNull(key: String, value: Any?) {
        if (value == null) remove(key)
        else put(key, value)
    }
}
