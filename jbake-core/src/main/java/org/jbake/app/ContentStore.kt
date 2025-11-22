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

/**
 * @author jdlee
 */
class ContentStore(private val type: String, private val name: String?) {
    private val logger: Logger = LoggerFactory.getLogger(ContentStore::class.java)

    private var db: ODatabaseSession? = null

    var start: Long = -1
        private set
    var limit: Long = -1
        private set
    private var orient: OrientDB? = null


    fun startup() {
        startupIfEnginesAreMissing()

        if (type.equals(ODatabaseType.PLOCAL.name, ignoreCase = true)) {
            orient = OrientDB(type + ":" + name, OrientDBConfig.defaultConfig())
        } else {
            orient = OrientDB(type + ":", OrientDBConfig.defaultConfig())
        }

        orient!!.createIfNotExists(name, ODatabaseType.valueOf(type.uppercase(Locale.getDefault())))

        db = orient!!.open(name, "admin", "admin")

        activateOnCurrentThread()

        updateSchema()
    }

    fun setStart(start: Int) {
        this.start = start.toLong()
    }

    fun setLimit(limit: Int) {
        this.limit = limit.toLong()
    }

    fun resetPagination() {
        this.start = -1
        this.limit = -1
    }

    fun updateSchema() {
        val schema = db!!.getMetadata().getSchema()

        if (!schema.existsClass(Schema.DOCUMENTS)) {
            createDocType(schema)
        }
        if (!schema.existsClass(Schema.SIGNATURES)) {
            createSignatureType(schema)
        }
    }

