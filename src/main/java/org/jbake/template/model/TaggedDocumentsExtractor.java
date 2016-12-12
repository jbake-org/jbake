package org.jbake.template.model;

import java.util.Map;

import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

public class TaggedDocumentsExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        String tag = null;
        if (model.get(Crawler.Attributes.TAG) != null) {
            tag = model.get(Crawler.Attributes.TAG).toString();
        }
        // fetch the tagged documents from db
        return db.getPublishedDocumentsByTag(tag);
    }

}
