package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.jbake.template.ModelExtractor;

public class PublishedPagesExtractor implements ModelExtractor<DocumentList<DocumentModel>> {

    @Override
    public DocumentList<DocumentModel> get(ContentStore db, TemplateModel model, String key) {
        return db.getPublishedPages();
    }

}
