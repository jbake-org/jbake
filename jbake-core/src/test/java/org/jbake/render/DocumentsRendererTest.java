package org.jbake.render;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.template.RenderingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DocumentsRendererTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private DocumentsRenderer documentsRenderer;
    private ContentStore db;
    private Renderer renderer;
    private JBakeConfiguration configuration;
    private DocumentList emptyTemplateModelList;

    @Captor
    private ArgumentCaptor<DocumentModel> argument;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        documentsRenderer = new DocumentsRenderer();

        db = mock(ContentStore.class);
        renderer = mock(Renderer.class);
        configuration = mock(JBakeConfiguration.class);
        emptyTemplateModelList = new DocumentList();
    }

    @Test
    public void shouldReturnZeroIfNothingHasRendered() throws Exception {

        when(db.getUnrenderedContent(anyString())).thenReturn(emptyTemplateModelList);

        int renderResponse = documentsRenderer.render(renderer, db, configuration);

        assertThat(renderResponse).isEqualTo(0);
    }

    @Test
    public void shouldReturnCountOfProcessedDocuments() throws Exception {

        // given:
        DocumentTypes.addDocumentType("customType");

        DocumentList templateModelList = new DocumentList();
        templateModelList.add(emptyDocument());
        templateModelList.add(emptyDocument());

        // return empty DocumentList independent from DocumentType
        when(db.getUnrenderedContent(anyString())).thenReturn(emptyTemplateModelList);
        // return given DocumentList for DocumentType 'custom type'
        when(db.getUnrenderedContent("customType")).thenReturn(templateModelList);

        // when:
        int renderResponse = documentsRenderer.render(renderer, db, configuration);

        // then:
        assertThat(renderResponse).isEqualTo(2);
    }

    @Test
    public void shouldThrowAnExceptionWithCollectedErrorMessages() throws Exception {
        String fakeExceptionMessage = "fake exception";
        // expect
        exception.expect(RenderingException.class);
        exception.expectMessage(fakeExceptionMessage + "\n" + fakeExceptionMessage);

        // given
        DocumentTypes.addDocumentType("customType");

        DocumentList templateModelList = new DocumentList();
        DocumentModel document = emptyDocument();
        DocumentModel document2 = emptyDocument();
        templateModelList.add(document);
        templateModelList.add(document2);

        // throw an exception for every call of renderer's render method
        doThrow(new Exception(fakeExceptionMessage)).when(renderer).render(any(DocumentModel.class));
        when(db.getUnrenderedContent(anyString())).thenReturn(emptyTemplateModelList);
        when(db.getUnrenderedContent("customType")).thenReturn(templateModelList);

        // when
        int renderResponse = documentsRenderer.render(renderer, db, configuration);

        // then
        assertThat(renderResponse).isEqualTo(2);
    }

    @Test
    public void shouldContainPostNavigation() throws Exception {
        DocumentTypes.addDocumentType("customType");

        String first = "First Document";
        String second = "Second Document";
        String third = "Third Document";
        String fourth = "Fourth Document";

        DocumentList documents = new DocumentList();
        documents.add(simpleDocument(fourth));
        documents.add(simpleDocument(third));
        documents.add(simpleDocument(second));
        documents.add(simpleDocument(first));

        when(db.getUnrenderedContent("customType")).thenReturn(documents);

        int renderResponse = documentsRenderer.render(renderer, db, configuration);

        DocumentModel fourthDoc = simpleDocument(fourth);
        fourthDoc.put("previousContent", simpleDocument(third));
        fourthDoc.put("nextContent", null);

        DocumentModel thirdDoc = simpleDocument(third);
        thirdDoc.put("nextContent", simpleDocument(fourth));
        thirdDoc.put("previousContent", simpleDocument(second));

        DocumentModel secondDoc = simpleDocument(second);
        secondDoc.put("nextContent", simpleDocument(third));
        secondDoc.put("previousContent", simpleDocument(first));

        DocumentModel firstDoc = simpleDocument(first);
        firstDoc.put("nextContent", simpleDocument(second));
        firstDoc.put("previousContent", null);

        verify(renderer, times(4)).render(argument.capture());

        List<DocumentModel> maps = argument.getAllValues();

        assertThat(maps).contains(fourthDoc);

        assertThat(maps).contains(thirdDoc);

        assertThat(maps).contains(secondDoc);

        assertThat(maps).contains(firstDoc);

        assertThat(renderResponse).isEqualTo(4);
    }

    private DocumentModel emptyDocument() {
        return new DocumentModel();
    }

    private DocumentModel simpleDocument(String title) {
        DocumentModel simpleDoc = new DocumentModel();
        String uri = title.replace(" ", "_");
        simpleDoc.setNoExtensionUri(uri);
        simpleDoc.setUri(uri);
        simpleDoc.setTitle(title);
        return simpleDoc;
    }

}