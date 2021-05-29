package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypeUtils;
import org.jbake.template.ModelExtractor;

public class TypedDocumentsExtractor implements ModelExtractor<DocumentList<DocumentModel>> {

    @Override
    public DocumentList<DocumentModel> get(ContentStore db, TemplateModel model, String key) {
        // document types are pluralized in model, so unpluralize
        try {
            String type = DocumentTypeUtils.unpluralize(key);
            return db.getAllContent(type);
        } catch (UnsupportedOperationException e) {
            return new DocumentList<>();
        }
    }

}
