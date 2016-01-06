package org.jbake.app;

import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DBUtil {
    private static ContentStore contentStore;
    
    public static ContentStore createDataStore(final String type, String name) {
        if (contentStore == null) {
            contentStore = new ContentStore(type, name);
        }
        return contentStore;
    }
    
    public static void closeDataStore() {
        contentStore = null;
    }
    
    public static void updateSchema(final ContentStore db) {
        db.updateSchema();
    }

    public static Map<String, Object> documentToModel(ODocument doc) {
        Map<String, Object> result = new HashMap<String, Object>();
        Iterator<Map.Entry<String, Object>> fieldIterator = doc.iterator();
        while (fieldIterator.hasNext()) {
            Map.Entry<String, Object> entry = fieldIterator.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

//    private static List<ODocument> query(ContentStore db, String query, Object... args) {
//        return db.command(new OSQLSynchQuery<ODocument>(query)).execute(args);
//    }
//
//    public static void update(ContentStore db, String query, Object... args) {
//        db.command(new OCommandSQL(query)).execute(args);
//    }
//
//    public static DocumentIterator fetchDocuments(ContentStore db, String query, Object... args) {
//        return new DocumentIterator(query(db, query, args).iterator());
//    }

    /**
     * Converts a DB list into a String array
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
