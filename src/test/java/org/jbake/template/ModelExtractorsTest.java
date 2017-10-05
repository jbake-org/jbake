package org.jbake.template;

import org.jbake.model.DocumentTypes;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class ModelExtractorsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @After
    public void tearDown() throws Exception {
        ModelExtractors.getInstance().reset();
    }

    @Test
    public void shouldLoadExtractorsOnInstantiation() {

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
        };

        for (String aKey : expectedKeys) {
            assertThat(ModelExtractors.getInstance().containsKey(aKey)).isTrue();
        }
    }

    @Test
    public void shouldRegisterExtractorsOnlyForCustomTypes() {
        String knownDocumentType = "alltag";
        DocumentTypes.addDocumentType(knownDocumentType);

        ModelExtractors.getInstance().registerExtractorsForCustomTypes(knownDocumentType);

        assertThat(ModelExtractors.getInstance().containsKey("published_alltags")).isFalse();
    }

    @Test
    public void shouldRegisterExtractorsForCustomType() {
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
    public void shouldThrowAnExceptionIfDocumentTypeIsUnknown() {
        thrown.expect(UnsupportedOperationException.class);

        String unknownDocumentType = "unknown";
        ModelExtractors.getInstance().registerExtractorsForCustomTypes(unknownDocumentType);
    }

    @Test
    public void shouldResetToNonCustomizedExtractors() throws Exception {

        //given:
        // A document type is known
        String newDocumentType = "project";
        DocumentTypes.addDocumentType(newDocumentType);

        // when we register extractors for the new type
        ModelExtractors.getInstance().registerExtractorsForCustomTypes(newDocumentType);

        //expect:
        assertThat(ModelExtractors.getInstance().keySet().size()).isEqualTo(17);

        //when:
        ModelExtractors.getInstance().reset();

        //then:
        assertThat(ModelExtractors.getInstance().keySet().size()).isEqualTo(15);

    }
}
