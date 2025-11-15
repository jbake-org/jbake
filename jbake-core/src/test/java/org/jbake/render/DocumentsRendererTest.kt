package org.jbake.render;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes;
import org.jbake.template.RenderingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
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

        assertThat(renderResponse).isEqualTo(0);
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
        when(db.getAllContent(any())).thenReturn(templateModelList);

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
        DocumentModel firstDoc = simpleDocument(firstTitle, ModelAttributes.Status.PUBLISHED, "page");
        String secondTitle = "Second Document";
        DocumentModel secondDoc = simpleDocument(secondTitle, ModelAttributes.Status.PUBLISHED, "post");
        String thirdTitle = "Third Document";
        DocumentModel thirdDoc = simpleDocument(thirdTitle, ModelAttributes.Status.PUBLISHED, "page");
        String fourthTitle = "Fourth Document (draft)";
        DocumentModel fourthDoc = simpleDocument(fourthTitle, ModelAttributes.Status.DRAFT, "post");
        String fifthTitle = "Fifth Document";
        DocumentModel fifthDoc = simpleDocument(fifthTitle, ModelAttributes.Status.PUBLISHED, "page");
        String sixthTitle = "Sixth Document";
        DocumentModel sixthDoc = simpleDocument(sixthTitle, ModelAttributes.Status.PUBLISHED, "post");
        String seventhTitle = "Seventh Document";
        DocumentModel seventhDoc = simpleDocument(seventhTitle, ModelAttributes.Status.PUBLISHED, "post");

        DocumentList allDocs = new DocumentList();
        allDocs.add(seventhDoc);
        allDocs.add(sixthDoc);
        allDocs.add(fifthDoc);
        allDocs.add(fourthDoc);
        allDocs.add(thirdDoc);
        allDocs.add(secondDoc);
        allDocs.add(firstDoc);

        DocumentList pageDocs = new DocumentList();
        pageDocs.add(fifthDoc);
        pageDocs.add(thirdDoc);
        pageDocs.add(firstDoc);

        DocumentList postDocs = new DocumentList();
        postDocs.add(seventhDoc);
        postDocs.add(sixthDoc);
        postDocs.add(fourthDoc);
        postDocs.add(secondDoc);

        when(db.getUnrenderedContent()).thenReturn(allDocs);
        when(db.getAllContent("page")).thenReturn(pageDocs);
        when(db.getAllContent("post")).thenReturn(postDocs);

        // when
        int renderResponse = documentsRenderer.render(renderer, db, configuration);

        // then
        verify(renderer, times(7)).render(argument.capture());
        final Map<String, Map<String, Object>> renderedDocs = asTitleToDocMap(argument.getAllValues());

        // page checks
        assertDocumentNavigation(renderedDocs.get(fifthTitle), thirdTitle, null);
        assertDocumentNavigation(renderedDocs.get(thirdTitle), firstTitle, fifthTitle);
        assertDocumentNavigation(renderedDocs.get(firstTitle), null, thirdTitle);

        // post checks
        assertDocumentNavigation(renderedDocs.get(seventhTitle), sixthTitle, null);
        assertDocumentNavigation(renderedDocs.get(sixthTitle), secondTitle, seventhTitle);
        assertDocumentNavigation(renderedDocs.get(fourthTitle), secondTitle, sixthTitle);
        assertDocumentNavigation(renderedDocs.get(secondTitle), null, sixthTitle);

        assertThat(renderResponse).isEqualTo(7);
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

    private DocumentModel simpleDocument(String title, String status, String docType) {
        DocumentModel simpleDoc = new DocumentModel();
        String uri = title.replace(" ", "_");
        simpleDoc.setNoExtensionUri(uri);
        simpleDoc.setUri(uri);
        simpleDoc.setType(docType);
        simpleDoc.setTitle(title);
        simpleDoc.setStatus(status);
        return simpleDoc;
    }

}
