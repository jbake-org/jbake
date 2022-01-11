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

import com.arcadedb.GlobalConfiguration;
import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.engine.Bucket;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Type;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

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

    private Database db;

    private long            start = -1;
    private long            limit = -1;
    private DatabaseFactory factory;

    public ContentStore(final String type, String name) {
        this.type = type;
        this.name = name;

        // USE A 4X BIGGER PAGE THAN THE DEFAULT
        GlobalConfiguration.BUCKET_DEFAULT_PAGE_SIZE.setValue(Bucket.DEF_PAGE_SIZE * 4);
    }

    public void startup() {
        factory = new DatabaseFactory(name);

        if( !factory.exists() )
            db = factory.create();
        else
            db = factory.open();

        db.setAutoTransaction(true);
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
        com.arcadedb.schema.Schema schema = db.getSchema();

        if (!schema.existsType(Schema.DOCUMENTS)) {
            createDocType(schema);
        }
        if (!schema.existsType(Schema.SIGNATURES)) {
            createSignatureType(schema);
        }
    }

    public void close() {
        if (db != null) {
            db.close();
        }

        if (factory != null) {
            factory.close();
        }
        DBUtil.closeDataStore();
    }

    public void shutdown() {
        close();
    }

    public void drop() {
        if( db != null)
            db.drop();
    }

    public long getDocumentCount(String docType) {
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

    private DocumentList<DocumentModel> query(String sql) {
        ResultSet results = db.query("sql", sql);
        return DocumentList.wrap(results);
    }

    private DocumentList<DocumentModel> query(String sql, Object... args) {
        ResultSet results = db.command("sql", sql, args);
        return DocumentList.wrap(results);
    }

    private void executeCommand(String query, Object... args) {
        db.command("sql", query, args);
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

    private void createDocType(final com.arcadedb.schema.Schema schema) {
        logger.debug("Create document class");

        DocumentType page = schema.createDocumentType(Schema.DOCUMENTS);
        page.createProperty(ModelAttributes.SHA1, Type.STRING);
        page.createTypeIndex(com.arcadedb.schema.Schema.INDEX_TYPE.LSM_TREE,false, ModelAttributes.SHA1);
        page.createProperty(ModelAttributes.SOURCE_URI, Type.STRING);
        page.createTypeIndex(com.arcadedb.schema.Schema.INDEX_TYPE.LSM_TREE, true,  ModelAttributes.SOURCE_URI);
        page.createProperty(ModelAttributes.CACHED, Type.BOOLEAN);
        page.createTypeIndex(com.arcadedb.schema.Schema.INDEX_TYPE.LSM_TREE, false, ModelAttributes.CACHED);
        page.createProperty(ModelAttributes.RENDERED, Type.BOOLEAN);
        page.createTypeIndex(com.arcadedb.schema.Schema.INDEX_TYPE.LSM_TREE, false, ModelAttributes.RENDERED);
        page.createProperty(ModelAttributes.STATUS, Type.STRING);
        page.createTypeIndex(com.arcadedb.schema.Schema.INDEX_TYPE.LSM_TREE, false, ModelAttributes.STATUS);
        page.createProperty(ModelAttributes.TYPE, Type.STRING);
        page.createTypeIndex(com.arcadedb.schema.Schema.INDEX_TYPE.LSM_TREE, false, ModelAttributes.TYPE);

    }

    private void createSignatureType(com.arcadedb.schema.Schema schema) {
        DocumentType signatures = schema.createDocumentType(Schema.SIGNATURES);
        signatures.createProperty(ModelAttributes.SHA1, Type.STRING);
        signatures.createTypeIndex(com.arcadedb.schema.Schema.INDEX_TYPE.LSM_TREE, true, ModelAttributes.SHA1);
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
        return true;
    }

    public void addDocument(final DocumentModel document) {
        final MutableDocument doc = db.newDocument(Schema.DOCUMENTS);
        doc.fromMap(document);
        doc.save();
    }

    protected abstract class Schema {
        static final String DOCUMENTS = "Documents";
        static final String SIGNATURES = "Signatures";
    }

}
