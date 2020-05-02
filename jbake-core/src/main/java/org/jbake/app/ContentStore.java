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

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.jbake.launcher.SystemExit;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jdlee
 */
public class ContentStore {

    private static final String STATEMENT_GET_PUBLISHED_POST_BY_TYPE_AND_TAG = "select * from Documents where status='published' and type='%s' and ? in tags order by date desc";
    private static final String STATEMENT_GET_DOCUMENT_STATUS_BY_DOCTYPE_AND_URI = "select sha1,rendered from Documents where sourceuri=?";
    private static final String STATEMENT_GET_PUBLISHED_COUNT = "select count(*) as count from Documents where status='published' and type='%s'";
    private static final String STATEMENT_MARK_CONTENT_AS_RENDERD = "update Documents set rendered=true where rendered=false and type='%s' and sourceuri='%s' and cached=true";
    private static final String STATEMENT_DELETE_DOCTYPE_BY_SOURCEURI = "delete from Documents where sourceuri=?";
    private static final String STATEMENT_GET_UNDRENDERED_CONTENT = "select * from Documents where rendered=false order by date desc";
    private static final String STATEMENT_GET_SIGNATURE_FOR_TEMPLATES = "select sha1 from Signatures where key='templates'";
    private static final String STATEMENT_GET_TAGS_FROM_PUBLISHED_POSTS = "select tags from Documents where status='published' and type='post'";
    private static final String STATEMENT_GET_ALL_CONTENT_BY_DOCTYPE = "select * from Documents where type='%s' order by date desc";
    private static final String STATEMENT_GET_PUBLISHED_CONTENT_BY_DOCTYPE = "select * from Documents where status='published' and type='%s' order by date desc";
    private static final String STATEMENT_GET_PUBLISHED_POSTS_BY_TAG = "select * from Documents where status='published' and type='post' and ? in tags order by date desc";
    private static final String STATEMENT_GET_TAGS_BY_DOCTYPE = "select tags from Documents where status='published' and type='%s'";
    private static final String STATEMENT_INSERT_TEMPLATES_SIGNATURE = "insert into Signatures(key,sha1) values('templates',?)";
    private static final String STATEMENT_DELETE_ALL = "delete from Documents where type='%s'";
    private static final String STATEMENT_UPDATE_TEMPLATE_SIGNATURE = "update Signatures set sha1=? where key='templates'";
    private static final String STATEMENT_GET_DOCUMENT_COUNT_BY_TYPE = "select count(*) as count from Documents where type='%s'";

    private final Logger logger = LoggerFactory.getLogger(ContentStore.class);
    private final String type;
    private final String name;

    private ODatabaseSession db;

    private long start = -1;
    private long limit = -1;
    private OrientDB orient;

    public ContentStore(final String type, String name) {
        this.type = type;
        this.name = name;
    }


    public void startup() {
        startupIfEnginesAreMissing();

        if (type.equalsIgnoreCase(ODatabaseType.PLOCAL.name())) {
            orient = new OrientDB(type + ":" + name, OrientDBConfig.defaultConfig());
        } else {
            orient = new OrientDB(type + ":", OrientDBConfig.defaultConfig());
        }

        orient.createIfNotExists(name, ODatabaseType.valueOf(type.toUpperCase()));

        db = orient.open(name, "admin", "admin");

        activateOnCurrentThread();

        updateSchema();
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

        if (!schema.existsClass(Schema.DOCUMENTS)) {
            createDocType(schema);
        }
        if (!schema.existsClass(Schema.SIGNATURES)) {
            createSignatureType(schema);
        }
    }

    public void close() {
        if (db != null) {
            activateOnCurrentThread();
            db.close();
        }

        if (orient != null) {
            orient.close();
        }
        DBUtil.closeDataStore();
    }

    public void shutdown() {

//        Orient.instance().shutdown();
    }

    private void startupIfEnginesAreMissing() {
        // Using a jdk which doesn't bundle a javascript engine
        // throws a NoClassDefFoundError while logging the warning
        // see https://github.com/orientechnologies/orientdb/issues/5855
        OLogManager.instance().setWarnEnabled(false);

        // If an instance of Orient was previously shutdown all engines are removed.
        // We need to startup Orient again.
        if (Orient.instance().getEngines().isEmpty()) {
            Orient.instance().startup();
        }
        OLogManager.instance().setWarnEnabled(true);
    }

    public void drop() {
        activateOnCurrentThread();
        orient.drop(name);
    }

    private void activateOnCurrentThread() {
        if (db != null) {
            db.activateOnCurrentThread();
        } else {
            System.out.println("db is null on activate");
        }
    }

