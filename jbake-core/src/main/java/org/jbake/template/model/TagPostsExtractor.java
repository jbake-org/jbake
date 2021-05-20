package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

import java.util.Map;

public class TagPostsExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        String tag = null;
        TemplateModel templateModel = new TemplateModel();
        templateModel.putAll(model);
        if (templateModel.getTag() != null) {
            tag = templateModel.getTag();
        }
        // fetch the tag posts from db
        return db.getPublishedPostsByTag(tag);
    }

}
