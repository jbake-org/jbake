package org.jbake.app;

import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Wraps an OrientDB document iterator into a model usable by
 * template engines.
 *
 * @author Cédric Champeau
 */
public class DocumentList extends LinkedList<Map<String,Object>> {

    public static DocumentList wrap(Iterator<ODocument> docs) {
        DocumentList list = new DocumentList();
        while (docs.hasNext()) {
            ODocument next = docs.next();
            list.add(DBUtil.documentToModel(next));
        }
        return list;
    }

    public DocumentList() {
    }

}
