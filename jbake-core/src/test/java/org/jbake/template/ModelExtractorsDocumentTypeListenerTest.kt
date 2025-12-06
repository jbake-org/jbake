package org.jbake.template

import org.jbake.model.DocumentTypeRegistry.addDocumentType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.jbake.model.ModelExtractorsDocumentTypeListener

class ModelExtractorsDocumentTypeListenerTest : StringSpec({
    "shouldRegisterExtractorsForCustomType" {
        // Given: "A document type is known."
        val newDocumentType = "project"
        addDocumentType(newDocumentType)
        val listener = ModelExtractorsDocumentTypeListener()

        // When: "the listener is called with that type."
        listener.onAdded(newDocumentType)

        // Then: "an extractor is registered by pluralized type as key."
        ModelExtractorsRegistry.instance.containsKey("projects") shouldBe true

        // And: "an extractor for published types is registered."
        ModelExtractorsRegistry.instance.containsKey("published_projects") shouldBe true
    }
})
