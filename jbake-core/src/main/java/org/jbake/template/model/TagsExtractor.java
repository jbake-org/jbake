package org.jbake.template.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

import static org.jbake.app.configuration.JBakeConfiguration.OUTPUT_EXTENSION;
import static org.jbake.app.configuration.JBakeConfiguration.TAG_PATH;


public class TagsExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ContentStore db, Map model, String key) {
		DocumentList dl = new DocumentList();
		Map<?, ?> config = (Map<?, ?>) model.get("config");
		
		String tagPath = config.get(TAG_PATH.replace(".", "_")).toString();
		
		for (String tag : db.getAllTags()){
			Map<String, Object> newTag = new HashMap<>();
			newTag.put("name", tag);
			
			String uri = tagPath + File.separator + tag + config.get(OUTPUT_EXTENSION.replace(".", "_")).toString();
			
			newTag.put("uri", uri);
			newTag.put("tagged_posts", db.getPublishedPostsByTag(tag));
			newTag.put("tagged_documents", db.getPublishedDocumentsByTag(tag));
			dl.push(newTag);
		}
		return dl;
	}

}
