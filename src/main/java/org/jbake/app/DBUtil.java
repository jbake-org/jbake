package org.jbake.app;

import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DBUtil {
    public static ContentStore createDataStore(final String type, String name) {
        return new ContentStore(type, name);
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
    	if(entry==null) {
    		return new String[0];
    	} else if (entry instanceof String[]) {
            return (String[]) entry;
        } else if (entry instanceof OTrackedList) {
            OTrackedList<String> list = (OTrackedList<String>) entry;
            return list.toArray(new String[list.size()]);
        }
        throw new IllegalArgumentException("Unable to convert object to String[]");
    }

}
