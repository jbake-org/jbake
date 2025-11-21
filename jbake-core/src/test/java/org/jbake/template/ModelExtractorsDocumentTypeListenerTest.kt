package org.jbake.template

import org.assertj.core.api.Assertions.assertThat
import org.jbake.model.DocumentTypes.addDocumentType
import org.junit.Test

class ModelExtractorsDocumentTypeListenerTest {
    @Test
    fun shouldRegisterExtractorsForCustomType() {
        // given: "A document type is known"
        val newDocumentType = "project"
        addDocumentType(newDocumentType)
        val listener = ModelExtractorsDocumentTypeListener()

        // when: "the listener is called with that type"
        listener.added(newDocumentType)

        // then: "an extractor is registered by pluralized type as key"
        assertThat(ModelExtractors.instance.containsKey("projects")).isTrue()

        // and: "an extractor for published types is registered"
        assertThat(ModelExtractors.instance.containsKey("published_projects")).isTrue()
    }
}
