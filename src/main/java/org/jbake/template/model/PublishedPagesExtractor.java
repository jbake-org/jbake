package org.jbake.template.model;

import java.util.List;
import java.util.Map;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class PublishedPagesExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ContentStore db, Map model, String key) {
        List<ODocument> query = db.getPublishedPages();
        return DocumentList.wrap(query.iterator());
	}

}