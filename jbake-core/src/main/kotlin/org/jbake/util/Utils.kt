package org.jbake.util



fun Map<Any?, Any?>.mapOfStringToAny(): Map<String, Any> {
    @Suppress("UNCHECKED_CAST")
    return this.mapValues {
        if (it.key !is String) throw Exception("Map keys must be Strings.")
        if (it.value == null) throw Exception("Map values must not be null.")
        it.value!!
    } as Map<String, Any>
}
