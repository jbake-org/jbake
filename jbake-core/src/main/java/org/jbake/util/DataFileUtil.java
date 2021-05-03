package org.jbake.util;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;

import java.util.Map;

public class DataFileUtil {
    private ContentStore db;
    private String defaultDocType;

    public DataFileUtil(ContentStore db, String defaultDocType) {
        this.db = db;
        this.defaultDocType = defaultDocType;
    }

    public Map<String, Object> get(String ref) {
        DocumentList docs = db.getDocumentByUri(defaultDocType, ref);
        if (docs.size() == 1) {
            return docs.get(0);
        }
        return null;
    }
}
