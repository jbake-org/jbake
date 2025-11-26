package org.jbake.app.configuration

import java.util.*

class Property @JvmOverloads constructor(
    @JvmField val key: String,
    @JvmField val description: String?,
    @JvmField val group: Group = Group.DEFAULT
) : Comparable<Property> {

    override fun toString() = this.key

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val property = other as Property
        return key == property.key &&
                description == property.description && group == property.group
    }

    override fun hashCode(): Int {
        return Objects.hash(key, description, group)
    }

    override fun compareTo(other: Property): Int {
        var result = this.group.compareTo(other.group)

        if (result == 0) {
            result = this.key.compareTo(other.key)
        }
        return result
    }

    enum class Group {
        DEFAULT, CUSTOM
    }
}
