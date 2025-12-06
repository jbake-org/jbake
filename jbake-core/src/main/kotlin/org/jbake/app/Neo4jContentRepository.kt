package org.jbake.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypeRegistry
import org.jbake.model.ModelAttributes
import org.jbake.util.Logging.logger
import org.neo4j.dbms.api.DatabaseManagementService
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
import org.neo4j.graphdb.GraphDatabaseService
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path

/**
 * Neo4j embedded database implementation of ContentRepository.
 * Uses Neo4j 5.x embedded database for content storage.
 */
class Neo4jContentRepository(private val type: String, private val name: String) : ContentRepository {

    private lateinit var managementService: DatabaseManagementService
    private lateinit var database: GraphDatabaseService

    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    override var paginationOffset: Int = -1
    override var paginationLimit: Int = -1

    override fun startup() {
        val dbPath = when (type.lowercase()) {
            "memory" -> Path.of(System.getProperty("java.io.tmpdir"), "neo4j-$name-${System.currentTimeMillis()}")
            "plocal", "local" -> Path.of(name)
            else -> throw IllegalArgumentException("Unknown database type: $type")
        }

        managementService = DatabaseManagementServiceBuilder(dbPath).build()
        database = managementService.database("neo4j")
        Runtime.getRuntime().addShutdownHook(Thread { managementService.shutdown() })
        updateSchema()
    }

    override fun close() {
        runCatching { managementService.shutdown() }
        DbUtils.closeDataStore()
    }

    override fun shutdown() {}

    override fun drop() = database.beginTx().use { tx ->
        tx.execute("MATCH (n:Document) DETACH DELETE n").close()
        tx.execute("MATCH (n:Signature) DETACH DELETE n").close()
        tx.commit()
    }

    override fun updateSchema() {
        database.beginTx().use { tx ->
            tx.execute("CREATE CONSTRAINT document_sourceuri IF NOT EXISTS FOR (d:Document) REQUIRE d.sourceuri IS UNIQUE").close()
            tx.execute("CREATE INDEX document_type IF NOT EXISTS FOR (d:Document) ON (d.type)").close()
            tx.execute("CREATE INDEX document_status IF NOT EXISTS FOR (d:Document) ON (d.status)").close()
            tx.commit()
        }
    }

    override val isActive: Boolean
        get() = database.isAvailable(1000)

    override fun addDocument(document: DocumentModel) {
        val propertiesJson = objectMapper.writeValueAsString(document)

        database.beginTx().use { tx ->
            tx.execute($$"MATCH (d:Document {sourceuri: $sourceuri}) DETACH DELETE d", mapOf("sourceuri" to document.sourceUri)).close()
            tx.execute($$"""CREATE (d:Document {
                        sourceuri: $sourceuri,
                        type: $type,
                        status: $status,
                        sha1: $sha1,
                        cached: $cached,
                        rendered: $rendered,
                        title: $title,
                        date: $date,
                        tags: $tags,
                        body: $body,
                        properties: $properties
                    })""",
                mapOf(
                    "sourceuri" to document.sourceUri,
                    "type" to document.type,
                    "status" to document.status,
                    "sha1" to document.sha1,
                    "cached" to (document.cached ?: false),
                    "rendered" to document.rendered,
                    "title" to document.title,
                    "date" to document.date?.toInstant()?.toEpochMilli(),
                    "tags" to document.tags.toTypedArray(),
                    "body" to document.getOrDefault(ModelAttributes.DOC_BODY_RENDERED, ""),
                    "properties" to propertiesJson
                )).close()

            tx.commit()
        }
    }

    override fun getDocumentByUri(uri: String?): DocumentList<DocumentModel> =
        query($$"MATCH (d:Document {sourceuri: $sourceuri}) RETURN d", mapOf("sourceuri" to uri))

    override fun getDocumentStatus(uri: String?): DocumentList<DocumentModel> =
        query($$"MATCH (d:Document {sourceuri: $sourceuri}) RETURN d.sha1 as sha1, d.rendered as rendered", mapOf("sourceuri" to uri))

