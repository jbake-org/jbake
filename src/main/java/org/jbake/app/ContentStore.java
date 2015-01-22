/*
 * The MIT License
 *
 * Copyright 2015 jdlee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jbake.app;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.query.OQuery;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import java.util.List;
import static org.jbake.app.DBUtil.updateSchema;
import org.jbake.model.DocumentTypes;

/**
 *
 * @author jdlee
 */
public class ContentStore {
    private ODatabaseDocumentTx db;
    
    public ContentStore(final String type, String name) {
        db = new ODatabaseDocumentTx(type + ":" + name);
        boolean exists = db.exists();
        if (!exists) {
            db.create();
        }
        db = ODatabaseDocumentPool.global().acquire(type + ":" + name, "admin", "admin");
        ODatabaseRecordThreadLocal.INSTANCE.set(db);
        if (!exists) {
            updateSchema();
        }
    }
    
    public  void updateSchema() {
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



    public void close() {
        db.close();
    }

    public void drop() {
        db.drop();
    }

    public long countClass(String iClassName) {
        return db.countClass(iClassName);
    }

    private static void createDocType(final OSchema schema, final String doctype) {
        OClass page = schema.createClass(doctype);
        page.createProperty("sha1", OType.STRING).setNotNull(true);
        page.createProperty("sourceuri", OType.STRING).setNotNull(true);
        page.createProperty("rendered", OType.BOOLEAN).setNotNull(true);
        page.createProperty("cached", OType.BOOLEAN).setNotNull(true);

        // commented out because for some reason index seems to be written
        // after the database is closed to this triggers an exception
        //page.createIndex("uriIdx", OClass.INDEX_TYPE.UNIQUE, "uri");
        //page.createIndex("renderedIdx", OClass.INDEX_TYPE.NOTUNIQUE, "rendered");
    }    

    public List<ODocument> getPublishedPosts() {
        return getPublishedContent("post");
    }

    public List<ODocument> getPublishedPages() {
        return getPublishedContent("page");
    }

    public List<ODocument> getPublishedContent(String docType) {
        return query("select * from "+docType+" where status='published' order by date desc");
    }

    public List<ODocument> getAllContent(String docType) {
        return query("select * from "+docType+" order by date desc");
    }

    public List<ODocument> getAllTagsFromPublishedPosts() {
        return query("select tags from post where status='published'");
    }

    public List<ODocument> getPublishedPostsByTag(String tag) {
        return executeCommand("select * from post where status='published' where ? in tags order by date desc", tag);
    }

    public List<ODocument> getSignatures() {
        return query("select sha1 from Signatures where key='templates'");
    }

    public List<ODocument> getDocumentStatus(String docType, String uri) {
        return executeCommand("select sha1,rendered from " + docType + " where sourceuri=?", uri);

    }

    public void deleteContent(String docType, String uri) {
        executeCommand("delete from " + docType + " where sourceuri=?", uri);
    }

    public List<ODocument> getUnrenderedContent(String docType) {
        return query("select * from " + docType + " where rendered=false");
    }

    public void markConentAsRendered(String docType) {
        executeCommand("update " + docType + " set rendered=true where rendered=false and cached=true");
    }

    public void updateSignatures(String currentTemplatesSignature) {
        executeCommand("update Signatures set sha1=? where key='templates'", currentTemplatesSignature);
    }
    
    public void deleteAllByDocType(String docType) {
        executeCommand("delete from "+docType);
    }

    public void insertSignature(String currentTemplatesSignature) {
        executeCommand("insert into Signatures(key,sha1) values('templates',?)", currentTemplatesSignature);
    }

    private List<ODocument> query (String sql) {
        return db.query(new OSQLSynchQuery<ODocument>(sql));
    }

    private List<ODocument> executeCommand(String query, Object... args) {
        return db.command(new OSQLSynchQuery<ODocument>(query)).execute(args);
    }
}
