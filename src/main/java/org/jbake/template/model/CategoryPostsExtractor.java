package org.jbake.template.model;

import java.util.Map;

import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;


public class CategoryPostsExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ContentStore db, Map model, String key) {
        String category = null;
		if (model.get(Crawler.Attributes.CATEGORY) != null) {
			category = model.get(Crawler.Attributes.CATEGORY).toString();
		}
		DocumentList query = db.getPublishedPostsByCategories(category);
        return query;
	}

}