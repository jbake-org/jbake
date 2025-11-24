package org.jbake.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DocumentTypesTest {

    @Test
    void shouldReturnDefaultDocumentTypes() throws Exception {
        String[] knownDocumentTypes = DocumentTypes.getDocumentTypes();
        String[] expectedDocumentType = new String[] {"page", "post", "masterindex", "archive", "feed" };

        assertThat(knownDocumentTypes).contains(expectedDocumentType);
    }

    @Test
    void shouldAddNewDocumentType() {
        String newDocumentType = "newDocumentType";

        DocumentTypes.addDocumentType(newDocumentType);

        assertThat(DocumentTypes.getDocumentTypes()).contains(newDocumentType);
    }

    @Test
    void shouldAddDocumentTypeOnlyOnce() {
        // A a document type is already known
        String knownDocumentType = "known";
        DocumentTypes.addDocumentType(knownDocumentType);

        // adding the known document type again
        DocumentTypes.addDocumentType(knownDocumentType);

        // only one document type could be found in the list
        assertThat(DocumentTypes.getDocumentTypes()).containsOnlyOnce(knownDocumentType);
    }

    @Test
    void shouldTellIfDocumentTypeIsKnown() {
        String knownDocumentType = "known";
        DocumentTypes.addDocumentType(knownDocumentType);

        assertThat( DocumentTypes.contains(knownDocumentType) ).isTrue();
    }

    @Test
    void shouldTellIfDocumentTypeIsUnknown() {
        String unknownType = "unknown";

        assertThat( DocumentTypes.contains(unknownType) ).isFalse();
    }

    @Test
    void shouldNotifyListenersWhenNewDocumentTypeIsAdded() {
        // A DocumentTypeListener is added
        String newDocumentType = "newDocumentType";
        DocumentTypeListener listener = mock(DocumentTypeListener.class);
        DocumentTypes.addListener(listener);

        // a new document type added
        DocumentTypes.addDocumentType(newDocumentType);

        // the listener was called with new document type
        verify(listener).added(newDocumentType);
    }
}
