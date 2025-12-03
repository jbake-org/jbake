package org.jbake.model

import org.jbake.parser.ParserEnginesRegistry

/**
 * Utility class used to determine the list of document types.
 * Currently supports: "page", "post", "index", "archive" and "feed".
 *
 * Additional document types are added at runtime based on the types found in the configuration.
 */
object DocumentTypeRegistry {

    private val defaultDocTypes: MutableSet<String> = LinkedHashSet()
    private val listeners: MutableSet<DocumentTypeListener> = HashSet()

    init {
        resetDocumentTypes()
    }

    @JvmStatic fun resetDocumentTypes() {
        defaultDocTypes.clear()
        defaultDocTypes.addAll(listOf("page", "post", "masterindex", "archive", "feed"))
    }


    @JvmStatic fun addDocumentType(docType: String) {
        defaultDocTypes.add(docType)
        notifyListeners(docType)
    }

    private fun notifyListeners(docType: String) = listeners.forEach { it.onAdded(docType) }

    @JvmStatic fun addListener(listener: DocumentTypeListener) = listeners.add(listener)

    /** Clears registered listeners. Tests call this when they need a clean listener state between cases instead of relying on resetDocumentTypes to do so. */
    @JvmStatic fun clearListenersForTests() = listeners.clear()

    /** All supported document types. Additional document types are added automagically before returning. */
    @JvmStatic val documentTypes: Array<String>
        get() {
            // TODO: is this needed?
            // Make sure engines are loaded before to get document types.
            ParserEnginesRegistry.recognizedExtensions
            return defaultDocTypes.toTypedArray()
        }

    @JvmStatic fun contains(documentType: String) = defaultDocTypes.contains(documentType)
}


interface DocumentTypeListener {
    fun onAdded(doctype: String)
}


class ModelExtractorsDocumentTypeListener : DocumentTypeListener {
    override fun onAdded(doctype: String) {
        org.jbake.template.ModelExtractors.instance.registerExtractorsForCustomTypes(doctype)
    }
}
