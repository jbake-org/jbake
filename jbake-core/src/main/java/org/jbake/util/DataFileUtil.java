package org.jbake.util;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DataFileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFileUtil.class);

    private final ContentStore db;

    public DataFileUtil(ContentStore db) {
        this.db = db;
    }

    public Map<String, Object> get(String ref) {
        Map<String, Object> result = new HashMap<>();
        DocumentList<DocumentModel> docs = db.getDocumentByUri(ref);
        if (docs.isEmpty()) {
            LOGGER.warn("Unable to locate content for ref: {}", ref);
        } else {
            if (docs.size() == 1) {
                result = docs.get(0);
            } else {
                LOGGER.warn("Located multiple hits for ref: {}", ref);
            }
        }
        return result;
    }
}
