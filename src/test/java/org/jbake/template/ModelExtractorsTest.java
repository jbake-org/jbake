package org.jbake.template;

import static org.assertj.core.api.Assertions.*;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.model.DocumentTypes;
import org.jbake.render.support.MockCompositeConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;

public class ModelExtractorsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
                "category_posts",
                "category_documents",
                "categories"
        };

        for (String aKey : expectedKeys) {
            assertThat(ModelExtractors.getInstance().containsKey(aKey)).as("Extractor with key %s to exist", aKey).isTrue();
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
    public void shouldContainCategories() throws NoModelExtractorException{
    	
    	Map<String, Object> config = new HashMap<String, Object>();
    	config.put(Keys.CATEGORY_PATH.replace(".","_"), "categories");
    	config.put(Keys.OUTPUT_EXTENSION.replace(".","_"), ".html");
    	
   	 	ContentStore contentStore = mock(ContentStore.class);
        Set<String> cats = new HashSet<>();
        cats.add("Coding");
        
        Mockito.when(contentStore.getCategories()).thenReturn(cats);
        
        Mockito.when(contentStore.getPublishedPostsByCategories(Mockito.anyString())).thenReturn(new DocumentList());
        
        Mockito.when(contentStore.getPublishedDocumentsByCategory(Mockito.anyString())).thenReturn(new DocumentList());
    	
        DocumentList list = (DocumentList) ModelExtractors.getInstance()
    						.extractAndTransform(contentStore, "categories", 
    								Collections.singletonMap("config", config), 
    								new TemplateEngineAdapter.NoopAdapter());
    								
    	assertThat(list)
    		.hasSize(1);
    	
    	for (Map<String, Object> cat : list){
    		assertThat(cat)
    			.containsEntry("uri", "categories/coding.html")
    			.containsKeys("posts", "documents");
    	}
    		
    	
    	
    }
}
