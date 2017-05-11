package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.model.DocumentTypes;
import org.jbake.template.RenderingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DocumentsRendererTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private DocumentsRenderer documentsRenderer;
    private ContentStore db;
    private Renderer renderer;
    private CompositeConfiguration configuration;
    private DocumentList emptyDocumentList;
    private File destinationFile;
    private File templatePath;

    @Before
    public void setUp() throws Exception {
        documentsRenderer = new DocumentsRenderer();

        db = mock(ContentStore.class);
        renderer = mock(Renderer.class);
        configuration = mock(CompositeConfiguration.class);
        emptyDocumentList = new DocumentList();

        destinationFile = new File("fakefile");
        templatePath = new File("fakepath");
    }

    @Test
    public void shouldReturnZeroIfNothingHasRendered() throws Exception {

        when(db.getUnrenderedContent(anyString())).thenReturn(emptyDocumentList);
        
        int renderResponse = documentsRenderer.render(renderer,db,destinationFile,templatePath,configuration);

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
        int renderResponse = documentsRenderer.render(renderer,db,destinationFile,templatePath,configuration);

        // then:
        assertThat(renderResponse).isEqualTo(2);
    }

    @Test
    public void shouldThrowAnExceptionWithCollectedErrorMessages() throws Exception {
        String fakeExceptionMessage = "fake exception";
        // expect
        exception.expect(RenderingException.class);
        exception.expectMessage(fakeExceptionMessage+"\n"+fakeExceptionMessage);

        // given
        DocumentTypes.addDocumentType("customType");

        DocumentList documentList = new DocumentList();
        HashMap<String, Object> document = emptyDocument();
        HashMap<String, Object> document2 = emptyDocument();
        documentList.add(document);
        documentList.add(document2);

        // throw an exception for every call of renderer's render method
        doThrow(new Exception(fakeExceptionMessage)).when(renderer).render(anyMap());
        when(db.getUnrenderedContent(anyString())).thenReturn(emptyDocumentList);
        when(db.getUnrenderedContent("customType")).thenReturn(documentList);

        // when
        int renderResponse = documentsRenderer.render(renderer,db,destinationFile,templatePath,configuration);

        // then
        assertThat(renderResponse).isEqualTo(2);
    }

    private HashMap<String, Object> emptyDocument() {
        return new HashMap<String,Object>();
    }
}