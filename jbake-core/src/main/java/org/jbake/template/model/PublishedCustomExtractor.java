package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.jbake.model.TemplateModel;

public class PublishedCustomExtractor implements ModelExtractor<DocumentList<DocumentModel>> {

    String customDocumentType;

    public PublishedCustomExtractor(String customDocumentType) {
        this.customDocumentType = customDocumentType;
    }

    @Override
    public DocumentList<DocumentModel> get(ContentStore db, TemplateModel model, String key) {
        return db.getPublishedContent(customDocumentType);
    }

}
