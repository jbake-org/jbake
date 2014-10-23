package org.jbake.template.model;

import org.jbake.model.DocumentTypes;

public class ModelExtractionUtils {
	public static String unpluralizeDocumentType(String pluralized) {
        String[] documentTypes = DocumentTypes.getDocumentTypes();
        for (String docType : documentTypes) {
            if ((docType+"s").equals(pluralized)) {
            	return docType;
            }
        }
        throw new UnsupportedOperationException("there is no document type we can pluralize as \""+pluralized+"\"\n"
        				+ "We only have "+documentTypes);
	}
}
