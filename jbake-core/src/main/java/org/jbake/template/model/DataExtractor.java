package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;
import org.jbake.util.DataFileUtil;

import static org.jbake.app.configuration.PropertyList.*;

import java.util.HashMap;
import java.util.Map;

public class DataExtractor implements ModelExtractor<DataFileUtil> {

    @Override
    public DataFileUtil get(ContentStore db, Map model, String key) {
        DocumentList dl = new DocumentList();
        Map<String, Object> config = (Map<String, Object>) model.get("config");

        String defaultDocType = config.get(DATA_FILE_DOCTYPE.getKey()).toString();
        DataFileUtil dataUtil = new DataFileUtil(db, defaultDocType);
        return dataUtil;
    }

}
