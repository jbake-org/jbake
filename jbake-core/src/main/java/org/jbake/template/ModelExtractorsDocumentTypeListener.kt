package org.jbake.template

import org.jbake.model.DocumentTypeListener

class ModelExtractorsDocumentTypeListener : DocumentTypeListener {
    override fun added(doctype: String) {
        ModelExtractors.instance.registerExtractorsForCustomTypes(doctype)
    }
}
