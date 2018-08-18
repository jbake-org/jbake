package org.jbake.app;

import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jbake.model.BaseModel;
import org.jbake.model.DocumentModel;

import java.util.LinkedList;

/**
 * Wraps an OrientDB document iterator into a model usable by
 * template engines.
 *
 * @author Cédric Champeau
 */
public class DocumentList extends LinkedList<BaseModel> {

    public static DocumentList wrap(OResultSet docs) {
        DocumentList list = new DocumentList();
        while (docs.hasNext()) {
            OResult next = docs.next();
            list.add(DBUtil.documentToModel(next));
        }
        docs.close();
        return list;
    }

    public DocumentModel getDocumentModel(int index) {
        return (DocumentModel) get(index);
    }
}