    public long getDocumentCount(String docType) {
        activateOnCurrentThread();
        String statement = String.format(STATEMENT_GET_DOCUMENT_COUNT_BY_TYPE, docType);
        return (long) query(statement).get(0).get("count");
    }

    public long getPublishedCount(String docType) {
        String statement = String.format(STATEMENT_GET_PUBLISHED_COUNT, docType);
        return (long) query(statement).get(0).get("count");
    }

    public DocumentList<DocumentModel> getDocumentByUri(String uri) {
        return query("select * from Documents where sourceuri=?", uri);
    }

    public DocumentList<DocumentModel> getDocumentStatus(String uri) {
        return query(STATEMENT_GET_DOCUMENT_STATUS_BY_DOCTYPE_AND_URI, uri);
    }

    public DocumentList<DocumentModel> getPublishedPosts() {
        return getPublishedContent("post");
    }

    public DocumentList<DocumentModel> getPublishedPosts(boolean applyPaging) {
        return getPublishedContent("post", applyPaging);
    }

    public DocumentList<DocumentModel> getPublishedPostsByTag(String tag) {
        return query(STATEMENT_GET_PUBLISHED_POSTS_BY_TAG, tag);
    }

    public DocumentList<DocumentModel> getPublishedDocumentsByTag(String tag) {
        final DocumentList<DocumentModel> documents = new DocumentList<>();

        for (final String docType : DocumentTypes.getDocumentTypes()) {
            String statement = String.format(STATEMENT_GET_PUBLISHED_POST_BY_TYPE_AND_TAG, docType);
            DocumentList<DocumentModel> documentsByTag = query(statement, tag);
            documents.addAll(documentsByTag);
        }
        return documents;
    }

    public DocumentList<DocumentModel> getPublishedPages() {
        return getPublishedContent("page");
    }

    public DocumentList<DocumentModel> getPublishedContent(String docType) {
        return getPublishedContent(docType, false);
    }

    private DocumentList<DocumentModel> getPublishedContent(String docType, boolean applyPaging) {
        String query = String.format(STATEMENT_GET_PUBLISHED_CONTENT_BY_DOCTYPE, docType);
        if (applyPaging && hasStartAndLimitBoundary()) {
            query += " SKIP " + start + " LIMIT " + limit;
        }
        return query(query);
    }

    public DocumentList<DocumentModel> getAllContent(String docType) {
        return getAllContent(docType, false);
    }

    public DocumentList<DocumentModel> getAllContent(String docType, boolean applyPaging) {
        String query = String.format(STATEMENT_GET_ALL_CONTENT_BY_DOCTYPE, docType);
        if (applyPaging && hasStartAndLimitBoundary()) {
            query += " SKIP " + start + " LIMIT " + limit;
        }
        return query(query);
    }

    private boolean hasStartAndLimitBoundary() {
        return (start >= 0) && (limit > -1);
    }

    private DocumentList<DocumentModel> getAllTagsFromPublishedPosts() {
        return query(STATEMENT_GET_TAGS_FROM_PUBLISHED_POSTS);
    }

    private DocumentList<DocumentModel> getSignaturesForTemplates() {
        return query(STATEMENT_GET_SIGNATURE_FOR_TEMPLATES);
    }

    public DocumentList<DocumentModel> getUnrenderedContent() {
        return query(STATEMENT_GET_UNDRENDERED_CONTENT);
    }

    public void deleteContent(String uri) {
        executeCommand(STATEMENT_DELETE_DOCTYPE_BY_SOURCEURI, uri);
    }

    public void markContentAsRendered(DocumentModel document) {
        String statement = String.format(STATEMENT_MARK_CONTENT_AS_RENDERD, document.getType(), document.getSourceuri());
        executeCommand(statement);
    }

    private void updateSignatures(String currentTemplatesSignature) {
        executeCommand(STATEMENT_UPDATE_TEMPLATE_SIGNATURE, currentTemplatesSignature);
    }

    public void deleteAllByDocType(String docType) {
        String statement = String.format(STATEMENT_DELETE_ALL, docType);
        executeCommand(statement);
    }

    private void insertTemplatesSignature(String currentTemplatesSignature) {
        executeCommand(STATEMENT_INSERT_TEMPLATES_SIGNATURE, currentTemplatesSignature);
    }

    private synchronized DocumentList<DocumentModel> query(String sql) {
        activateOnCurrentThread();
        OResultSet results = db.query(sql);
        return DocumentList.wrap(results);
    }

    private synchronized DocumentList<DocumentModel> query(String sql, Object... args) {
        activateOnCurrentThread();
        OResultSet results = db.command(sql, args);
        return DocumentList.wrap(results);
    }

