package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.template.ModelExtractor;
import org.jbake.util.DataFileUtil;
import java.util.Map;
import static org.jbake.app.configuration.PropertyList.*;

public class DataExtractor implements ModelExtractor<DataFileUtil> {

    @Override
    public DataFileUtil get(ContentStore db, Map model, String key) {
        Map<String, Object> config = (Map<String, Object>) model.get("config");
        String defaultDocType = config.get(DATA_FILE_DOCTYPE.getKey().replace(".", "_")).toString();
        DataFileUtil dataUtil = new DataFileUtil(db, defaultDocType);
        return dataUtil;
    }

}
