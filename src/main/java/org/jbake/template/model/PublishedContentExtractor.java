package org.jbake.template.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;
import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class PublishedContentExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ContentStore db, Map model, String key) {
    	List<ODocument> publishedContent = new ArrayList<ODocument>();
    	String[] documentTypes = DocumentTypes.getDocumentTypes();
    	for (String docType : documentTypes) {
    		List<ODocument> query = db.getPublishedContent(docType);
    		publishedContent.addAll(query);
    	}
    	return DocumentList.wrap(publishedContent.iterator());
	}

}