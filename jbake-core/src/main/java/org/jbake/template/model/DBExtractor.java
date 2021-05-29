package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.template.ModelExtractor;

public class DBExtractor implements ModelExtractor<ContentStore> {

    @Override
    public ContentStore get(ContentStore db, TemplateModel model, String key) {
        return db;
    }

}
