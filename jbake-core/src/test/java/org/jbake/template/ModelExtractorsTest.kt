package org.jbake.template

import org.jbake.model.DocumentTypes.addDocumentType
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class ModelExtractorsTest {
    @Rule
    var thrown: ExpectedException = ExpectedException.none()

    @After
    @Throws(Exception::class)
    fun tearDown() {
        ModelExtractors.getInstance().reset()
    }

    @Test
    fun shouldLoadExtractorsOnInstantiation() {
        ModelExtractors.getInstance()
        val expectedKeys: Array<String?> = arrayOf<String>(
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
            assertThat(ModelExtractors.getInstance().containsKey(aKey)).isTrue()
        }
    }

    @Test
    fun shouldRegisterExtractorsOnlyForCustomTypes() {
        val knownDocumentType = "alltag"
        addDocumentType(knownDocumentType)

        ModelExtractors.getInstance().registerExtractorsForCustomTypes(knownDocumentType)

        assertThat(ModelExtractors.getInstance().containsKey("published_alltags")).isFalse()
    }

    @Test
    fun shouldRegisterExtractorsForCustomType() {
        // A document type is known
        val newDocumentType = "project"
        addDocumentType(newDocumentType)

        // when we register extractors for the new type
        ModelExtractors.getInstance().registerExtractorsForCustomTypes(newDocumentType)

        // then an extractor is registered by pluralized type as key
        assertThat(ModelExtractors.getInstance().containsKey("projects")).isTrue()

        // and an extractor for published types is registered
        assertThat(ModelExtractors.getInstance().containsKey("published_projects")).isTrue()
    }

    @Test
    fun shouldThrowAnExceptionIfDocumentTypeIsUnknown() {
        thrown.expect(UnsupportedOperationException::class.java)

        val unknownDocumentType = "unknown"
        ModelExtractors.getInstance().registerExtractorsForCustomTypes(unknownDocumentType)
    }

    @Test
    @Throws(Exception::class)
    fun shouldResetToNonCustomizedExtractors() {
        //given:
        // A document type is known

        val newDocumentType = "project"
        addDocumentType(newDocumentType)

        // when we register extractors for the new type
        ModelExtractors.getInstance().registerExtractorsForCustomTypes(newDocumentType)

        //expect:
        assertThat(ModelExtractors.getInstance().keySet().size()).isEqualTo(18)

        //when:
        ModelExtractors.getInstance().reset()

        //then:
        assertThat(ModelExtractors.getInstance().keySet().size()).isEqualTo(16)
    }
}
