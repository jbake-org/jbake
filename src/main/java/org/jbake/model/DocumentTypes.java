package org.jbake.model;

import org.jbake.parser.Engines;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>Utility class used to determine the list of document types. Currently only supports "page", "post", "index",
 * "archive" and "feed".</p>
 * <p>Additional document types are added at runtime based on the types found in the configuration.</p>
 *
 * @author Cédric Champeau
 */
public class DocumentTypes {

	private static final Set<String> DEFAULT_DOC_TYPES = new LinkedHashSet<String>();
    private static final Set<DocumentTypeListener> LISTENERS = new HashSet<DocumentTypeListener>();

    static {
    	resetDocumentTypes();
    }
    
    public static void resetDocumentTypes() {
    	DEFAULT_DOC_TYPES.clear();
    	DEFAULT_DOC_TYPES.addAll(Arrays.asList("page", "post", "masterindex", "archive", "feed"));
    }
    
    
    public static void addDocumentType(String docType) {
        DEFAULT_DOC_TYPES.add(docType);
        notifyListener(docType);
    }

    private static void notifyListener(String docType) {
        for ( DocumentTypeListener listener : LISTENERS) {
            listener.added(docType);
        }
    }

    public static void addListener( DocumentTypeListener listener ) {
        LISTENERS.add(listener);
    }

    /**
     * Notice additional document types are added automagically before returning them
     * @return all supported document types
     */
    public static String[] getDocumentTypes() {
    	// TODO: is this needed?
        // make sure engines are loaded before to get document types
        Engines.getRecognizedExtensions();
        return DEFAULT_DOC_TYPES.toArray(new String[DEFAULT_DOC_TYPES.size()]);
    }

    public static boolean contains(String documentType) {
        return DEFAULT_DOC_TYPES.contains(documentType);
    }
}
