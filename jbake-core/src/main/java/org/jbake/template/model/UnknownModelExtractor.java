package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.jbake.model.TemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnknownModelExtractor implements ModelExtractor<DocumentList<DocumentModel>> {

    private final Logger logger = LoggerFactory.getLogger(UnknownModelExtractor.class);
    private final String className;

    public UnknownModelExtractor(String className) {
        this.className = className;
    }

    @Override
    public DocumentList<DocumentModel> get(ContentStore db, TemplateModel model, String key) {
        logger.debug("Could not find class [{}] to get document list by key [{}]", className, key);
        return new DocumentList<>();
    }
}
