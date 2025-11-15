package org.jbake.app;

import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.sql.executor.OResult;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;

import java.util.ArrayList;

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

    public static DocumentModel documentToModel(OResult doc) {
        DocumentModel result = new DocumentModel();

        for (String key : doc.getPropertyNames()) {
            result.put(key, doc.getProperty(key));
        }
        return result;
    }

    /**
     * Converts a DB list into a String array
     *
     * @param entry Entry input to be converted
     * @return input entry as String[]
     */
    @SuppressWarnings("unchecked")
    public static String[] toStringArray(Object entry) {
        if (entry instanceof String[]) {
            return (String[]) entry;
        } else if (entry instanceof OTrackedList) {
            OTrackedList<String> list = (OTrackedList<String>) entry;
            return list.toArray(new String[list.size()]);
        } else if (entry instanceof ArrayList) {
            ArrayList<String> list = (ArrayList<String>) entry;
            return list.toArray(new String[list.size()]);
        }
        return new String[0];
    }

}
