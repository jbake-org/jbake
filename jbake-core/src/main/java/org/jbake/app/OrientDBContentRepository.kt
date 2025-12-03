package org.jbake.app

import com.orientechnologies.common.log.OLogManager
import com.orientechnologies.orient.core.Orient
import com.orientechnologies.orient.core.config.OGlobalConfiguration
import com.orientechnologies.orient.core.db.ODatabaseSession
import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.ODatabaseType.MEMORY
import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.OrientDBConfig
import com.orientechnologies.orient.core.metadata.schema.OClass
import com.orientechnologies.orient.core.metadata.schema.OSchema
import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.record.ORecord
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypeRegistry
import org.jbake.model.ModelAttributes
import org.jbake.util.Logging.logger
import org.slf4j.Logger
import java.io.File

class OrientDBContentRepository(type: String, private val name: String) : ContentRepository {

    private lateinit var db: ODatabaseSession
    private lateinit var orient: OrientDB

    override var paginationOffset: Int = -1

    /** Items per page limit for pagination, like, posts per page. */
    override var paginationLimit: Int = -1

    private val dbStorageType = ODatabaseType.valueOf(type.uppercase())

    override fun startup() {
        System.setProperty("orientdb.script.pool.enabled", "false")

        // OrientDB logging configuration - try ALL possible methods to enable DEBUG level
        val logLevel = "debug"
        setLoggingLevelViaSysProps(logLevel)

        startupIfEnginesAreMissing()

        setLoggingLevelViaApi(logLevel)

        val dbUri = dbStorageType.name + ":" + if (MEMORY == dbStorageType) "" else name
        orient = OrientDB(dbUri, OrientDBConfig.defaultConfig())

        setupOrOpenDatabase(DbAccessInfo(name, "admin", "admin"))

        setLoggingLevelViaApi(logLevel)
    }

    data class DbAccessInfo(val dbname: String, val user: String, val pass: String)


    /**
     * Set up database: create with proper admin user if it doesn't exist, or just open if it does.
     */
    private fun setupOrOpenDatabase(access: DbAccessInfo) {
        runCatching { orient.drop(access.dbname) }

        try {
            // Try to open existing database
            db = orient.open(access.dbname, access.user, access.pass)
            log.debug("Opened existing database: {}", access.dbname)
            setConfigLevelViaSql_ConfigSet("finest")
        }
        catch (e: Exception) {
            // Database doesn't exist or credentials don't work - recreate it properly
            log.info("Database '${access.dbname}' not accessible with admin/admin, creating fresh database", e)

            try {
                // Drop existing database if it exists but is inaccessible
                if (orient.exists(access.dbname)) {
                    log.warn("Dropping existing database '${access.dbname}' due to authentication failure")
                    orient.drop(access.dbname)
                }
            } catch (dropEx: Exception) {
                log.warn("Failed to drop database: ${dropEx.message}")
            }

            try {
                // Create database with explicit admin user using SQL command.
                // This ensures the database is created with known admin/admin credentials.
                val query = "CREATE DATABASE ${access.dbname} ${dbStorageType.name} USER ${access.user} IDENTIFIED BY '${access.pass}' ROLE admin"
                log.info("Query: $query")
                orient.execute(query)
                log.info("Created database '${access.dbname}' with user ${access.user}")
            }
            catch (e: Exception) {
                // If SQL create fails, try the API method
                log.warn("Query 'CREATE DATABASE ...' failed, trying API method: ${e.message}")
                orient.create(access.dbname, dbStorageType)
            }

            // Open the newly created database
            db = orient.open(access.dbname, access.user, access.pass)

            setConfigLevelViaSql_ConfigSet("finest")
        }
        activateOnCurrentThread()
        updateSchema()
    }

    private fun setLoggingLevelViaSysProps(level: String) {
        // System properties with orientdb. prefix
        System.setProperty("orientdb.log.console.level", level)
        System.setProperty("orientdb.log.file.level", level)

        // System properties without prefix (backup attempt)
        System.setProperty("log.console.level", level)
        System.setProperty("log.file.level", level)
    }

    private fun setLoggingLevelViaApi(level: String) {
        // OGlobalConfiguration - set to DEBUG
        OGlobalConfiguration.LOG_CONSOLE_LEVEL.setValue(level)
        OGlobalConfiguration.LOG_FILE_LEVEL.setValue(level)
        OGlobalConfiguration.SERVER_LOG_DUMP_CLIENT_EXCEPTION_LEVEL.setValue(level)
        OGlobalConfiguration.SERVER_LOG_DUMP_CLIENT_EXCEPTION_FULLSTACKTRACE.setValue(true)
    }


