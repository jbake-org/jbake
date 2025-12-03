package org.jbake.template

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.jbake.model.DocumentTypeRegistry.addDocumentType

class ModelExtractorsTest : StringSpec({

    afterTest {
        ModelExtractors.instance.reset()
    }

    "shouldLoadExtractorsOnInstantiation" {
        val expectedKeys = ("pages,posts,indexs,archives,feeds,published_posts,published_pages," +
            "published_content,published_date,all_content,alltags,db,tag_posts,tags,tagged_documents").split(",")

        for (aKey in expectedKeys) {
            ModelExtractors.instance.containsKey(aKey).shouldBeTrue()
        }
    }

    "shouldRegisterExtractorsOnlyForCustomTypes" {
        val knownDocumentType = "alltag"
        addDocumentType(knownDocumentType)

        ModelExtractors.instance.registerExtractorsForCustomTypes(knownDocumentType)

        ModelExtractors.instance.containsKey("published_alltags").shouldBeFalse()
    }

    "shouldRegisterExtractorsForCustomType" {
        // A document type is known.
        val newDocumentType = "project"
        addDocumentType(newDocumentType)

        // When we register extractors for the new type.
        ModelExtractors.instance.registerExtractorsForCustomTypes(newDocumentType)

        // Then an extractor is registered by pluralized type as key.
        ModelExtractors.instance.containsKey("projects").shouldBeTrue()

        // And an extractor for published types is registered.
        ModelExtractors.instance.containsKey("published_projects").shouldBeTrue()
    }

    "shouldThrowAnExceptionIfDocumentTypeIsUnknown" {
        val unknownDocumentType = "unknown"
        shouldThrow<UnsupportedOperationException> {
            ModelExtractors.instance.registerExtractorsForCustomTypes(unknownDocumentType)
        }
    }

    "shouldResetToNonCustomizedExtractors" {
        // Given: A document type is known.
        val newDocumentType = "project"
        addDocumentType(newDocumentType)

        // When we register extractors for the new type.
        ModelExtractors.instance.registerExtractorsForCustomTypes(newDocumentType)

        // Expect: 18 extractors.
        ModelExtractors.instance.keySet().size shouldBe 18

        // When: reset.
        ModelExtractors.instance.reset()

        // Then: 16 extractors.
        ModelExtractors.instance.keySet().size shouldBe 16
    }
})

