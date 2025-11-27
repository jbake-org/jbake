package org.jbake.model

import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.DocumentTypes.addListener
import org.jbake.model.DocumentTypes.contains
import org.jbake.model.DocumentTypes.documentTypes
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.*

class DocumentTypesTest : StringSpec({
    "shouldReturnDefaultDocumentTypes" {
        val knownDocumentTypes = documentTypes
        val expectedDocumentType = listOf("page", "post", "masterindex", "archive", "feed")

        for (type in expectedDocumentType) {
            knownDocumentTypes shouldContain type
        }
    }

    "shouldAddNewDocumentType" {
        val newDocumentType = "newDocumentType"
        addDocumentType(newDocumentType)
        documentTypes shouldContain newDocumentType
    }

    "shouldAddDocumentTypeOnlyOnce" {
        // A document type is already known.
        val knownDocumentType = "known"
        addDocumentType(knownDocumentType)

        // Adding the known document type again.
        addDocumentType(knownDocumentType)

        // Only one document type could be found in the list.
        documentTypes.count { it == knownDocumentType } shouldBe 1
    }

    "shouldTellIfDocumentTypeIsKnown" {
        val knownDocumentType = "known"
        addDocumentType(knownDocumentType)
        contains(knownDocumentType) shouldBe true
    }

    "shouldTellIfDocumentTypeIsUnknown" {
        val unknownType = "unknown"
        contains(unknownType) shouldBe false
    }

    "shouldNotifyListenersWhenNewDocumentTypeIsAdded" {
        // A DocumentTypeListener is added
        val newDocumentType = "newDocumentType"
        val listener = mockk<DocumentTypeListener>(relaxed = true)
        addListener(listener)

        // a new document type added
        addDocumentType(newDocumentType)

        // the listener was called with new document type
        verify { listener.added(newDocumentType) }
    }
})
