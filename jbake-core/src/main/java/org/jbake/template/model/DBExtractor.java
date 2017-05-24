package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.template.ModelExtractor;

import java.util.Map;

public class DBExtractor implements ModelExtractor<ContentStore> {

    @Override
    public ContentStore get(ContentStore db, Map model, String key) {
        return db;
    }

}