package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.model.TemplateModel;

import java.util.Set;

public class AllTagsExtractor implements ModelExtractor<Set<String>> {

    @Override
    public Set<String> get(ContentStore db, TemplateModel model, String key) {
        return db.getAllTags();
    }

}
