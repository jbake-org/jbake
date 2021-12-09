package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.model.TemplateModel;

import java.util.Date;

public class PublishedDateExtractor implements ModelExtractor<Date> {

    @Override
    public Date get(ContentStore db, TemplateModel model, String key) {
        return new Date();
    }

}
