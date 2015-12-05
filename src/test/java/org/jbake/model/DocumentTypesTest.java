package org.jbake.model;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentTypesTest {
    @Test
    public void defaultDocumentTypesAreAllThere() {
        assertThat(DocumentTypes.getDocumentTypes())
                .containsAll(Arrays.asList("page", "post", "masterindex", "archive", "feed"));
    }

    @Test
    public void addingDocumentToTypesPersist() {
        String newDocumentType = "new document Type";
        DocumentTypes.addDocumentType(newDocumentType);

        assertThat(DocumentTypes.getDocumentTypes()).contains(newDocumentType);
    }
}
