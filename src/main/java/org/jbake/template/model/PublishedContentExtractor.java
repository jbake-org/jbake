package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;
import org.jbake.template.ModelExtractor;

import java.util.Map;

public class PublishedContentExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        DocumentList publishedContent = new DocumentList();
        String[] documentTypes = DocumentTypes.getDocumentTypes();
        for (String docType : documentTypes) {
            DocumentList query = db.getPublishedContent(docType);
            publishedContent.addAll(query);
        }
        return publishedContent;
    }

}