    override fun getDocumentCount(docType: String): Long = database.beginTx().use { tx ->
        val result = tx.execute($$"MATCH (d:Document {type: $type}) RETURN count(d) as count", mapOf("type" to docType))
        val count = if (result.hasNext()) result.next()["count"] as Long else 0L
        tx.commit()
        count
    }

    override fun getPublishedCount(docType: String): Long = database.beginTx().use { tx ->
        val result = tx.execute("MATCH (d:Document {type: \$type, status: 'published'}) RETURN count(d) as count", mapOf("type" to docType))
        val count = if (result.hasNext()) result.next()["count"] as Long else 0L
        tx.commit()
        count
    }

    override val publishedPosts: DocumentList<DocumentModel>
        get() = getPublishedContent("post")

    override fun getPublishedPosts(applyPaging: Boolean) =
        getPublishedContent("post", applyPaging)

    override fun getPublishedPostsByTag(tag: String?): DocumentList<DocumentModel> =
        query("MATCH (d:Document {type: 'post', status: 'published'}) WHERE \$tag IN d.tags RETURN d ORDER BY d.date DESC", mapOf("tag" to tag))

    override fun getPublishedDocumentsByTag(tag: String?): DocumentList<DocumentModel> {
        val cypher = "MATCH (d:Document {type: \$type, status: 'published'}) WHERE \$tag IN d.tags RETURN d ORDER BY d.date DESC"
        val documents = DocumentList<DocumentModel>()
        for (docType in DocumentTypeRegistry.documentTypes) {
            documents.addAll(query(cypher, mapOf("type" to docType, "tag" to tag)))
        }
        return documents
    }

    override val publishedPages: DocumentList<DocumentModel>
        get() = getPublishedContent("page")

    override fun getPublishedContent(docType: String) =
        getPublishedContent(docType, false)

    override fun getPublishedContent(docType: String, applyPaging: Boolean): DocumentList<DocumentModel> {
        var cypher = "MATCH (d:Document {type: \$type, status: 'published'}) RETURN d ORDER BY d.date DESC"
        if (applyPaging && hasStartAndLimitBoundary()) {
            cypher += " SKIP $paginationOffset LIMIT $paginationLimit"
        }
        return query(cypher, mapOf("type" to docType))
    }

    override fun getAllContent(docType: String) =
        getAllContent(docType, false)

    override fun getAllContent(docType: String, applyPaging: Boolean): DocumentList<DocumentModel> {
        var cypher = "MATCH (d:Document {type: \$type}) RETURN d ORDER BY d.date DESC"
        if (applyPaging && hasStartAndLimitBoundary()) {
            cypher += " SKIP $paginationOffset LIMIT $paginationLimit"
        }
        return query(cypher, mapOf("type" to docType))
    }

    override val unrenderedContent: DocumentList<DocumentModel>
        get() = query("MATCH (d:Document {rendered: false}) RETURN d ORDER BY d.date DESC")

    override fun deleteContent(uri: String) = database.beginTx().use { tx ->
        tx.execute("MATCH (d:Document {sourceuri: \$sourceuri}) DETACH DELETE d", mapOf("sourceuri" to uri)).close()
        tx.commit()
    }

    override fun markContentAsRendered(document: DocumentModel) = database.beginTx().use { tx ->
        tx.execute(
            "MATCH (d:Document {type: \$type, sourceuri: \$sourceuri, cached: true, rendered: false}) SET d.rendered = true",
            mapOf("type" to document.type, "sourceuri" to document.sourceUri)
        ).close()
        tx.commit()
    }

    override fun deleteAllByDocType(docType: String) = database.beginTx().use { tx ->
        tx.execute("MATCH (d:Document {type: \$type}) DETACH DELETE d", mapOf("type" to docType)).close()
        tx.commit()
    }

    override val tags: MutableSet<String>
        get() {
            val result = mutableSetOf<String>()
            database.beginTx().use { tx ->
                val queryResult = tx.execute("MATCH (d:Document {type: 'post', status: 'published'}) RETURN d.tags as tags")
                while (queryResult.hasNext()) {
                    val tags = queryResult.next()["tags"] as? Array<*>
                    tags?.filterIsInstance<String>()?.forEach { result.add(it) }
                }
                tx.commit()
            }
            return result
        }

