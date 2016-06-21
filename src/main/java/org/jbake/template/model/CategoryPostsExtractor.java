package org.jbake.template.model;

import java.util.List;
import java.util.Map;

import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class CategoryPostsExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ContentStore db, Map model, String key) {
        String category = null;
		if (model.get(Crawler.Attributes.CATEGORY) != null) {
			category = model.get(Crawler.Attributes.CATEGORY).toString();
		}
        List<ODocument> query = db.getPublishedPostsByCategories(category);
        return DocumentList.wrap(query.iterator());
	}

}