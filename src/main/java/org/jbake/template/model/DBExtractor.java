package org.jbake.template.model;

import java.util.Map;

import org.jbake.app.ContentStore;
import org.jbake.template.ModelExtractor;

public class DBExtractor implements ModelExtractor<ContentStore> {

	@Override
	public ContentStore get(ContentStore db, Map model, String key) {
		return db;
	}

}