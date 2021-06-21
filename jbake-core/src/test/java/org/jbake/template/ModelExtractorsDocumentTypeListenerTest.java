package org.jbake.template;

import org.jbake.engine.ModelExtractors;
import org.jbake.model.DocumentTypes;
import org.jbake.template.model.ModelExtractorsDocumentTypeListener;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelExtractorsDocumentTypeListenerTest {

    @Test
    public void shouldRegisterExtractorsForCustomType() {
        // given: "A document type is known"
        String newDocumentType = "project";
        DocumentTypes.addDocumentType(newDocumentType);
        ModelExtractorsDocumentTypeListener listener = new ModelExtractorsDocumentTypeListener();

        // when: "the listener is called with that type"
        listener.added(newDocumentType);

        // then: "an extractor is registered by pluralized type as key"
        assertThat(ModelExtractors.getInstance().supportsExtension("projects")).isTrue();

        // and: "an extractor for published types is registered"
        assertThat(ModelExtractors.getInstance().supportsExtension("published_projects")).isTrue();
    }
}
