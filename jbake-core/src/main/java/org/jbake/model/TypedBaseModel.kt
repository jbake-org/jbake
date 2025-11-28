package org.jbake.model

/**
 * New base model that uses composition instead of inheritance from HashMap.
 * Provides typed accessors while maintaining flexibility for custom attributes.
 */
abstract class TypedBaseModel {
    // Internal storage for attributes
    private val attributes: MutableMap<String, Any> = mutableMapOf()

    // Common properties
    var uri: String
        get() = attributes[ModelAttributes.URI] as? String ?: ""
        set(value) { attributes[ModelAttributes.URI] = value }

    var name: String
        get() = attributes[ModelAttributes.NAME] as? String ?: ""
        set(value) { attributes[ModelAttributes.NAME] = value }

    /**
     * Get a custom attribute by key.
     */
    operator fun get(key: String): Any? = attributes[key]

    /**
     * Set a custom attribute.
     */
    operator fun set(key: String, value: Any?) {
        if (value == null) attributes.remove(key)
        else attributes[key] = value
    }

    /**
     * Check if an attribute exists.
     */
    fun containsKey(key: String): Boolean = attributes.containsKey(key)

    /**
     * Remove an attribute.
     */
    fun remove(key: String): Any? = attributes.remove(key)

    /**
     * Get all attribute keys.
     */
    val keys: Set<String> get() = attributes.keys

    /**
     * Get all entries.
     */
    val entries: Set<Map.Entry<String, Any>> get() = attributes.entries

    /**
     * Put or remove if null - convenience method.
     */
    fun putOrRemoveIfNull(key: String, value: Any?) {
        if (value == null) attributes.remove(key)
        else attributes[key] = value
    }

    /**
     * Convert to map for backward compatibility.
     * Should be used sparingly during migration.
     */
    @Deprecated("Direct map access should be avoided. Use typed properties.")
    fun toMap(): Map<String, Any> = attributes.toMap()

    /**
     * Put all from another map.
     */
    fun putAll(other: Map<String, Any>) {
        attributes.putAll(other)
    }
}

