package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.configuration.PropertyList;
import org.jbake.template.ModelExtractor;
import org.jbake.util.DataFileUtil;

import java.util.Map;

public class DataExtractor implements ModelExtractor<DataFileUtil> {

    @Override
    public DataFileUtil get(ContentStore db, Map model, String key) {
        DocumentList dl = new DocumentList();
        Map<String, Object> config = (Map<String, Object>) model.get("config");

        String defaultDocType = config.get(PropertyList.DATA_FILE_DOCTYPE).toString();
        DataFileUtil dataUtil = new DataFileUtil(db, defaultDocType);
        return dataUtil;
    }

}
