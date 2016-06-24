package org.jbake.template.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;


/**
 * 
 * This extractor model will list of categories from all published content.
 * 
 * @author Manik MAgar <manik.magar@gmail.com>
 *
 */
public class AllCategoriesExtractor implements ModelExtractor<Set<String>> {

	@Override
	public Set<String> get(ContentStore db, Map model, String key) {
        DocumentList query = db.getAllCategoriesFromPublishedPosts();
        Set<String> result = new HashSet<String>();
        for (Map<String, Object> document : query) {
            String[] categories = DBUtil.toStringArray(document.get(Crawler.Attributes.CATEGORIES));
            Collections.addAll(result, categories);
        }
        return result;
	}

}