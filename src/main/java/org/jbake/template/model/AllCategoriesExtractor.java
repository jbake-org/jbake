package org.jbake.template.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.template.ModelExtractor;


/**
 * 
 * This extractor model will list of categories from all published content.
 * 
 * @author Manik Magar <manik.magar@gmail.com>
 *
 */
public class AllCategoriesExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ContentStore db, Map model, String key) {
		DocumentList dl = new DocumentList();
		Map<String, Object> config = (Map<String, Object>) model.get("config");
		
		String categoryPath = config.get(Keys.CATEGORY_PATH.replace(".", "_")).toString();
		
		for (String category : db.getCategories()){
			Map<String, Object> newCategory = new HashMap<String, Object>();
			newCategory.put("name",category);
			
			String uri = categoryPath + File.separator + Renderer.toSanitizedUriName(category) + config.get(Keys.OUTPUT_EXTENSION.replace(".", "_")).toString();
			
			newCategory.put("uri", uri);
			newCategory.put("posts", db.getPublishedPostsByCategories(category));
			newCategory.put("documents", db.getPublishedDocumentsByCategory(category));
			dl.push(newCategory);
		}
		return dl;
	}

}