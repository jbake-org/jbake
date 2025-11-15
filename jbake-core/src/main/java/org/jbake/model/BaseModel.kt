package org.jbake.model

abstract class BaseModel : HashMap<String?, Any?>() {
    var uri: String?
        get() = get(ModelAttributes.URI) as String?
        set(uri) {
            put(ModelAttributes.URI, uri)
        }

    fun setName(name: String?) {
        put(ModelAttributes.NAME, name)
    }
}
