package org.jbake.template;

import org.jbake.model.DocumentTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelExtractorsTest {


    @AfterEach
    void tearDown() throws Exception {
        ModelExtractors.getInstance().reset();
    }

    @Test
    void shouldLoadExtractorsOnInstantiation() {

        ModelExtractors.getInstance();
        String[] expectedKeys = new String[]{
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
        };

        for (String aKey : expectedKeys) {
            assertThat(ModelExtractors.getInstance().containsKey(aKey)).isTrue();
        }
    }

    @Test
    void shouldRegisterExtractorsOnlyForCustomTypes() {
        String knownDocumentType = "alltag";
        DocumentTypes.addDocumentType(knownDocumentType);

        ModelExtractors.getInstance().registerExtractorsForCustomTypes(knownDocumentType);

        assertThat(ModelExtractors.getInstance().containsKey("published_alltags")).isFalse();
    }

    @Test
    void shouldRegisterExtractorsForCustomType() {
        // A document type is known
        String newDocumentType = "project";
        DocumentTypes.addDocumentType(newDocumentType);

        // when we register extractors for the new type
        ModelExtractors.getInstance().registerExtractorsForCustomTypes(newDocumentType);

        // then an extractor is registered by pluralized type as key
        assertThat(ModelExtractors.getInstance().containsKey("projects")).isTrue();

        // and an extractor for published types is registered
        assertThat(ModelExtractors.getInstance().containsKey("published_projects")).isTrue();
    }

    @Test
    void shouldThrowAnExceptionIfDocumentTypeIsUnknown() {
        String unknownDocumentType = "unknown";
        assertThatThrownBy(() -> ModelExtractors.getInstance().registerExtractorsForCustomTypes(unknownDocumentType))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldResetToNonCustomizedExtractors() throws Exception {

        //given:
        // A document type is known
        String newDocumentType = "project";
        DocumentTypes.addDocumentType(newDocumentType);

        // when we register extractors for the new type
        ModelExtractors.getInstance().registerExtractorsForCustomTypes(newDocumentType);

        //expect:
        assertThat(ModelExtractors.getInstance().keySet().size()).isEqualTo(18);

        //when:
        ModelExtractors.getInstance().reset();

        //then:
        assertThat(ModelExtractors.getInstance().keySet().size()).isEqualTo(16);
    }
}
