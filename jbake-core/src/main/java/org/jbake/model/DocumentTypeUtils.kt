package org.jbake.model

object DocumentTypeUtils {
    fun unpluralize(pluralized: String): String {
        require(!((pluralized == null) || (pluralized.isEmpty()))) { "pluralized string should not be null or length should be bigger than zero" }
        val documentTypes = DocumentTypes.documentTypes

        val unpluralizedDoctype = pluralized.dropLast(1)
        if (DocumentTypes.contains(unpluralizedDoctype)) {
            return unpluralizedDoctype
        }
        throw UnsupportedOperationException(
            ("there is no document type pluralized as \"" + pluralized + "\"\n"
                    + "We only have " + documentTypes)
        )
    }

    fun pluralize(documentType: String): String {
        val documentTypes = DocumentTypes.documentTypes

        if (DocumentTypes.contains(documentType)) {
            return documentType + "s"
        }
        throw UnsupportedOperationException(
            ("there is no document type \"" + documentType + "\" we can pluralize\n"
                    + "We only have " + documentTypes)
        )
    }
}
