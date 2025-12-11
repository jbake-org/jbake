package org.jbake.util

import org.jbake.template.model.JbakeTemplateModel
import java.time.ZoneId


fun Map<Any?, Any?>.mapOfStringToAny(): Map<String, Any> {
    @Suppress("UNCHECKED_CAST")
    return this.mapValues {
        if (it.key !is String) throw Exception("Map keys must be Strings.")
        if (it.value == null) throw Exception("Map values must not be null.")
        it.value!!
    } as Map<String, Any>
}



/**
 * Recursively convert Java time types to java.util.Date in the model for Freemarker compatibility.
 * Handles OffsetDateTime, ZonedDateTime, Instant, LocalDateTime and LocalDate.
 */
fun convertTemporalsInModelToJavaUtilDate(model: Map<String, Any>): JbakeTemplateModel {
    val converted = doConvertDatesInModel(model)
    return when (converted) {
        is JbakeTemplateModel -> converted
        is Map<*, *> -> JbakeTemplateModel.fromMap(converted)
        //is null ->  throw Exception("Unexpected type returned from conversion: ${model1.javaClass.simpleName}")
        else -> throw Exception("Unexpected type returned from conversion: ${converted?.javaClass?.simpleName}")
    }
}

private fun doConvertDatesInModel(value: Any?): Any?
    = when (value) {
    is Map<*, *> -> value.mapValues { (_, v) -> doConvertDatesInModel(v) } // Also handles DocumentModel etc.
    is Collection<*> -> value.map { doConvertDatesInModel(it) }
    else -> convertScalar(value)
}

private fun convertScalar(value: Any?): Any?
    = when (value) {
    is java.time.OffsetDateTime -> java.util.Date.from(value.toInstant())
    is java.time.ZonedDateTime -> java.util.Date.from(value.toInstant())
    is java.time.Instant -> java.util.Date.from(value)
    is java.time.LocalDateTime -> java.util.Date.from(value.atZone(ZoneId.systemDefault()).toInstant())
    is java.time.LocalDate -> java.util.Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant())
    else -> value
}
