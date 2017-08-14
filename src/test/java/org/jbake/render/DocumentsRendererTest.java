package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.app.Crawler.Attributes;
import org.jbake.model.DocumentTypes;
import org.jbake.template.RenderingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    @Captor
    private ArgumentCaptor<Map<String, Object>> argument;

    @Before
    public void setUp() throws Exception {
    	
    	MockitoAnnotations.initMocks(this);
    	
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
    
    @Test
    public void shouldContainPostNavigation()  throws Exception{
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
    	
    	int renderResponse = documentsRenderer.render(renderer,db,destinationFile,templatePath,configuration);
    	
    	Map<String, Object> fourthDoc = simpleDocument(fourth);
    	fourthDoc.put("previousContent", simpleDocument(third));
    	fourthDoc.put("nextContent", null);
   
    	Map<String, Object> thirdDoc = simpleDocument(third);
    	thirdDoc.put("nextContent", simpleDocument(fourth));
    	thirdDoc.put("previousContent", simpleDocument(second));
    	
    	Map<String, Object> secondDoc = simpleDocument(second);
    	secondDoc.put("nextContent", simpleDocument(third));
    	secondDoc.put("previousContent", simpleDocument(first));
    	
    	Map<String, Object> firstDoc = simpleDocument(first);
    	firstDoc.put("nextContent", simpleDocument(second));
    	firstDoc.put("previousContent", null);
    	
    	verify(renderer,times(4)).render(argument.capture());
    	
    	List<Map<String, Object>> maps = argument.getAllValues();
    	
    	assertThat(maps).contains(fourthDoc);
    	
    	assertThat(maps).contains(thirdDoc);
    	
    	assertThat(maps).contains(secondDoc);
    	
    	assertThat(maps).contains(firstDoc);
    	
    	assertThat(renderResponse).isEqualTo(4);
    }

    private HashMap<String, Object> emptyDocument() {
        return new HashMap<String,Object>();
    }
    
    private Map<String, Object> simpleDocument(String title) {
    	Map<String, Object> simpleDoc = new HashMap<String, Object>();
    	String uri = title.replace(" ", "_");
    	simpleDoc.put(Attributes.NO_EXTENSION_URI, uri);
    	simpleDoc.put(Attributes.URI, uri);
    	simpleDoc.put(Attributes.TITLE, title);;
		return simpleDoc;
    }
    
}