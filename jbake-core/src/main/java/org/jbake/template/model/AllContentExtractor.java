package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.TemplateModel;

import java.util.Map;

import static org.jbake.app.configuration.PropertyList.DATA_FILE_DOCTYPE;

public class AllContentExtractor implements ModelExtractor<DocumentList<DocumentModel>> {

    @Override
    public DocumentList<DocumentModel> get(ContentStore db, TemplateModel model, String key) {
        Map<String, Object> config = model.getConfig();
        String dataFileDocType = config.get(DATA_FILE_DOCTYPE.getKey().replace(".", "_")).toString();
        DocumentList<DocumentModel> allContent = new DocumentList<>();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            if (!docType.equals(dataFileDocType)) {
                DocumentList<DocumentModel> query = db.getAllContent(docType);
                allContent.addAll(query);
            }
        }
        return allContent;
    }

}
