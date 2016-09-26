package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

import java.util.Map;

public class TagPostsExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        String tag = null;
        if (model.get(Crawler.Attributes.TAG) != null) {
            tag = model.get(Crawler.Attributes.TAG).toString();
        }
        // fetch the tag posts from db
        return db.getPublishedPostsByTag(tag);
    }

}