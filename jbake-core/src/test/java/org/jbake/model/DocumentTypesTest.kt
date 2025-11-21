package org.jbake.model

import org.assertj.core.api.Assertions
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
        val expectedDocumentType: Array<String> = arrayOf<String>("page", "post", "masterindex", "archive", "feed")

        Assertions.assertThat<String>(knownDocumentTypes).contains(*expectedDocumentType)
    }

    @Test
    fun shouldAddNewDocumentType() {
        val newDocumentType = "newDocumentType"

        addDocumentType(newDocumentType)

        Assertions.assertThat<String>(documentTypes).contains(newDocumentType)
    }

    @Test
    fun shouldAddDocumentTypeOnlyOnce() {
        // A a document type is already known
        val knownDocumentType = "known"
        addDocumentType(knownDocumentType)

        // adding the known document type again
        addDocumentType(knownDocumentType)

        // only one document type could be found in the list
        Assertions.assertThat<String>(documentTypes).containsOnlyOnce(knownDocumentType)
    }

    @Test
    fun shouldTellIfDocumentTypeIsKnown() {
        val knownDocumentType = "known"
        addDocumentType(knownDocumentType)

        Assertions.assertThat(contains(knownDocumentType)).isTrue()
    }

    @Test
    fun shouldTellIfDocumentTypeIsUnknown() {
        val unknownType = "unknown"

        Assertions.assertThat(contains(unknownType)).isFalse()
    }

    @Test
    fun shouldNotifyListenersWhenNewDocumentTypeIsAdded() {
        // A DocumentTypeListener is added
        val newDocumentType = "newDocumentType"
        val listener = Mockito.mock<DocumentTypeListener?>(DocumentTypeListener::class.java)
        addListener(listener)

        // a new document type added
        addDocumentType(newDocumentType)

        // the listener was called with new document type
        Mockito.verify<DocumentTypeListener?>(listener).added(newDocumentType)
    }
}
