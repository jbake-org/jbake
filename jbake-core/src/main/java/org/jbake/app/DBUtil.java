package org.jbake.app;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;

import java.util.ArrayList;

public class DBUtil {
    private static ContentStore contentStore;

    @Deprecated
    public static ContentStore createDataStore(final String type, String name) {
        if (contentStore == null) {
            contentStore = new ContentStore();
        }
        return contentStore;
    }

    @Deprecated
    public static void updateSchema(final ContentStore db) {
        // no-op
    }

    public static ContentStore createDataStore(JBakeConfiguration configuration) {
        if (contentStore == null) {
            contentStore = new ContentStore();
        }

        return contentStore;
    }

    public static void closeDataStore() {
        contentStore = null;
    }

    /**
     * Converts a list entry into a String array.
     *
     * @param entry Entry input to be converted
     * @return input entry as String[]
     */
    @SuppressWarnings("unchecked")
    public static String[] toStringArray(Object entry) {
        if (entry instanceof String[]) {
            return (String[]) entry;
        } else if (entry instanceof ArrayList) {
            ArrayList<String> list = (ArrayList<String>) entry;
            return list.toArray(new String[0]);
        }
        return new String[0];
    }

}
