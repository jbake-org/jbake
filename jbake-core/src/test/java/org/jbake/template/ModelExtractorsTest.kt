package org.jbake.template

import org.assertj.core.api.Assertions.assertThat
import org.jbake.model.DocumentTypes.addDocumentType
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class ModelExtractorsTest {
    @Rule @JvmField
    var thrown: ExpectedException = ExpectedException.none()

    @After
    fun tearDown() {
        ModelExtractors.instance.reset()
    }

    @Test
    fun shouldLoadExtractorsOnInstantiation() {
        ModelExtractors.instance
        val expectedKeys: Array<String> = arrayOf<String>(
            "pages",
            "posts",
            "indexs",
            "archives",
            "feeds",
            "published_posts",
            "published_pages",
            "published_content",
            "published_date",
            "all_content",
            "alltags",
            "db",
            "tag_posts",
            "tags",
            "tagged_documents",
        )

        for (aKey in expectedKeys) {
            assertThat(ModelExtractors.instance.containsKey(aKey)).isTrue()
        }
    }

    @Test
    fun shouldRegisterExtractorsOnlyForCustomTypes() {
        val knownDocumentType = "alltag"
        addDocumentType(knownDocumentType)

        ModelExtractors.instance.registerExtractorsForCustomTypes(knownDocumentType)

        assertThat(ModelExtractors.instance.containsKey("published_alltags")).isFalse()
    }

    @Test
    fun shouldRegisterExtractorsForCustomType() {
        // A document type is known
        val newDocumentType = "project"
        addDocumentType(newDocumentType)

        // when we register extractors for the new type
        ModelExtractors.instance.registerExtractorsForCustomTypes(newDocumentType)

        // then an extractor is registered by pluralized type as key
        assertThat(ModelExtractors.instance.containsKey("projects")).isTrue()

        // and an extractor for published types is registered
        assertThat(ModelExtractors.instance.containsKey("published_projects")).isTrue()
    }

    @Test
    fun shouldThrowAnExceptionIfDocumentTypeIsUnknown() {
        thrown.expect(UnsupportedOperationException::class.java)

        val unknownDocumentType = "unknown"
        ModelExtractors.instance.registerExtractorsForCustomTypes(unknownDocumentType)
    }

    @Test
    fun shouldResetToNonCustomizedExtractors() {
        //given:
        // A document type is known

        val newDocumentType = "project"
        addDocumentType(newDocumentType)

        // when we register extractors for the new type
        ModelExtractors.instance.registerExtractorsForCustomTypes(newDocumentType)

        //expect:
        assertThat(ModelExtractors.instance.keySet().size).isEqualTo(18)

        //when:
        ModelExtractors.instance.reset()

        //then:
        assertThat(ModelExtractors.instance.keySet().size).isEqualTo(16)
    }
}
