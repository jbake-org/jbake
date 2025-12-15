package org.jbake.util

import org.jbake.template.model.JbakeTemplateModel
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit


fun instantNowMs() = Instant.now().truncatedTo(ChronoUnit.MILLIS)
fun instantNowSec() = Instant.now().truncatedTo(ChronoUnit.SECONDS)
fun OffsetDateTime.ms() = this.truncatedTo(ChronoUnit.MILLIS)
fun OffsetDateTime.sec() = this.truncatedTo(ChronoUnit.SECONDS)

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
fun convertTemporalsInModelToJavaUtilDate(model: Map<String, Any>, timeZoneId: ZoneId): JbakeTemplateModel {
    val converted = doConvertDatesInModel(model, timeZoneId)
    return when (converted) {
        is JbakeTemplateModel -> converted
        is Map<*, *> -> JbakeTemplateModel.fromMap(converted)
        //is null ->  throw Exception("Unexpected type returned from conversion: ${model1.javaClass.simpleName}")
        else -> throw Exception("Unexpected type returned from conversion: ${converted?.javaClass?.simpleName}")
    }
}

private fun doConvertDatesInModel(value: Any?, timeZoneId: ZoneId): Any?
    = when (value) {
    is Map<*, *> -> value.mapValues { (_, v) -> doConvertDatesInModel(v, ZoneId.systemDefault()) } // Also handles DocumentModel etc.
    is Collection<*> -> value.map { doConvertDatesInModel(it, ZoneId.systemDefault()) }
    else -> convertScalar(value, timeZoneId)
}

private fun convertScalar(value: Any?, timeZoneId: ZoneId): Any?
    = when (value) {
    is java.time.OffsetDateTime -> java.util.Date.from(value.toInstant())
    is java.time.ZonedDateTime -> java.util.Date.from(value.toInstant())
    is java.time.Instant -> java.util.Date.from(value)
    is java.time.LocalDateTime -> java.util.Date.from(value.atZone(timeZoneId).toInstant())
    is java.time.LocalDate -> java.util.Date.from(value.atStartOfDay(timeZoneId).toInstant())
    else -> value
}