    fun close() {
        if (db != null) {
            activateOnCurrentThread()
            db!!.close()
        }

        if (orient != null) {
            orient!!.close()
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
        OLogManager.instance().setWarnEnabled(false)

        // If an instance of Orient was previously shutdown all engines are removed.
        // We need to startup Orient again.
        if (Orient.instance().getEngines().isEmpty()) {
            Orient.instance().startup()
        }
        OLogManager.instance().setWarnEnabled(true)
    }

    fun drop() {
        activateOnCurrentThread()

        //        db.drop();
        orient!!.drop(name)
    }

    private fun activateOnCurrentThread() {
        if (db != null) {
            db!!.activateOnCurrentThread()
        } else {
            println("db is null on activate")
        }
    }

    fun getDocumentCount(docType: String): Long {
        activateOnCurrentThread()
        val statement = String.format(STATEMENT_GET_DOCUMENT_COUNT_BY_TYPE, docType)
        return query(statement).get(0).get("count") as Long
    }

    fun getPublishedCount(docType: String): Long {
        val statement = String.format(STATEMENT_GET_PUBLISHED_COUNT, docType)
        return query(statement).get(0).get("count") as Long
    }

    fun getDocumentByUri(uri: String?): DocumentList<DocumentModel> {
        return query("select * from Documents where sourceuri=?", uri)
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
        if (applyPaging && hasStartAndLimitBoundary()) {
            query += " SKIP $start LIMIT $limit"
        }
        return query(query)
    }

    fun getAllContent(docType: String): DocumentList<DocumentModel> {
        return getAllContent(docType, false)
    }

    fun getAllContent(docType: String?, applyPaging: Boolean): DocumentList<DocumentModel> {
        var query = String.format(STATEMENT_GET_ALL_CONTENT_BY_DOCTYPE, docType)
        if (applyPaging && hasStartAndLimitBoundary()) {
            query += " SKIP " + start + " LIMIT " + limit
        }
        return query(query)
    }

    private fun hasStartAndLimitBoundary(): Boolean {
        return (start >= 0) && (limit > -1)
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
        val results = db!!.query(sql)
        return DocumentList.wrap(results)
    }

    private fun query(sql: String?, vararg args: Any?): DocumentList<DocumentModel> {
        activateOnCurrentThread()
        val results = db!!.command(sql, *args)
        return DocumentList.wrap(results)
    }

    private fun executeCommand(query: String?, vararg args: Any?) {
        activateOnCurrentThread()
        db!!.command(query, *args)
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
        logger.debug("Create document class")

        val page = schema.createClass(Schema.DOCUMENTS)
        page.createProperty(ModelAttributes.SHA1, OType.STRING).setNotNull(true)
        page.createIndex(Schema.DOCUMENTS + "sha1Index", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.SHA1)
        page.createProperty(ModelAttributes.SOURCE_URI, OType.STRING).setNotNull(true)
        page.createIndex(Schema.DOCUMENTS + "sourceUriIndex", OClass.INDEX_TYPE.UNIQUE, ModelAttributes.SOURCE_URI)
        page.createProperty(ModelAttributes.CACHED, OType.BOOLEAN).setNotNull(true)
        page.createIndex(Schema.DOCUMENTS + "cachedIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.CACHED)
        page.createProperty(ModelAttributes.RENDERED, OType.BOOLEAN).setNotNull(true)
        page.createIndex(Schema.DOCUMENTS + "renderedIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.RENDERED)
        page.createProperty(ModelAttributes.STATUS, OType.STRING).setNotNull(true)
        page.createIndex(Schema.DOCUMENTS + "statusIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.STATUS)
        page.createProperty(ModelAttributes.TYPE, OType.STRING).setNotNull(true)
        page.createIndex(Schema.DOCUMENTS + "typeIndex", OClass.INDEX_TYPE.NOTUNIQUE, ModelAttributes.TYPE)
    }

    private fun createSignatureType(schema: OSchema) {
        val signatures = schema.createClass(Schema.SIGNATURES)
        signatures.createProperty(ModelAttributes.SHA1, OType.STRING).setNotNull(true)
        signatures.createIndex("sha1Idx", OClass.INDEX_TYPE.UNIQUE, ModelAttributes.SHA1)
    }

    fun updateAndClearCacheIfNeeded(needed: Boolean, templateFolder: File) {
        var clearCache = needed

        if (!needed) {
            clearCache = updateTemplateSignatureIfChanged(templateFolder)
        }

        if (clearCache) {
            deleteAllDocumentTypes()
            this.updateSchema()
        }
    }

    private fun updateTemplateSignatureIfChanged(templateFolder: File): Boolean {
        var templateSignatureChanged = false

        val docs = this.signaturesForTemplates
        var currentTemplatesSignature: String?
        try {
            currentTemplatesSignature = FileUtil.sha1(templateFolder)
        } catch (e: Exception) {
            currentTemplatesSignature = ""
        }
        if (!docs.isEmpty()) {
            val sha1 = docs.get(0).sha1
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
        get() = db!!.isActiveOnCurrentThread()

    fun addDocument(document: DocumentModel) {
        val element = db!!.newElement(Schema.DOCUMENTS)
        document.forEach { (k: String?, v: Any?) -> element.setProperty(k, v, OType.ANY) }
        element.save<ORecord?>()
    }

    protected object Schema {
        const val DOCUMENTS: String = "Documents"
        const val SIGNATURES: String = "Signatures"
    }

    companion object {
        private const val STATEMENT_GET_PUBLISHED_POST_BY_TYPE_AND_TAG =
            "select * from Documents where status='published' and type='%s' and ? in tags order by date desc"
        private const val STATEMENT_GET_DOCUMENT_STATUS_BY_DOCTYPE_AND_URI =
            "select sha1,rendered from Documents where sourceuri=?"
        private const val STATEMENT_GET_PUBLISHED_COUNT =
            "select count(*) as count from Documents where status='published' and type='%s'"
        private const val STATEMENT_MARK_CONTENT_AS_RENDERD =
            "update Documents set rendered=true where rendered=false and type='%s' and sourceuri='%s' and cached=true"
        private const val STATEMENT_DELETE_DOCTYPE_BY_SOURCEURI = "delete from Documents where sourceuri=?"
        private const val STATEMENT_GET_UNDRENDERED_CONTENT =
            "select * from Documents where rendered=false order by date desc"
        private const val STATEMENT_GET_SIGNATURE_FOR_TEMPLATES = "select sha1 from Signatures where key='templates'"
        private const val STATEMENT_GET_TAGS_FROM_PUBLISHED_POSTS =
            "select tags from Documents where status='published' and type='post'"
        private const val STATEMENT_GET_ALL_CONTENT_BY_DOCTYPE =
            "select * from Documents where type='%s' order by date desc"
        private const val STATEMENT_GET_PUBLISHED_CONTENT_BY_DOCTYPE =
            "select * from Documents where status='published' and type='%s' order by date desc"
        private const val STATEMENT_GET_PUBLISHED_POSTS_BY_TAG =
            "select * from Documents where status='published' and type='post' and ? in tags order by date desc"
        private const val STATEMENT_GET_TAGS_BY_DOCTYPE =
            "select tags from Documents where status='published' and type='%s'"
        private const val STATEMENT_INSERT_TEMPLATES_SIGNATURE =
            "insert into Signatures(key,sha1) values('templates',?)"
        private const val STATEMENT_DELETE_ALL = "delete from Documents where type='%s'"
        private const val STATEMENT_UPDATE_TEMPLATE_SIGNATURE = "update Signatures set sha1=? where key='templates'"
        private const val STATEMENT_GET_DOCUMENT_COUNT_BY_TYPE =
            "select count(*) as count from Documents where type='%s'"
    }
}
