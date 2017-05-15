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

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.jbake.model.DocumentAttributes;
import org.jbake.model.DocumentTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jdlee
 */
public class ContentStore {

    private final Logger logger = LoggerFactory.getLogger(ContentStore.class);
    private ODatabaseDocumentTx db;
    private long start = -1;
    private long limit = -1;

    public ContentStore(final String type, String name) {
        startupIfEnginesAreMissing();
        db = new ODatabaseDocumentTx(type + ":" + name);
        boolean exists = db.exists();
        if (!exists) {
            db.create();
        }
        db = new OPartitionedDatabasePoolFactory().get(type+":"+name, "admin", "admin").acquire();
        ODatabaseRecordThreadLocal.INSTANCE.set(db);
        if (!exists) {
            updateSchema();
        }
    }

    public long getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void resetPagination() {
        this.start = -1;
        this.limit = -1;
    }

    public final void updateSchema() {
        OSchema schema = db.getMetadata().getSchema();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            if (schema.getClass(docType) == null) {
                createDocType(schema, docType);
            }
        }
        if (schema.getClass("Signatures") == null) {
            createSignatureType(schema);
        }
    }

    public void close() {
        db.close();
        DBUtil.closeDataStore();
    }

    public void shutdown() {
        Orient.instance().shutdown();
    }

    private void startupIfEnginesAreMissing() {
        // If an instance of Orient was previously shutdown all engines are removed.
        // We need to startup Orient again.
        if ( Orient.instance().getEngines().size() == 0 ) {
            Orient.instance().startup();
        }
    }

    public void drop() {
        db.drop();
    }

    public long getDocumentCount(String docType) {
        return db.countClass(docType);
    }

    public long getPublishedCount(String docType) {
        return (Long) query("select count(*) as count from " + docType + " where status='published'").get(0).get("count");
    }

    public DocumentList getDocumentStatus(String docType, String uri) {
        return query("select sha1,rendered from " + docType + " where sourceuri=?", uri);

    }

    public DocumentList getPublishedPosts() {
        return getPublishedContent("post");
    }

    public DocumentList getPublishedPostsByTag(String tag) {
        return query("select * from post where status='published' and ? in tags order by date desc", tag);
    }

    public DocumentList getPublishedDocumentsByTag(String tag) {
        final DocumentList documents = new DocumentList();
        for (final String docType : DocumentTypes.getDocumentTypes()) {
            DocumentList documentsByTag = query("select * from " + docType + " where status='published' and ? in tags order by date desc", tag);
            documents.addAll(documentsByTag);
        }
        return documents;
    }

    public DocumentList getPublishedPages() {
        return getPublishedContent("page");
    }

    public DocumentList getPublishedContent(String docType) {
        String query = "select * from " + docType + " where status='published' order by date desc";
        if ((start >= 0) && (limit > -1)) {
            query += " SKIP " + start + " LIMIT " + limit;
        }
        return query(query);
    }

    public DocumentList getAllContent(String docType) {
        String query = "select * from " + docType + " order by date desc";
        if ((start >= 0) && (limit > -1)) {
            query += " SKIP " + start + " LIMIT " + limit;
        }
        return query(query);
    }

    public DocumentList getAllTagsFromPublishedPosts() {
        return query("select tags from post where status='published'");
    }

    public DocumentList getSignaturesForTemplates() {
        return query("select sha1 from Signatures where key='templates'");
    }

    public DocumentList getUnrenderedContent(String docType) {
        return query("select * from " + docType + " where rendered=false order by date desc");
    }

    public void deleteContent(String docType, String uri) {
        executeCommand("delete from " + docType + " where sourceuri=?", uri);
    }

    public void markConentAsRendered(String docType) {
        executeCommand("update " + docType + " set rendered=true where rendered=false and cached=true");
    }

    public void updateSignatures(String currentTemplatesSignature) {
        executeCommand("update Signatures set sha1=? where key='templates'", currentTemplatesSignature);
    }

    public void deleteAllByDocType(String docType) {
        executeCommand("delete from " + docType);
    }

    public void insertSignature(String currentTemplatesSignature) {
        executeCommand("insert into Signatures(key,sha1) values('templates',?)", currentTemplatesSignature);
    }

    private DocumentList query(String sql) {
        List<ODocument> results = db.query(new OSQLSynchQuery<ODocument>(sql));
        return DocumentList.wrap(results.iterator());
    }

    private DocumentList query(String sql, Object... args) {
        List<ODocument> results = db.command(new OSQLSynchQuery<ODocument>(sql)).execute(args);
        return DocumentList.wrap(results.iterator());
    }

    private void executeCommand(String query, Object... args) {
        db.command(new OCommandSQL(query)).execute(args);
    }

    public Set<String> getTags() {
        DocumentList docs = this.getAllTagsFromPublishedPosts();
        Set<String> result = new HashSet<String>();
        for (Map<String, Object> document : docs) {
            String[] tags = DBUtil.toStringArray(document.get(Crawler.Attributes.TAGS));
            Collections.addAll(result, tags);
        }
        return result;
    }

    public Set<String> getAllTags() {
        Set<String> result = new HashSet<String>();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            DocumentList docs = query("select tags from " + docType + " where status='published'");
            for (Map<String, Object> document : docs) {
                String[] tags = DBUtil.toStringArray(document.get(Crawler.Attributes.TAGS));
                Collections.addAll(result, tags);
            }
        }
        return result;
    }

    private void createDocType(final OSchema schema, final String doctype) {
        logger.debug("Create document class '{}'", doctype );

        OClass page = schema.createClass(doctype);
        page.createProperty(String.valueOf(DocumentAttributes.SHA1), OType.STRING).setNotNull(true);
        page.createIndex(doctype + "sha1Index", OClass.INDEX_TYPE.NOTUNIQUE, DocumentAttributes.SHA1.toString());
        page.createProperty(String.valueOf(DocumentAttributes.SOURCE_URI), OType.STRING).setNotNull(true);
        page.createIndex(doctype + "sourceUriIndex", OClass.INDEX_TYPE.UNIQUE, DocumentAttributes.SOURCE_URI.toString());
        page.createProperty(String.valueOf(DocumentAttributes.CACHED), OType.BOOLEAN).setNotNull(true);
        page.createIndex(doctype + "cachedIndex", OClass.INDEX_TYPE.NOTUNIQUE, DocumentAttributes.CACHED.toString());
        page.createProperty(String.valueOf(DocumentAttributes.RENDERED), OType.BOOLEAN).setNotNull(true);
        page.createIndex(doctype + "renderedIndex", OClass.INDEX_TYPE.NOTUNIQUE, DocumentAttributes.RENDERED.toString());
        page.createProperty(String.valueOf(DocumentAttributes.STATUS), OType.STRING).setNotNull(true);
        page.createIndex(doctype + "statusIndex", OClass.INDEX_TYPE.NOTUNIQUE, DocumentAttributes.STATUS.toString());
    }

    private void createSignatureType(OSchema schema) {
        OClass signatures = schema.createClass("Signatures");
        signatures.createProperty(String.valueOf(DocumentAttributes.SHA1), OType.STRING).setNotNull(true);
        signatures.createIndex("sha1Idx", OClass.INDEX_TYPE.UNIQUE, DocumentAttributes.SHA1.toString());
    }
}
