package org.jbake.render;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.exception.RenderingException;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
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
    private DocumentList<DocumentModel> emptyTemplateModelList;

    @Captor
    private ArgumentCaptor<DocumentModel> argument;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        documentsRenderer = new DocumentsRenderer();

        db = mock(ContentStore.class);
        renderer = mock(Renderer.class);
        configuration = mock(JBakeConfiguration.class);
        emptyTemplateModelList = new DocumentList<>();
    }

    @Test
    public void shouldReturnZeroIfNothingHasRendered() throws Exception {

        when(db.getUnrenderedContent()).thenReturn(emptyTemplateModelList);

        int renderResponse = documentsRenderer.render(renderer, db, configuration);

        assertThat(renderResponse).isZero();
    }

    @Test
    public void shouldReturnCountOfProcessedDocuments() throws Exception {

        // given:
        DocumentTypes.addDocumentType("customType");

        DocumentList<DocumentModel> templateModelList = new DocumentList<>();
        templateModelList.add(emptyDocument());
        templateModelList.add(emptyDocument());

        // return given DocumentList for DocumentType 'custom type'
        when(db.getUnrenderedContent()).thenReturn(templateModelList);

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

                DocumentList<DocumentModel> templateModelList = new DocumentList<>();
                DocumentModel document = emptyDocument();
                DocumentModel document2 = emptyDocument();
                templateModelList.add(document);
                templateModelList.add(document2);

                // throw an exception for every call of renderer's render method
                doThrow(new Exception(fakeExceptionMessage)).when(renderer).render(any(DocumentModel.class));
                when(db.getUnrenderedContent()).thenReturn(templateModelList);

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
        documents.add(simpleDocument(fourthTitle, ModelAttributes.Status.PUBLISHED));
        documents.add(simpleDocument(thirdTitle, ModelAttributes.Status.PUBLISHED));
        documents.add(simpleDocument(secondTitleIsDraft, ModelAttributes.Status.DRAFT));
        documents.add(simpleDocument(firstTitle, ModelAttributes.Status.PUBLISHED));

        when(db.getUnrenderedContent()).thenReturn(documents);

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
            "previousContent." + ModelAttributes.TITLE,
            "nextContent." + ModelAttributes.TITLE)
            .containsExactly(prevDocumentTitle, nextDocumentTitle);
    }

    private Map<String, Map<String, Object>> asTitleToDocMap(List<DocumentModel> values) {
        return values.stream()
            .collect(Collectors.toMap(doc -> doc.get(ModelAttributes.TITLE).toString(), doc -> doc));
    }

    private DocumentModel emptyDocument() {
        return new DocumentModel();
    }

    private DocumentModel simpleDocument(String title, String status) {
        DocumentModel simpleDoc = new DocumentModel();
        String uri = title.replace(" ", "_");
        simpleDoc.setNoExtensionUri(uri);
        simpleDoc.setUri(uri);
        simpleDoc.setTitle(title);
        simpleDoc.setStatus(status);
        return simpleDoc;
    }

}
