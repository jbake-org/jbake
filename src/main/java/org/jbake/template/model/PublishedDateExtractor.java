package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.template.ModelExtractor;

import java.util.Date;
import java.util.Map;

public class PublishedDateExtractor implements ModelExtractor<Date> {

    @Override
    public Date get(ContentStore db, Map model, String key) {
        return new Date();
    }

}