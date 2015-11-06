package org.jbake.app;

import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.HashMap;
import java.util.Map;

public class DBUtil {
    public static ContentStore createDB(final String type, String name) {
        return new ContentStore(type, name);
    }

    public static ContentStore createDataStore(final String type, String name) {
        return new ContentStore(type, name);
    }

    public static void updateSchema(final ContentStore db) {
        db.updateSchema();
    }

    public static Map<String, Object> documentToModel(ODocument doc) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : doc) {
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
