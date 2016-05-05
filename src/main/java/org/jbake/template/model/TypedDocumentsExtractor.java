package org.jbake.template.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypeUtils;
import org.jbake.template.ModelExtractor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TypedDocumentsExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        // document types are pluralized in model, so unpluralize
        try {
            String type = DocumentTypeUtils.unpluralize(key);
            return DocumentList.wrap(db.getAllContent(type).iterator());
        } catch (UnsupportedOperationException e) {
            List<ODocument> none = Collections.emptyList();
            return DocumentList.wrap(none.iterator());
        }
    }

}