package org.jbake.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class used to determine the list of locale types. Default is just one existing locale.
 * <p/>
 * Additional document types are added at runtime based on the types found in the configuration.
 *
 * @author asemelit
 */
public class LocaleTypes {

    private static final Map<String, Set<String>> DEFAULT_LOC_TYPES = new HashMap<String, Set<String>>(); //empty by default

    public static void addLocale(String locale, Set<String> docTypeSet) {
        if (hasLocale(locale)) {
            DEFAULT_LOC_TYPES.get(locale).addAll(docTypeSet);
        }
        else {
            DEFAULT_LOC_TYPES.put(locale, docTypeSet);
        }
    }

    public static String[] getLocales() {       
        return DEFAULT_LOC_TYPES.keySet().toArray(new String[DEFAULT_LOC_TYPES.size()]);
    }
    
    public static boolean hasLocale(String locale) {
        return DEFAULT_LOC_TYPES.containsKey(locale);
    }

    public static boolean hasLocalizedType(String locale, String docType) {
        return DEFAULT_LOC_TYPES.containsKey(locale) ? DEFAULT_LOC_TYPES.get(locale).contains(docType) : false;
    }

    
    public static String[] getLocalizedDocTypes(String locale) {
        if (hasLocale(locale)) {
            return DEFAULT_LOC_TYPES.get(locale).toArray(new String[DEFAULT_LOC_TYPES.size()]);
        }
        else {
            return new String[0];
        }
    }
    
    public static String[] getLocalesForType(String docType) {
        ArrayList<String> result = new ArrayList<String>();
        for (Map.Entry<String, Set<String>> localeMapping : DEFAULT_LOC_TYPES.entrySet()) {
            if (localeMapping.getValue().contains(docType)) {
                result.add(localeMapping.getKey());
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
