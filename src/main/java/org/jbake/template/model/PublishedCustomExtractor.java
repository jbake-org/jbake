package org.jbake.template.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

import java.util.List;
import java.util.Map;

public class PublishedCustomExtractor implements ModelExtractor<DocumentList> {

	String customDocumentType;

    public PublishedCustomExtractor(String customDocumentType) {
        this.customDocumentType = customDocumentType;
    }

    @Override
	public DocumentList get(ContentStore db, Map model, String key) {
        List<ODocument> query = db.getPublishedContent(customDocumentType);
        return DocumentList.wrap(query.iterator());
	}

}