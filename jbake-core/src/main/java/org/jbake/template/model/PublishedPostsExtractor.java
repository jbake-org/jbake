package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.jbake.model.TemplateModel;

public class PublishedPostsExtractor implements ModelExtractor<DocumentList<DocumentModel>> {

    @Override
    public DocumentList<DocumentModel> get(ContentStore db, TemplateModel model, String key) {
        if (model.containsKey("numberOfPages")) {
            return db.getPublishedPosts(true);
        } else {
            return db.getPublishedPosts();
        }
    }

}