    private synchronized void executeCommand(String query, Object... args) {
        activateOnCurrentThread();
        db.getTransaction().begin();
        db.command(query, args);
        db.getTransaction().commit();
    }

    public Set<String> getTags() {
        DocumentList<DocumentModel> docs = this.getAllTagsFromPublishedPosts();
        Set<String> result = new HashSet<>();
        for (DocumentModel document : docs) {
            String[] tags = document.getTags();
            Collections.addAll(result, tags);
        }
        return result;
    }

    public Set<String> getAllTags() {
        Set<String> result = new HashSet<>();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            String statement = String.format(STATEMENT_GET_TAGS_BY_DOCTYPE, docType);
            DocumentList<DocumentModel> docs = query(statement);
            for (DocumentModel document : docs) {
                String[] tags = document.getTags();
                Collections.addAll(result, tags);
            }
        }
        return result;
    }

    private void createDocType(final OSchema schema) {
        logger.debug("Create document class");

        OClass page = schema.createClass(Schema.DOCUMENTS);
        page.createProperty(ModelAttributes.SHA1, OType.STRING).setNotNull(true);
        page.createIndex(Schema.DOCUMENTS + "sha1Index", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.SHA1);
        page.createProperty(ModelAttributes.SOURCE_URI, OType.STRING).setNotNull(true);
        page.createIndex(Schema.DOCUMENTS + "sourceUriIndex", OClass.INDEX_TYPE.UNIQUE, ModelAttributes.SOURCE_URI);
        page.createProperty(ModelAttributes.CACHED, OType.BOOLEAN).setNotNull(true);
        page.createIndex(Schema.DOCUMENTS + "cachedIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.CACHED);
        page.createProperty(ModelAttributes.RENDERED, OType.BOOLEAN).setNotNull(true);
        page.createIndex(Schema.DOCUMENTS + "renderedIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.RENDERED);
        page.createProperty(ModelAttributes.STATUS, OType.STRING).setNotNull(true);
        page.createIndex(Schema.DOCUMENTS + "statusIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.STATUS);
        page.createProperty(ModelAttributes.TYPE, OType.STRING).setNotNull(true);
        page.createIndex(Schema.DOCUMENTS + "typeIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.TYPE);

    }

    private void createSignatureType(OSchema schema) {
        OClass signatures = schema.createClass(Schema.SIGNATURES);
        signatures.createProperty(ModelAttributes.SHA1, OType.STRING).setNotNull(true);
        signatures.createIndex("sha1Idx", OClass.INDEX_TYPE.UNIQUE, ModelAttributes.SHA1);

        signatures.createProperty("key", OType.STRING);
        signatures.createIndex("kexIdx", OClass.INDEX_TYPE.UNIQUE, "key");
    }

    public void updateAndClearCacheIfNeeded(boolean needed, File templateFolder) {

        boolean clearCache = needed;

        if (!needed) {
            clearCache = updateTemplateSignatureIfChanged(templateFolder);
        }

        if (clearCache) {
            deleteAllDocumentTypes();
            this.updateSchema();
        }
    }

    private boolean updateTemplateSignatureIfChanged(File templateFolder) {
        boolean templateSignatureChanged = false;

        DocumentList<DocumentModel> docs = this.getSignaturesForTemplates();
        String currentTemplatesSignature;
        try {
            currentTemplatesSignature = FileUtil.sha1(templateFolder);
        } catch (Exception e) {
            currentTemplatesSignature = "";
        }
        if (!docs.isEmpty()) {
            String sha1 = docs.get(0).getSha1();
            if (!sha1.equals(currentTemplatesSignature)) {
                this.updateSignatures(currentTemplatesSignature);
                templateSignatureChanged = true;
            }
        } else {
            // first computation of templates signature
            this.insertTemplatesSignature(currentTemplatesSignature);
            templateSignatureChanged = true;
        }
        return templateSignatureChanged;
    }

    private void deleteAllDocumentTypes() {
        for (String docType : DocumentTypes.getDocumentTypes()) {
            try {
                this.deleteAllByDocType(docType);
            } catch (Exception e) {
                // maybe a non existing document type
            }
        }
    }

    public boolean isActive() {
        return db.isActiveOnCurrentThread();
    }

    public synchronized void addDocument(DocumentModel document) {
        db.getTransaction().begin();
        ODocument doc = new ODocument(Schema.DOCUMENTS);
        doc.fromMap(document);
        doc.save();
        db.getTransaction().commit();
    }

    protected abstract class Schema {
        static final String DOCUMENTS = "Documents";
        static final String SIGNATURES = "Signatures";
    }

}
