package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.jbake.model.TemplateModel;

public class TagPostsExtractor implements ModelExtractor<DocumentList<DocumentModel>> {

    @Override
    public DocumentList<DocumentModel> get(ContentStore db, TemplateModel model, String key) {
        String tag = null;
        if (model.getTag() != null) {
            tag = model.getTag();
        }
        // fetch the tag posts from db
        return db.getPublishedPostsByTag(tag);
    }

}
