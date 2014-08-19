package org.jbake.app;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.jbake.model.DocumentTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DBUtil {
    public static ODatabaseDocumentTx createDB(final String type, String name) {
        ODatabaseDocumentTx db = new ODatabaseDocumentTx(type + ":" + name);
        boolean exists = db.exists();
        if (!exists) {
            db.create();
        }
        db = ODatabaseDocumentPool.global().acquire(type + ":" + name, "admin", "admin");
        ODatabaseRecordThreadLocal.INSTANCE.set(db);
        if (!exists) {
            updateSchema(db);
        }
        return db;
    }

    public static void updateSchema(final ODatabaseDocumentTx db) {
        OSchema schema = db.getMetadata().getSchema();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            if (schema.getClass(docType)==null) {
                createDocType(schema, docType);
            }
        }
        if (schema.getClass("Signatures")==null) {
            // create the sha1 signatures class
            OClass signatures = schema.createClass("Signatures");
            signatures.createProperty("key", OType.STRING).setNotNull(true);
            signatures.createProperty("sha1", OType.STRING).setNotNull(true);
        }
    }

    private static void createDocType(final OSchema schema, final String doctype) {
        OClass page = schema.createClass(doctype);
        page.createProperty("sha1", OType.STRING).setNotNull(true);
        page.createProperty("uri", OType.STRING).setNotNull(true);
        page.createProperty("rendered", OType.BOOLEAN).setNotNull(true);
        page.createProperty("cached", OType.BOOLEAN).setNotNull(true);

        // commented out because for some reason index seems to be written
        // after the database is closed to this triggers an exception
        //page.createIndex("uriIdx", OClass.INDEX_TYPE.UNIQUE, "uri");
        //page.createIndex("renderedIdx", OClass.INDEX_TYPE.NOTUNIQUE, "rendered");
    }

    public static Map<String, Object> documentToModel(ODocument doc) {
        Map<String, Object> result = new HashMap<String, Object>();
        Iterator<Map.Entry<String, Object>> fieldIterator = doc.iterator();
        while (fieldIterator.hasNext()) {
            Map.Entry<String, Object> entry = fieldIterator.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static List<ODocument> query(ODatabaseDocumentTx db, String query, Object... args) {
        return db.command(new OSQLSynchQuery<ODocument>(query)).execute(args);
    }

    public static void update(ODatabaseDocumentTx db, String query, Object... args) {
        db.command(new OCommandSQL(query)).execute(args);
    }

    public static DocumentIterator fetchDocuments(ODatabaseDocumentTx db, String query, Object... args) {
        return new DocumentIterator(query(db, query, args).iterator());
    }

    /**
     * Converts a DB list into a String array
     */
    @SuppressWarnings("unchecked")
    public static String[] toStringArray(Object entry) {
    	if(entry==null) {
    		return new String[0];
    	} else if (entry instanceof String[]) {
            return (String[]) entry;
        } else if (entry instanceof OTrackedList) {
            OTrackedList<String> list = (OTrackedList<String>) entry;
            return list.toArray(new String[list.size()]);
        }
        throw new IllegalArgumentException("Unable to convert object to String[]");
    }

}