    /** Won't work, because CONFIG SET is only for the OrientDB console. */
    private fun setConfigLevelViaSql_ConfigSet(level: String) {
        try {
            orient.execute("CONFIG SET log.console.level='$level'")
            orient.execute("CONFIG SET log.file.level='$level'")
            log.info("OrientDB logging configured to level '$level' via SQL")
        }
        catch (e: Exception) {
            log.warn("Failed to configure OrientDB log level via SQL: ${e.message}")
        }
    }

    override fun resetPagination() {
        this.paginationOffset = -1
        this.paginationLimit = -1
    }

    override fun updateSchema() {
        val schema = db.metadata.schema

        if (!schema.existsClass(Schema.DOCUMENTS))
            createDocType(schema)
        if (!schema.existsClass(Schema.SIGNATURES))
            createSignatureType(schema)
    }

    override fun close() {
        if (::db.isInitialized) { activateOnCurrentThread(); db.close() }
        if (::orient.isInitialized) orient.close()
        DbUtils.closeDataStore()
    }

    override fun shutdown() {
        //        Orient.instance().shutdown();
    }

    private fun startupIfEnginesAreMissing() {
        // Using a jdk which doesn't bundle a javascript engine
        // throws a NoClassDefFoundError while logging the warning
        // see https://github.com/orientechnologies/orientdb/issues/5855
        OLogManager.instance().isWarnEnabled = false

        // Set OrientDB logging to FINEST level via OLogManager
        OLogManager.instance().setFileLevel("finest")
        OLogManager.instance().setConsoleLevel("finest")

        // If an instance of Orient was previously shutdown all engines are removed.
        // We need to startup Orient again.
        if (Orient.instance().engines.isEmpty())
            Orient.instance().startup()

        OLogManager.instance().isWarnEnabled = true

        // Re-apply logging levels after Orient startup
        OLogManager.instance().setFileLevel("finest")
        OLogManager.instance().setConsoleLevel("finest")
    }

    override fun drop() {
        activateOnCurrentThread()
        orient.drop(name)
    }

    private fun activateOnCurrentThread() {
        if (::db.isInitialized) {
            db.activateOnCurrentThread()
        } else {
            log.error("lateinit val 'db' is null on activateOnCurrentThread()")
        }
    }

    override fun getDocumentCount(docType: String): Long {
        activateOnCurrentThread()
        return query("SELECT count(*) AS count FROM Documents WHERE type=?", docType)[0]["count"] as Long
    }

    override fun getPublishedCount(docType: String): Long {
        return query("SELECT count(*) AS count FROM Documents WHERE status='published' AND type=?", docType)[0]["count"] as Long
    }

    override fun getDocumentByUri(uri: String?): DocumentList<DocumentModel> {
        return query("SELECT * FROM Documents WHERE sourceuri=?", uri)
    }

    override fun getDocumentStatus(uri: String?): DocumentList<DocumentModel> {
        return query("SELECT sha1,rendered FROM Documents WHERE sourceuri=?", uri)
    }

    override val publishedPosts: DocumentList<DocumentModel>
        get() = getPublishedContent("post")

    override fun getPublishedPosts(applyPaging: Boolean): DocumentList<DocumentModel> {
        return getPublishedContent("post", applyPaging)
    }

    override fun getPublishedPostsByTag(tag: String?): DocumentList<DocumentModel> {
        return query("SELECT * FROM Documents WHERE status='published' AND type='post' AND ? IN tags ORDER BY date DESC", tag)
    }

    override fun getPublishedDocumentsByTag(tag: String?): DocumentList<DocumentModel> {
        val documents = DocumentList<DocumentModel>()
        for (docType in DocumentTypeRegistry.documentTypes) {
            val documentsByTag = query("SELECT * FROM Documents WHERE status='published' AND type=? AND ? IN tags ORDER BY date DESC", docType, tag)
            documents.addAll(documentsByTag)
        }
        return documents
    }

    override val publishedPages: DocumentList<DocumentModel>
        get() = getPublishedContent("page")

    override fun getPublishedContent(docType: String): DocumentList<DocumentModel> {
        return getPublishedContent(docType, false)
    }

    override fun getPublishedContent(docType: String, applyPaging: Boolean): DocumentList<DocumentModel> {
        var sql = "SELECT * FROM Documents WHERE status='published' AND type=? ORDER BY date DESC "
        if (applyPaging && hasStartAndLimitBoundary())
            sql += " SKIP $paginationOffset LIMIT $paginationLimit"
        return query(sql, docType)
    }

    override fun getAllContent(docType: String): DocumentList<DocumentModel> {
        return getAllContent(docType, false)
    }

    override fun getAllContent(docType: String, applyPaging: Boolean): DocumentList<DocumentModel> {
        var sql = "SELECT * FROM Documents WHERE type=? ORDER BY date DESC"
        if (applyPaging && hasStartAndLimitBoundary())
            sql += " SKIP $paginationOffset LIMIT $paginationLimit"
        return query(sql, docType)
    }

