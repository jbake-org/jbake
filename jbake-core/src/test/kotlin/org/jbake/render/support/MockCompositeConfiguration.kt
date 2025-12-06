package org.jbake.render.support

import org.apache.commons.configuration2.CompositeConfiguration


class MockCompositeConfiguration : CompositeConfiguration() {
    private var _bool = false
    private var _string: String? = "random string"

    fun withDefaultBoolean(bool: Boolean): MockCompositeConfiguration {
        _bool = bool
        return this
    }

    fun withInnerString(string: String?): MockCompositeConfiguration {
        _string = string
        return this
    }

    override fun getBoolean(key: String?): Boolean {
        if (super.containsKey(key)) {
            return super.getBoolean(key)
        }
        return _bool
    }

    override fun getString(key: String?): String? {
        if (super.containsKey(key)) {
            return super.getString(key)
        }
        return _string
    }
}
