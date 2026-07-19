package org.jbake.app;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SchemaManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaManager.class);
    private static final String STATEMENT_GET_SIGNATURE_FOR_TEMPLATES = "select sha1 from Signatures where key='templates'";
    private static final String STATEMENT_UPDATE_TEMPLATE_SIGNATURE = "update Signatures set sha1=? where key='templates'";
    private static final String STATEMENT_INSERT_TEMPLATES_SIGNATURE = "insert into Signatures(key,sha1) values('templates',?)";
    private static final String STATEMENT_DELETE_ALL = "delete from Documents where type='%s'";

    private final ODatabaseSession db;

    public SchemaManager(ODatabaseSession db) {
        this.db = db;
    }

    public void updateSchema() {
        OSchema schema = db.getMetadata().getSchema();

        if (!schema.existsClass("Documents")) {
            createDocuments(schema);
        }

        if (!schema.existsClass("Signatures")) {
            createSignatures(schema);
        }
    }

    private void createDocuments(OSchema schema) {
        LOGGER.debug("Create document class");

        OClass page = schema.createClass("Documents");
        page.createProperty(ModelAttributes.SHA1, OType.STRING).setNotNull(true);
        page.createIndex("Documentssha1Index", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.SHA1);

        page.createProperty(ModelAttributes.SOURCE_URI, OType.STRING).setNotNull(true);
        page.createIndex("DocumentssourceUriIndex", OClass.INDEX_TYPE.UNIQUE, ModelAttributes.SOURCE_URI);

        page.createProperty(ModelAttributes.CACHED, OType.BOOLEAN).setNotNull(true);
        page.createIndex("DocumentscachedIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.CACHED);

        page.createProperty(ModelAttributes.RENDERED, OType.BOOLEAN).setNotNull(true);
        page.createIndex("DocumentsrenderedIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.RENDERED);

        page.createProperty(ModelAttributes.STATUS, OType.STRING).setNotNull(true);
        page.createIndex("DocumentsstatusIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.STATUS);

        page.createProperty(ModelAttributes.TYPE, OType.STRING).setNotNull(true);
        page.createIndex("DocumentstypeIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.TYPE);
    }

    private void createSignatures(OSchema schema) {
        OClass signatures = schema.createClass("Signatures");
        signatures.createProperty(ModelAttributes.SHA1, OType.STRING).setNotNull(true);
        signatures.createIndex("sha1Idx", OClass.INDEX_TYPE.UNIQUE, ModelAttributes.SHA1);
    }
    private DocumentList<DocumentModel> getSignaturesForTemplates() {
        OResultSet results = db.query(STATEMENT_GET_SIGNATURE_FOR_TEMPLATES);
        return DocumentList.wrap(results);
    }
    public boolean updateTemplateSignatureIfChanged(File templateFolder) {
        boolean templateSignatureChanged = false;
        DocumentList<DocumentModel> docs = getSignaturesForTemplates();
        String currentTemplatesSignature;
        try {
            currentTemplatesSignature = FileUtil.sha1(templateFolder);
        } catch (Exception e) {
            currentTemplatesSignature = "";
        }
        if (!docs.isEmpty()) {
            String sha1 = docs.get(0).getSha1();

            if (!sha1.equals(currentTemplatesSignature)) {
                updateSignatures(currentTemplatesSignature);
                templateSignatureChanged = true;
            }
        } else {
            insertTemplatesSignature(currentTemplatesSignature);
            templateSignatureChanged = true;
        }

        return templateSignatureChanged;
    }

    private void updateSignatures(String sha1) {
        db.command(STATEMENT_UPDATE_TEMPLATE_SIGNATURE, sha1);
    }

    private void insertTemplatesSignature(String sha1) {
        db.command(STATEMENT_INSERT_TEMPLATES_SIGNATURE, sha1);
    }
    public void deleteAllByDocType(String docType) {
        String statement = String.format(STATEMENT_DELETE_ALL, docType);
        db.command(statement);
    }
    public void deleteAllDocumentTypes() {
        for (String docType : DocumentTypes.getDocumentTypes()) {
            try {
                deleteAllByDocType(docType);
            } catch (Exception e) {
                // maybe a non existing document type
            }
        }
    }

}
