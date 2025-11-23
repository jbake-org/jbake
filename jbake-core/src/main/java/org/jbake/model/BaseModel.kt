package org.jbake.model

abstract class BaseModel : HashMap<String, Any>() {

    var uri: String
        get() = get(ModelAttributes.URI) as String
        set(uri) {
            put(ModelAttributes.URI, uri)
        }

    var name: String
        get() = get(ModelAttributes.NAME) as String
        set(name) {
            put(ModelAttributes.NAME, name)
        }

    fun putOrRemoveIfNull(key: String, value: Any?) {
        if (value == null) remove(key)
        else put(key, value)
    }
}
