package org.jbake.template

import org.jbake.model.DocumentTypeRegistry.addDocumentType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ModelExtractorsDocumentTypeListenerTest : StringSpec({
    "shouldRegisterExtractorsForCustomType" {
        // Given: "A document type is known."
        val newDocumentType = "project"
        addDocumentType(newDocumentType)
        val listener = ModelExtractorsDocumentTypeListener()

        // When: "the listener is called with that type."
        listener.added(newDocumentType)

        // Then: "an extractor is registered by pluralized type as key."
        ModelExtractors.instance.containsKey("projects") shouldBe true

        // And: "an extractor for published types is registered."
        ModelExtractors.instance.containsKey("published_projects") shouldBe true
    }
})
