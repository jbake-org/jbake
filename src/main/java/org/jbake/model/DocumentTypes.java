package org.jbake.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility class used to determine the list of document types. Currently only supports "page", "post", "index",
 * "archive" and "feed".
 * <p/>
 * Additional document types are added at runtime based on the types found in the configuration.
 *
 * @author Cédric Champeau
 */
public class DocumentTypes {

    private static final Set<String> DEFAULT_DOC_TYPES = new LinkedHashSet<String>();

    static {
        resetDocumentTypes();
    }

    public static void resetDocumentTypes() {
        DEFAULT_DOC_TYPES.clear();
        DEFAULT_DOC_TYPES.addAll(Arrays.asList("page", "post", "masterindex", "archive", "feed"));
    }

    public static void addDocumentType(String docType) {
        DEFAULT_DOC_TYPES.add(docType);
    }

    public static String[] getDocumentTypes() {
        return DEFAULT_DOC_TYPES.toArray(new String[DEFAULT_DOC_TYPES.size()]);
    }

}
