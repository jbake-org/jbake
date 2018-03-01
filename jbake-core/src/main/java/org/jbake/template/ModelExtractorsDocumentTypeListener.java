package org.jbake.template;

import org.jbake.model.DocumentTypeListener;

public class ModelExtractorsDocumentTypeListener implements DocumentTypeListener {

    @Override
    public void added(String doctype) {
        ModelExtractors.getInstance().registerExtractorsForCustomTypes(doctype);
    }
}
