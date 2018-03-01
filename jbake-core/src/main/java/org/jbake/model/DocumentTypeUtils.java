package org.jbake.model;

public class DocumentTypeUtils {
    public static String unpluralize(String pluralized) {

        if ((pluralized == null) || (pluralized.length() == 0)) {
            throw new IllegalArgumentException("pluralized string should not be null or length should be bigger than zero");
        }
        String[] documentTypes = DocumentTypes.getDocumentTypes();

        String unpluralizedDoctype = pluralized.substring(0, pluralized.length() - 1);
        if (DocumentTypes.contains(unpluralizedDoctype)) {
            return unpluralizedDoctype;
        }
        throw new UnsupportedOperationException("there is no document type pluralized as \"" + pluralized + "\"\n"
                + "We only have " + documentTypes);
    }

    public static String pluralize(String documentType) {
        String[] documentTypes = DocumentTypes.getDocumentTypes();

        if (DocumentTypes.contains(documentType)) {
            return documentType + "s";
        }
        throw new UnsupportedOperationException("there is no document type \"" + documentType + "\" we can pluralize\n"
                + "We only have " + documentTypes);
    }
}