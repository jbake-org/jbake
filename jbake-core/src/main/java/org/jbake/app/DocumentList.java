package org.jbake.app;

import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultSet;
import org.jbake.model.DocumentModel;

import java.util.*;

/**
 * Wraps an OrientDB document iterator into a model usable by
 * template engines.
 *
 * @author Cédric Champeau
 */
public class DocumentList<T> extends LinkedList<T> {

    public static DocumentList<DocumentModel> wrap(ResultSet docs) {
        DocumentList<DocumentModel> list = new DocumentList<>();
        while (docs.hasNext()) {
            Result next = docs.next();
            list.add(DBUtil.documentToModel(next));
        }
        docs.close();
        return list;
    }

}
