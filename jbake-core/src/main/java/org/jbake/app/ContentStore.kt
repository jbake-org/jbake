package org.jbake.app

import com.orientechnologies.common.log.OLogManager
import com.orientechnologies.orient.core.Orient
import com.orientechnologies.orient.core.db.ODatabaseSession
import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.OrientDBConfig
import com.orientechnologies.orient.core.metadata.schema.OClass
import com.orientechnologies.orient.core.metadata.schema.OSchema
import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.record.ORecord
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes
import org.jbake.model.ModelAttributes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class ContentStore(private val type: String, private val name: String?) {

    private lateinit var db: ODatabaseSession
    private lateinit var orient: OrientDB

    var paginationOffset: Int = -1

    /** Items per page limit for pagination, like, posts per page. */
    var paginationLimit: Int = -1


    fun startup() {
        startupIfEnginesAreMissing()

        // Disable OrientDB's script manager to avoid JSR223 dependencies
        System.setProperty("orientdb.script.pool.enabled", "false")

        // For PLOCAL, the name is the database path/name. For MEMORY databases, no path needed
        val dbUri =
            if (type.equals(ODatabaseType.PLOCAL.name, ignoreCase = true))
                "$type:$name"
            else "$type:"

        orient = OrientDB(dbUri, OrientDBConfig.defaultConfig())

        // Set up database: create with proper admin user if it doesn't exist, or just open if it does
        setupOrOpenDatabase()
    }

    /**
     * Sets up the OrientDB database, creating it with proper admin credentials if needed.
     * This ensures the database is always created with admin/admin credentials.
     */
    private fun setupOrOpenDatabase() {
        val adminUser = "admin"
        val adminPass = "admin"
        val dbType = ODatabaseType.valueOf(type.uppercase(Locale.getDefault()))

        try {
            // Try to open existing database
            db = orient.open(name, adminUser, adminPass)
            log.debug("Opened existing database: {}", name)
        }
        catch (e: Exception) {
            // Database doesn't exist or credentials don't work - recreate it properly
            log.info("Database '{}' not accessible with admin/admin, creating fresh database", name)

            try {
                // Drop existing database if it exists but is inaccessible
                if (orient.exists(name)) {
                    log.warn("Dropping existing database '{}' due to authentication failure", name)
                    orient.drop(name)
                }
            } catch (dropEx: Exception) {
                log.warn("Failed to drop database: {}", dropEx.message)
            }

            try {
                // Create database with explicit admin user using SQL command
                // This ensures the database is created with known admin/admin credentials
                orient.execute("CREATE DATABASE $name ${dbType.name} USERS ($adminUser IDENTIFIED BY '$adminPass' ROLE admin)")
                log.info("Created database '{}' with admin user", name)
            } catch (createEx: Exception) {
                // If SQL create fails, try the API method
                log.warn("SQL CREATE DATABASE failed, trying API method: {}", createEx.message)
                orient.create(name, dbType)
            }

            // Open the newly created database
            db = orient.open(name, adminUser, adminPass)
        }
        activateOnCurrentThread()
        updateSchema()
    }

    fun resetPagination() {
        this.paginationOffset = -1
        this.paginationLimit = -1
    }

    fun updateSchema() {
        val schema = db.metadata.schema

        if (!schema.existsClass(Schema.DOCUMENTS))
            createDocType(schema)
        if (!schema.existsClass(Schema.SIGNATURES))
            createSignatureType(schema)
    }

    fun close() {
        if (::db.isInitialized) {
            activateOnCurrentThread()
            db.close()
        }

        if (::orient.isInitialized) {
            orient.close()
        }
        DBUtil.closeDataStore()
    }

    fun shutdown() {
        //        Orient.instance().shutdown();
    }

    private fun startupIfEnginesAreMissing() {
        // Using a jdk which doesn't bundle a javascript engine
        // throws a NoClassDefFoundError while logging the warning
        // see https://github.com/orientechnologies/orientdb/issues/5855
        OLogManager.instance().isWarnEnabled = false

        // If an instance of Orient was previously shutdown all engines are removed.
        // We need to startup Orient again.
        if (Orient.instance().engines.isEmpty())
            Orient.instance().startup()
        OLogManager.instance().isWarnEnabled = true
    }

    fun drop() {
        activateOnCurrentThread()
        orient.drop(name)
    }

    private fun activateOnCurrentThread() {
        if (::db.isInitialized) {
            db.activateOnCurrentThread()
        } else {
            println("db is null on activate")
        }
    }

    fun getDocumentCount(docType: String): Long {
        activateOnCurrentThread()
        val statement = String.format(STATEMENT_GET_DOCUMENT_COUNT_BY_TYPE, docType)
        return query(statement)[0]["count"] as Long
    }

    fun getPublishedCount(docType: String): Long {
        val statement = String.format(STATEMENT_GET_PUBLISHED_COUNT, docType)
        return query(statement)[0].get("count") as Long
    }

    fun getDocumentByUri(uri: String?): DocumentList<DocumentModel> {
        return query("SELECT * FROM Documents WHERE sourceuri=?", uri)
    }

    fun getDocumentStatus(uri: String?): DocumentList<DocumentModel> {
        return query(STATEMENT_GET_DOCUMENT_STATUS_BY_DOCTYPE_AND_URI, uri)
    }

    val publishedPosts: DocumentList<DocumentModel>
        get() = getPublishedContent("post")

    fun getPublishedPosts(applyPaging: Boolean): DocumentList<DocumentModel> {
        return getPublishedContent("post", applyPaging)
    }

    fun getPublishedPostsByTag(tag: String?): DocumentList<DocumentModel> {
        return query(STATEMENT_GET_PUBLISHED_POSTS_BY_TAG, tag)
    }

    fun getPublishedDocumentsByTag(tag: String?): DocumentList<DocumentModel> {
        val documents = DocumentList<DocumentModel>()

        for (docType in DocumentTypes.documentTypes) {
            val statement: String = String.format(STATEMENT_GET_PUBLISHED_POST_BY_TYPE_AND_TAG, docType)
            val documentsByTag = query(statement, tag)
            documents.addAll(documentsByTag)
        }
        return documents
    }

    val publishedPages: DocumentList<DocumentModel>
        get() = getPublishedContent("page")

    fun getPublishedContent(docType: String): DocumentList<DocumentModel> {
        return getPublishedContent(docType, false)
    }

    private fun getPublishedContent(docType: String?, applyPaging: Boolean): DocumentList<DocumentModel> {
        var query = String.format(STATEMENT_GET_PUBLISHED_CONTENT_BY_DOCTYPE, docType)
        if (applyPaging && hasStartAndLimitBoundary())
            query += " SKIP $paginationOffset LIMIT $paginationLimit"
        return query(query)
    }

    fun getAllContent(docType: String): DocumentList<DocumentModel> {
        return getAllContent(docType, false)
    }

    fun getAllContent(docType: String?, applyPaging: Boolean): DocumentList<DocumentModel> {
        var query = String.format(STATEMENT_GET_ALL_CONTENT_BY_DOCTYPE, docType)
        if (applyPaging && hasStartAndLimitBoundary())
            query += " SKIP $paginationOffset LIMIT $paginationLimit"
        return query(query)
    }

    private fun hasStartAndLimitBoundary(): Boolean {
        return (paginationOffset >= 0) && (paginationLimit > -1)
    }

    private val allTagsFromPublishedPosts: DocumentList<DocumentModel>
        get() = query(STATEMENT_GET_TAGS_FROM_PUBLISHED_POSTS)

    private val signaturesForTemplates: DocumentList<DocumentModel>
        get() = query(STATEMENT_GET_SIGNATURE_FOR_TEMPLATES)

    val unrenderedContent: DocumentList<DocumentModel>
        get() = query(STATEMENT_GET_UNDRENDERED_CONTENT)

    fun deleteContent(uri: String) {
        executeCommand(STATEMENT_DELETE_DOCTYPE_BY_SOURCEURI, uri)
    }

    fun markContentAsRendered(document: DocumentModel) {
        val statement: String =
            String.format(STATEMENT_MARK_CONTENT_AS_RENDERD, document.type, document.sourceUri)
        executeCommand(statement)
    }

    private fun updateSignatures(currentTemplatesSignature: String) {
        executeCommand(STATEMENT_UPDATE_TEMPLATE_SIGNATURE, currentTemplatesSignature)
    }

    fun deleteAllByDocType(docType: String) {
        val statement = String.format(STATEMENT_DELETE_ALL, docType)
        executeCommand(statement)
    }

    private fun insertTemplatesSignature(currentTemplatesSignature: String) {
        executeCommand(STATEMENT_INSERT_TEMPLATES_SIGNATURE, currentTemplatesSignature)
    }

    private fun query(sql: String): DocumentList<DocumentModel> {
        activateOnCurrentThread()
        val results = db.query(sql)
        return DocumentList.wrap(results)
    }

    private fun query(sql: String?, vararg args: Any?): DocumentList<DocumentModel> {
        activateOnCurrentThread()
        val results = db.command(sql, *args)
        return DocumentList.wrap(results)
    }

    private fun executeCommand(query: String?, vararg args: Any?) {
        activateOnCurrentThread()
        db.command(query, *args)
    }

    val tags: MutableSet<String>
        get() {
            val docs = this.allTagsFromPublishedPosts
            val result: MutableSet<String> = HashSet<String>()
            for (document in docs) {
                val tags = document.tags
                Collections.addAll(result, *tags)
            }
            return result
        }

    val allTags: MutableSet<String>
        get() {
            val result: MutableSet<String> = HashSet<String>()
            for (docType in DocumentTypes.documentTypes) {
                val statement: String =
                    String.format(STATEMENT_GET_TAGS_BY_DOCTYPE, docType)
                val docs = query(statement)
                for (document in docs) {
                    val tags = document.tags
                    Collections.addAll(result, *tags)
                }
            }
            return result
        }

    private fun createDocType(schema: OSchema) {
        log.debug("Create document class")

        val page = schema.createClass(Schema.DOCUMENTS)
        page.createProperty(ModelAttributes.SHA1, OType.STRING).isNotNull = true
        page.createIndex(Schema.DOCUMENTS + "sha1Index", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.SHA1)
        page.createProperty(ModelAttributes.SOURCE_URI, OType.STRING).isNotNull = true
        page.createIndex(Schema.DOCUMENTS + "sourceUriIndex", OClass.INDEX_TYPE.UNIQUE, ModelAttributes.SOURCE_URI)
        page.createProperty(ModelAttributes.CACHED, OType.BOOLEAN).isNotNull = true
        page.createIndex(Schema.DOCUMENTS + "cachedIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.CACHED)
        page.createProperty(ModelAttributes.RENDERED, OType.BOOLEAN).isNotNull = true
        page.createIndex(Schema.DOCUMENTS + "renderedIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.RENDERED)
        page.createProperty(ModelAttributes.STATUS, OType.STRING).isNotNull = true
        page.createIndex(Schema.DOCUMENTS + "statusIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.STATUS)
        page.createProperty(ModelAttributes.TYPE, OType.STRING).isNotNull = true
        page.createIndex(Schema.DOCUMENTS + "typeIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.TYPE)
    }

    private fun createSignatureType(schema: OSchema) {
        val signatures = schema.createClass(Schema.SIGNATURES)
        signatures.createProperty(ModelAttributes.SHA1, OType.STRING).isNotNull = true
        signatures.createIndex("sha1Idx", OClass.INDEX_TYPE.UNIQUE, ModelAttributes.SHA1)
    }

    fun updateAndClearCacheIfNeeded(needed: Boolean, templateFolder: File) {
        var clearCache = needed

        if (!needed)
            clearCache = updateTemplateSignatureIfChanged(templateFolder)

        if (clearCache) {
            deleteAllDocumentTypes()
            this.updateSchema()
        }
    }

    private fun updateTemplateSignatureIfChanged(templateFolder: File): Boolean {
        var templateSignatureChanged = false

        val docs = this.signaturesForTemplates
        var currentTemplatesSignature = try { FileUtil.sha1(templateFolder) }
            catch (e: Exception) { "" }

        if (!docs.isEmpty()) {
            val sha1 = docs[0].sha1
            if (sha1 != currentTemplatesSignature) {
                this.updateSignatures(currentTemplatesSignature)
                templateSignatureChanged = true
            }
        } else {
            // first computation of templates signature
            this.insertTemplatesSignature(currentTemplatesSignature)
            templateSignatureChanged = true
        }
        return templateSignatureChanged
    }

    private fun deleteAllDocumentTypes() {
        for (docType in DocumentTypes.documentTypes) {
            try {
                this.deleteAllByDocType(docType)
            } catch (e: Exception) {
                // maybe a non existing document type
            }
        }
    }

    val isActive: Boolean
        get() = ::db.isInitialized && db.isActiveOnCurrentThread

    fun addDocument(document: DocumentModel) {
        val element = db.newElement(Schema.DOCUMENTS)
        document.forEach { (k: String?, v: Any?) -> element.setProperty(k, v, OType.ANY) }
        @Suppress("DEPRECATION")
        element.save<ORecord?>()
    }

    private object Schema {
        const val DOCUMENTS: String = "Documents"
        const val SIGNATURES: String = "Signatures"
    }

    companion object {
        private const val STATEMENT_GET_PUBLISHED_POST_BY_TYPE_AND_TAG = "SELECT * FROM Documents WHERE status='published' AND type='%s' AND ? IN tags ORDER BY date DESC"
        private const val STATEMENT_GET_DOCUMENT_STATUS_BY_DOCTYPE_AND_URI = "SELECT sha1,rendered FROM Documents WHERE sourceuri=?"
        private const val STATEMENT_GET_PUBLISHED_COUNT = "SELECT count(*) AS count FROM Documents WHERE status='published' AND type='%s'"
        private const val STATEMENT_MARK_CONTENT_AS_RENDERD = "UPDATE Documents SET rendered=true WHERE rendered=false AND type='%s' AND sourceuri='%s' AND cached=true"
        private const val STATEMENT_DELETE_DOCTYPE_BY_SOURCEURI = "DELETE FROM Documents WHERE sourceuri=?"
        private const val STATEMENT_GET_UNDRENDERED_CONTENT = "SELECT * FROM Documents WHERE rendered=false ORDER BY date DESC"
        private const val STATEMENT_GET_SIGNATURE_FOR_TEMPLATES = "SELECT sha1 FROM Signatures WHERE key='templates'"
        private const val STATEMENT_GET_TAGS_FROM_PUBLISHED_POSTS = "SELECT tags FROM Documents WHERE status='published' AND type='post'"
        private const val STATEMENT_GET_ALL_CONTENT_BY_DOCTYPE = "SELECT * FROM Documents WHERE type='%s' ORDER BY date DESC"
        private const val STATEMENT_GET_PUBLISHED_CONTENT_BY_DOCTYPE = "SELECT * FROM Documents WHERE status='published' AND type='%s' ORDER BY date DESC"
        private const val STATEMENT_GET_PUBLISHED_POSTS_BY_TAG = "SELECT * FROM Documents WHERE status='published' AND type='post' AND ? IN tags ORDER BY date DESC"
        private const val STATEMENT_GET_TAGS_BY_DOCTYPE = "SELECT tags FROM Documents WHERE status='published' AND type='%s'"
        private const val STATEMENT_INSERT_TEMPLATES_SIGNATURE = "INSERT INTO Signatures(key,sha1) VALUES('templates',?)"
        private const val STATEMENT_DELETE_ALL = "DELETE FROM Documents WHERE type='%s'"
        private const val STATEMENT_UPDATE_TEMPLATE_SIGNATURE = "UPDATE Signatures SET sha1=? WHERE key='templates'"
        private const val STATEMENT_GET_DOCUMENT_COUNT_BY_TYPE = "SELECT count(*) AS count FROM Documents WHERE type='%s'"
    }

    private val log: Logger = LoggerFactory.getLogger(ContentStore::class.java)
}
