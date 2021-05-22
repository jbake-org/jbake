package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.template.ModelExtractor;
import static org.jbake.app.configuration.PropertyList.*;

import java.util.Map;

public class AllContentExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        Map<String, Object> config = (Map<String, Object>) model.get("config");
        String dataFileDocType = config.get(DATA_FILE_DOCTYPE.getKey().replace(".", "_")).toString();
        DocumentList<DocumentModel> allContent = new DocumentList<>();
        String[] documentTypes = DocumentTypes.getDocumentTypes();
        for (String docType : documentTypes) {
            if (!docType.equals(dataFileDocType)) {
                DocumentList<DocumentModel> query = db.getAllContent(docType);
                allContent.addAll(query);
            }
        }
        return allContent;
    }

}