    private fun hasStartAndLimitBoundary(): Boolean {
        return (paginationOffset >= 0) && (paginationLimit > -1)
    }

    private val allTagsFromPublishedPosts: DocumentList<DocumentModel>
        get() = query("SELECT tags FROM Documents WHERE status='published' AND type='post'")

    private val signaturesForTemplates: DocumentList<DocumentModel>
        get() = query("SELECT sha1 FROM Signatures WHERE key='templates'")

    override val unrenderedContent: DocumentList<DocumentModel>
        get() = query("SELECT * FROM Documents WHERE rendered=false ORDER BY date DESC")

    override fun deleteContent(uri: String) {
        executeCommand("DELETE FROM Documents WHERE sourceuri=?", uri)
    }

    override fun markContentAsRendered(document: DocumentModel) {
        executeCommand("UPDATE Documents SET rendered=true WHERE rendered=false AND type=? AND sourceuri=? AND cached=true", document.type, document.sourceUri)
    }

    private fun updateSignatures(currentTemplatesSignature: String) {
        executeCommand("UPDATE Signatures SET sha1=? WHERE key='templates'", currentTemplatesSignature)
    }

    override fun deleteAllByDocType(docType: String) {
        executeCommand("DELETE FROM Documents WHERE type=?", docType)
    }

    private fun insertTemplatesSignature(currentTemplatesSignature: String) {
        executeCommand("INSERT INTO Signatures(key,sha1) VALUES('templates',?)", currentTemplatesSignature)
    }

    private fun query(sql: String): DocumentList<DocumentModel> {
        activateOnCurrentThread()
        val results = db.query(sql)
        return DocumentList.wrapOrientDbResultToDocumentList(results)
    }

    private fun query(sql: String?, vararg args: Any?): DocumentList<DocumentModel> {
        activateOnCurrentThread()
        val results = db.command(sql, *args)
        return DocumentList.wrapOrientDbResultToDocumentList(results)
    }

    private fun executeCommand(query: String?, vararg args: Any?) {
        activateOnCurrentThread()
        db.command(query, *args)
    }

    override val tags: MutableSet<String>
        get() = allTagsFromPublishedPosts.flatMap { it.tags }.toMutableSet()

    override val allTags: MutableSet<String>
        get() = DocumentTypeRegistry.documentTypes
            .flatMap { docType -> query("SELECT tags FROM Documents WHERE status='published' AND type=?", docType) }
            .flatMap { it.tags }
            .toMutableSet()

    private fun createDocType(schema: OSchema) {
        log.debug("Creating database class ${Schema.DOCUMENTS}")

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
        log.debug("Creating database class ${Schema.SIGNATURES}")

        val signatures = schema.createClass(Schema.SIGNATURES)
        signatures.createProperty(ModelAttributes.SHA1, OType.STRING).isNotNull = true
        signatures.createIndex("sha1Idx", OClass.INDEX_TYPE.UNIQUE, ModelAttributes.SHA1)
    }

    override fun updateAndClearCacheIfNeeded(needed: Boolean, templateDir: File) {
        var clearCache = needed

        if (!needed)
            clearCache = updateTemplateSignatureIfChanged(templateDir)

        if (clearCache) {
            deleteAllDocumentTypes()
            this.updateSchema()
        }
    }

    private fun updateTemplateSignatureIfChanged(templateDir: File): Boolean {
        var templateSignatureChanged = false

        val docs = this.signaturesForTemplates
        val currentTemplatesSignature = try { FileUtil.sha1(templateDir) }
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
        for (docType in DocumentTypeRegistry.documentTypes) {
            try {
                this.deleteAllByDocType(docType)
            } catch (e: Exception) {
                log.warn("Failed to delete documents of type '$docType': ${e.message}")
            }
        }
    }

    override val isActive: Boolean
        get() = ::db.isInitialized && db.isActiveOnCurrentThread

    override fun addDocument(document: DocumentModel) {
        // Filter out Ruby objects that can't be stored
        val filteredDocument = document.filterNot { (key, value) -> rejectUnparsableTypes(key, value) }
        val element = db.newElement(Schema.DOCUMENTS)
        filteredDocument.forEach { (k: String?, v: Any?) -> element.setProperty(k, v, OType.ANY) }
        @Suppress("DEPRECATION")
        element.save<ORecord?>()
    }

    private object Schema {
        const val DOCUMENTS: String = "Documents"
        const val SIGNATURES: String = "Signatures"
    }

    private val log: Logger by logger()

    private fun rejectUnparsableTypes(key: String, value: Any?): Boolean = when (value) {
        is org.jruby.RubyObject -> true
        is org.jruby.RubySymbol -> true
        is org.jruby.RubyClass -> true
        is org.jruby.RubyModule -> true
        else -> false
    }
}
