package org.jbake.template.model;

import org.jbake.engine.ModelExtractors;
import org.jbake.model.DocumentTypeListener;

public class ModelExtractorsDocumentTypeListener implements DocumentTypeListener {

    @Override
    public void added(String doctype) {
        ModelExtractors.getInstance().registerExtractorsForCustomTypes(doctype);
    }
}
