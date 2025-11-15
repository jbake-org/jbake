package org.jbake.model

import org.jbake.parser.Engines

/**
 *
 * Utility class used to determine the list of document types. Currently only supports "page", "post", "index",
 * "archive" and "feed".
 *
 * Additional document types are added at runtime based on the types found in the configuration.
 *
 * @author Cédric Champeau
 */
object DocumentTypes {

    private val DEFAULT_DOC_TYPES: MutableSet<String> = LinkedHashSet()
    private val LISTENERS: MutableSet<DocumentTypeListener> = HashSet()

    init {
        resetDocumentTypes()
    }

    @JvmStatic
    fun resetDocumentTypes() {
        DEFAULT_DOC_TYPES.clear()
        DEFAULT_DOC_TYPES.addAll(mutableListOf("page", "post", "masterindex", "archive", "feed"))
    }


    @JvmStatic
    fun addDocumentType(docType: String) {
        DEFAULT_DOC_TYPES.add(docType)
        notifyListener(docType)
    }

    private fun notifyListener(docType: String) {
        for (listener in LISTENERS) {
            listener.added(docType)
        }
    }

    @JvmStatic
    fun addListener(listener: DocumentTypeListener?) {
        LISTENERS.add(listener!!)
    }

    @JvmStatic
    val documentTypes: Array<String>
        /**
         * Notice additional document types are added automagically before returning them
         *
         * @return all supported document types
         */
        get() {
            // TODO: is this needed?
            // make sure engines are loaded before to get document types
            Engines.recognizedExtensions
            return DEFAULT_DOC_TYPES.toTypedArray()
        }

    @JvmStatic
    fun contains(documentType: String?): Boolean {
        return DEFAULT_DOC_TYPES.contains(documentType)
    }
}
