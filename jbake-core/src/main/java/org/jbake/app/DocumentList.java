package org.jbake.app;

import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.util.LinkedList;
import java.util.Map;

/**
 * Wraps an OrientDB document iterator into a model usable by
 * template engines.
 *
 * @author Cédric Champeau
 */
public class DocumentList extends LinkedList<Map<String, Object>> {

    public static DocumentList wrap(OResultSet docs) {
        DocumentList list = new DocumentList();
        while (docs.hasNext()) {
            OResult next = docs.next();
            list.add(DBUtil.documentToModel(next));
        }
        docs.close();
        return list;
    }

}
