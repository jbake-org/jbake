package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.ModelAttributes;
import org.jbake.template.ModelExtractor;

import java.util.Map;

public class TaggedDocumentsExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        String tag = null;
        if (model.get(ModelAttributes.TAG.toString()) != null) {
            tag = model.get(ModelAttributes.TAG.toString()).toString();
        }
        // fetch the tagged documents from db
        return db.getPublishedDocumentsByTag(tag);
    }

}
