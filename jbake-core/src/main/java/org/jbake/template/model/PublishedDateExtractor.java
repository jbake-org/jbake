package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.template.ModelExtractor;

import java.time.Instant;
import java.util.Map;

public class PublishedDateExtractor implements ModelExtractor<Instant> {

    @Override
    public Instant get(ContentStore db, Map model, String key) {
        return Instant.now();
    }

}
