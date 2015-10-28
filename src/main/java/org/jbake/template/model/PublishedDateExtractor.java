package org.jbake.template.model;

import java.util.Date;
import java.util.Map;

import org.jbake.app.ContentStore;
import org.jbake.template.ModelExtractor;

public class PublishedDateExtractor implements ModelExtractor<Date> {

	@Override
	public Date get(ContentStore db, Map model, String key) {
		return new Date();
	}

}