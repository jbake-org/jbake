package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.template.ModelExtractor;

public class PublishedContentExtractor implements ModelExtractor<DocumentList<DocumentModel>> {

    @Override
    public DocumentList<DocumentModel> get(ContentStore db, TemplateModel model, String key) {
        DocumentList<DocumentModel> publishedContent = new DocumentList<>();
        String[] documentTypes = DocumentTypes.getDocumentTypes();
        for (String docType : documentTypes) {
            DocumentList<DocumentModel> query = db.getPublishedContent(docType);
            publishedContent.addAll(query);
        }
        return publishedContent;
    }

}
