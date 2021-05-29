package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.template.ModelExtractor;
import org.jbake.util.DataFileUtil;

public class DataExtractor implements ModelExtractor<DataFileUtil> {

    @Override
    public DataFileUtil get(ContentStore db, TemplateModel model, String key) {
        return new DataFileUtil(db);
    }

}
