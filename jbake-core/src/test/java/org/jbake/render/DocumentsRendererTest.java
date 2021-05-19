package org.jbake.render;

import org.jbake.app.ContentStore;
import org.jbake.app.Crawler.Attributes;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentTypes;
import org.jbake.template.RenderingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentsRendererTest {

    public DocumentsRenderer documentsRenderer;
    private ContentStore db;
    private Renderer renderer;
    private JBakeConfiguration configuration;
    private DocumentList emptyDocumentList;

    @Captor
    private ArgumentCaptor<Map<String, Object>> argument;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        documentsRenderer = new DocumentsRenderer();

        db = mock(ContentStore.class);
        renderer = mock(Renderer.class);
        configuration = mock(JBakeConfiguration.class);
        emptyDocumentList = new DocumentList();
    }

    @Test
    public void shouldReturnZeroIfNothingHasRendered() throws Exception {

        when(db.getUnrenderedContent(anyString())).thenReturn(emptyDocumentList);

        int renderResponse = documentsRenderer.render(renderer, db, configuration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    public void shouldReturnCountOfProcessedDocuments() throws Exception {

        // given:
        DocumentTypes.addDocumentType("customType");

        DocumentList documentList = new DocumentList();
        documentList.add(emptyDocument());
        documentList.add(emptyDocument());

        // return empty DocumentList independent from DocumentType
        when(db.getUnrenderedContent(anyString())).thenReturn(emptyDocumentList);
        // return given DocumentList for DocumentType 'custom type'
        when(db.getUnrenderedContent("customType")).thenReturn(documentList);

        // when:
        int renderResponse = documentsRenderer.render(renderer, db, configuration);

        // then:
        assertThat(renderResponse).isEqualTo(2);
    }

    @Test
    public void shouldThrowAnExceptionWithCollectedErrorMessages() {
        String fakeExceptionMessage = "fake exception";

        // expect
        Assertions.assertThrows(
            RenderingException.class, () -> {

                // given
                DocumentTypes.addDocumentType("customType");

                DocumentList documentList = new DocumentList();
                HashMap<String, Object> document = emptyDocument();
                HashMap<String, Object> document2 = emptyDocument();
                documentList.add(document);
                documentList.add(document2);

                // throw an exception for every call of renderer's render method
                doThrow(new Exception(fakeExceptionMessage)).when(renderer).render(ArgumentMatchers.anyMap());
                when(db.getUnrenderedContent(anyString())).thenReturn(emptyDocumentList);
                when(db.getUnrenderedContent("customType")).thenReturn(documentList);

                // when
                int renderResponse = documentsRenderer.render(renderer, db, configuration);

                // then
                assertThat(renderResponse).isEqualTo(2);
            },
            fakeExceptionMessage + "\n" + fakeExceptionMessage
        );
    }

    @Test
    public void shouldContainPostNavigation() throws Exception {
        // given
        DocumentTypes.addDocumentType("customType");

        String firstTitle = "First Document";
        String secondTitleIsDraft = "Second Document (draft)";
        String thirdTitle = "Third Document";
        String fourthTitle = "Fourth Document";

        DocumentList documents = new DocumentList();
        // Attributes.Status.PUBLISHED_DATE cannot occur here
        // because it's converted TO either PUBLISHED or DRAFT in the Crawler.
        documents.add(simpleDocument(fourthTitle, Attributes.Status.PUBLISHED));
        documents.add(simpleDocument(thirdTitle, Attributes.Status.PUBLISHED));
        documents.add(simpleDocument(secondTitleIsDraft, Attributes.Status.DRAFT));
        documents.add(simpleDocument(firstTitle, Attributes.Status.PUBLISHED));

        when(db.getUnrenderedContent("customType")).thenReturn(documents);

        // when
        int renderResponse = documentsRenderer.render(renderer, db, configuration);

        // then
        verify(renderer, times(4)).render(argument.capture());

        final Map<String, Map<String, Object>> renderedDocs = asTitleToDocMap(argument.getAllValues());
        assertDocumentNavigation(renderedDocs.get(firstTitle), null, thirdTitle);
        assertDocumentNavigation(renderedDocs.get(secondTitleIsDraft), firstTitle, thirdTitle);
        assertDocumentNavigation(renderedDocs.get(thirdTitle), firstTitle, fourthTitle);
        assertDocumentNavigation(renderedDocs.get(fourthTitle), thirdTitle, null);
        assertThat(renderResponse).isEqualTo(4);
    }

    private void assertDocumentNavigation(
        final Map<String, Object> renderedDoc,
        final String prevDocumentTitle, String nextDocumentTitle) {
        assertThat(renderedDoc).flatExtracting(
            "previousContent." + Attributes.TITLE,
            "nextContent." + Attributes.TITLE)
            .containsExactly(prevDocumentTitle, nextDocumentTitle);
    }

    private Map<String, Map<String, Object>> asTitleToDocMap(List<Map<String, Object>> values) {
        return values.stream()
            .collect(Collectors.toMap(doc -> doc.get(Attributes.TITLE).toString(), doc -> doc));
    }

    private HashMap<String, Object> emptyDocument() {
        return new HashMap<>();
    }

    private Map<String, Object> simpleDocument(String title, String status) {
        Map<String, Object> simpleDoc = new HashMap<>();
        String uri = title.replace(" ", "_");
        simpleDoc.put(Attributes.NO_EXTENSION_URI, uri);
        simpleDoc.put(Attributes.URI, uri);
        simpleDoc.put(Attributes.TITLE, title);
        simpleDoc.put(Attributes.STATUS, status);
        return simpleDoc;
    }

}
