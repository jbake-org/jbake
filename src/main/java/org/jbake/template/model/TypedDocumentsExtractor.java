package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypeUtils;
import org.jbake.template.ModelExtractor;

import java.util.Map;

public class TypedDocumentsExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        // document types are pluralized in model, so unpluralize
        try {
            String type = DocumentTypeUtils.unpluralize(key);
            return db.getAllContent(type);
        } catch (UnsupportedOperationException e) {

            return new DocumentList();
        }
    }

}