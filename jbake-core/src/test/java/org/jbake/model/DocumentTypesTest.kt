package org.jbake.model

import org.assertj.core.api.Assertions.assertThat
import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.DocumentTypes.addListener
import org.jbake.model.DocumentTypes.contains
import org.jbake.model.DocumentTypes.documentTypes
import org.junit.Test
import org.mockito.Mockito

class DocumentTypesTest {
    @Test
    fun shouldReturnDefaultDocumentTypes() {
        val knownDocumentTypes = documentTypes
        val expectedDocumentType: Array<String> = arrayOf("page", "post", "masterindex", "archive", "feed")

        assertThat(knownDocumentTypes).contains(*expectedDocumentType)
    }

    @Test
    fun shouldAddNewDocumentType() {
        val newDocumentType = "newDocumentType"

        addDocumentType(newDocumentType)

        assertThat(documentTypes).contains(newDocumentType)
    }

    @Test
    fun shouldAddDocumentTypeOnlyOnce() {
        // A document type is already known.
        val knownDocumentType = "known"
        addDocumentType(knownDocumentType)

        // Adding the known document type again.
        addDocumentType(knownDocumentType)

        // Only one document type could be found in the list.
        assertThat(documentTypes).containsOnlyOnce(knownDocumentType)
    }

    @Test
    fun shouldTellIfDocumentTypeIsKnown() {
        val knownDocumentType = "known"
        addDocumentType(knownDocumentType)

        assertThat(contains(knownDocumentType)).isTrue()
    }

    @Test
    fun shouldTellIfDocumentTypeIsUnknown() {
        val unknownType = "unknown"

        assertThat(contains(unknownType)).isFalse()
    }

    @Test
    fun shouldNotifyListenersWhenNewDocumentTypeIsAdded() {
        // A DocumentTypeListener is added
        val newDocumentType = "newDocumentType"
        val listener = Mockito.mock(DocumentTypeListener::class.java)
        addListener(listener)

        // a new document type added
        addDocumentType(newDocumentType)

        // the listener was called with new document type
        Mockito.verify(listener).added(newDocumentType)
    }
}
