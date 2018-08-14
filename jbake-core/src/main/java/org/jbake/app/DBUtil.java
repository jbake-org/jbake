package org.jbake.app;

import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jbake.app.configuration.JBakeConfiguration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DBUtil {
    private static ContentStore contentStore;

    @Deprecated
    public static ContentStore createDataStore(final String type, String name) {
        if (contentStore == null) {
            contentStore = new ContentStore(type, name);
        }
        return contentStore;
    }

    @Deprecated
    public static void updateSchema(final ContentStore db) {
        db.updateSchema();
    }

    public static ContentStore createDataStore(JBakeConfiguration configuration) {
        if (contentStore == null) {
            contentStore = new ContentStore(configuration.getDatabaseStore(), configuration.getDatabasePath());
        }

        return contentStore;
    }

    public static void closeDataStore() {
        contentStore = null;
    }
    
    public static Map<String, Object> documentToModel(ODocument doc) {
        Map<String, Object> result = new HashMap<>();
        Iterator<Map.Entry<String, Object>> fieldIterator = doc.iterator();
        while (fieldIterator.hasNext()) {
            Map.Entry<String, Object> entry = fieldIterator.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Converts a DB list into a String array
     * @param entry     Entry input to be converted
     * @return          input entry as String[]
     */
    @SuppressWarnings("unchecked")
    public static String[] toStringArray(Object entry) {
    	if (entry instanceof String[]) {
            return (String[]) entry;
        } else if (entry instanceof OTrackedList) {
            OTrackedList<String> list = (OTrackedList<String>) entry;
            return list.toArray(new String[list.size()]);
        }
    	return new String[0];
    }

}