    override val allTags: MutableSet<String>
        get() {
            val allTags = mutableSetOf<String>()
            for (docType in DocumentTypeRegistry.documentTypes) {
                database.beginTx().use { tx ->
                    val rows = tx.execute($$"MATCH (d:Document {type: $type, status: 'published'}) RETURN d.tags as tags", mapOf("type" to docType))
                    while (rows.hasNext()) {
                        val tags = rows.next()["tags"] as? Array<*>
                        tags?.filterIsInstance<String>()?.forEach { allTags.add(it) }
                    }
                    tx.commit() // TODO: Why commit here? It's a read operation.
                }
            }
            return allTags
        }

    override fun updateAndClearCacheIfNeeded(needed: Boolean, templateDir: File) {
        var clearCache = needed
        if (!needed) {
            clearCache = updateTemplateSignatureIfChanged(templateDir)
        }
        if (clearCache) {
            deleteAllDocumentTypes()
            updateSchema()
        }
    }

    override fun resetPagination() {
        paginationOffset = -1
        paginationLimit = -1
    }

    private fun hasStartAndLimitBoundary()
        = (paginationOffset >= 0) && (paginationLimit > -1)

    private fun query(cypher: String, params: Map<String, Any?> = emptyMap()): DocumentList<DocumentModel> {
        val documents = DocumentList<DocumentModel>()

        database.beginTx().use { tx ->
            val result = tx.execute(cypher, params)
            while (result.hasNext()) {
                val record = result.next()
                val document = DocumentModel()

                if (record.containsKey("d")) {
                    val node = record["d"] as org.neo4j.graphdb.Node

                    // First, load extra properties from JSON (for fields not stored as node properties)
                    val propertiesJson = node.getProperty("properties", null) as? String
                    if (propertiesJson != null) {
                        val map = objectMapper.readValue<Map<String, Any?>>(propertiesJson, objectMapper.typeFactory.constructMapType(Map::class.java, String::class.java, Any::class.java))
                        map.forEach { (k, v) -> if (v != null) document[k] = v }
                    }

                    // Then, override with actual node property values (which may have been updated)
                    node.propertyKeys.forEach { key: String ->
                        if (key == "properties") return@forEach
                        val value = node.getProperty(key)
                        when (key) {
                            "date" -> document[key] = if (value is Long) java.time.Instant.ofEpochMilli(value).atOffset(java.time.ZoneOffset.UTC) else value
                            "tags" -> document[key] = (value as? Array<*>)?.filterIsInstance<String>() ?: emptyList<String>()
                            else -> document[key] = value
                        }
                    }
                } else {
                    // Individual properties returned (e.g., for count queries)
                    for (key: String in record.keys) {
                        val value = record[key] ?: continue
                        when (key) {
                            "date" -> document[key] = if (value is Long) java.time.Instant.ofEpochMilli(value).atOffset(java.time.ZoneOffset.UTC) else value
                            "tags" -> document[key] = (value as? Array<*>)?.filterIsInstance<String>() ?: emptyList<String>()
                            else -> document[key] = value
                        }
                    }
                }

                documents.add(document)
            }
            tx.commit()
        }

        return documents
    }

    private fun updateTemplateSignatureIfChanged(templateDir: File): Boolean {
        val currentSignature = runCatching { FileUtil.sha1(templateDir) }.getOrElse { "" }

        return database.beginTx().use { tx ->
            val result = tx.execute("MATCH (s:Signature {key: 'templates'}) RETURN s.sha1 as sha1")
            val changed = if (result.hasNext()) {
                val existingSignature = result.next()["sha1"] as? String
                if (existingSignature == currentSignature) false
                else {
                    tx.execute("MATCH (s:Signature {key: 'templates'}) SET s.sha1 = \$sha1", mapOf("sha1" to currentSignature)).close()
                    true
                }
            } else {
                tx.execute("CREATE (s:Signature {key: 'templates', sha1: \$sha1})", mapOf("sha1" to currentSignature)).close()
                true
            }
            tx.commit()
            changed
        }
    }

    private fun deleteAllDocumentTypes() {
        DocumentTypeRegistry.documentTypes.forEach { docType -> runCatching { deleteAllByDocType(docType) } }
    }

    private val log: Logger by logger()
}

