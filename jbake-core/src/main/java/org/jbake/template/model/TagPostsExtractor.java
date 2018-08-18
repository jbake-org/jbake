package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentAttributes;
import org.jbake.template.ModelExtractor;

import java.util.Map;

public class TagPostsExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        String tag = null;
        if (model.get(DocumentAttributes.TAG.toString()) != null) {
            tag = model.get(DocumentAttributes.TAG.toString()).toString();
        }
        // fetch the tag posts from db
        return db.getPublishedPostsByTag(tag);
    }

}