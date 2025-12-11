package org.jbake.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypeRegistry
import org.jbake.model.ModelAttributes
import org.jbake.model.ModelAttributes.DOC_DATE
import org.jbake.util.Logging.logger
import org.jbake.util.ValueTracer
import org.jbake.util.debug
import org.slf4j.Logger
import java.io.File
import java.sql.*
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Date

/**
 * HSQLDB-based implementation of ContentRepository.
 * Uses in-memory or file-based HSQLDB database for content storage.
 */
class HsqldbContentRepository(private val type: String, private val name: String) : ContentRepository {

    private lateinit var connection: Connection

    override var paginationOffset: Int = -1
    override var paginationLimit: Int = -1


    override fun startup() {
        val jdbcUrl = when (type.lowercase()) {
            "memory" -> "jdbc:hsqldb:mem:$name;sql.syntax_mys=true"
            "plocal", "local" -> "jdbc:hsqldb:file:$name/cache;shutdown=true;sql.syntax_mys=true"
            else -> throw IllegalArgumentException("Unknown database type: $type")
        }

        // Always get connection - for in-memory DBs with same name, this returns connection to same DB
        connection = DriverManager.getConnection(jdbcUrl, "SA", "")
        connection.autoCommit = true
        updateSchema()
    }

    override fun close() {
        runCatching {
            if (!connection.isClosed) {
                connection.createStatement().execute("SHUTDOWN")
                connection.close()
            }
        }
        DbUtils.closeDataStore()
    }

    override fun shutdown() {
        // HSQLDB shutdown handled in close()
    }

    override fun drop() {
        connection.createStatement().use { stmt ->
            // Delete all data from tables instead of dropping schema
            // This is safer and faster for tests
            stmt.execute("""DELETE FROM "Documents"""")
            stmt.execute("""DELETE FROM "Signatures"""")
        }
    }

    override fun updateSchema() {
        connection.createStatement().use { stmt ->
            // Create Documents table - quote all identifiers to avoid HSQLDB normalization
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS "Documents" (
                    "sourceuri" VARCHAR(1024) PRIMARY KEY,
                    "type" VARCHAR(64) NOT NULL,
                    "status" VARCHAR(32),
                    "sha1" VARCHAR(256),
                    "cached" BOOLEAN,
                    "rendered" BOOLEAN,
                    "title" VARCHAR(512),
                    "date" TIMESTAMP,
                    "tags" VARCHAR(64) ARRAY,
                    "body" CLOB,
                    "properties" CLOB
                ) """)

            // Create indexes with IF NOT EXISTS - works with MySQL compatibility mode
            stmt.execute("""CREATE INDEX IF NOT EXISTS "idx_sha1" ON "Documents"("sha1")""")
            stmt.execute("""CREATE INDEX IF NOT EXISTS "idx_cached" ON "Documents"("cached")""")
            stmt.execute("""CREATE INDEX IF NOT EXISTS "idx_rendered" ON "Documents"("rendered")""")
            stmt.execute("""CREATE INDEX IF NOT EXISTS "idx_status" ON "Documents"("status")""")
            stmt.execute("""CREATE INDEX IF NOT EXISTS "idx_type" ON "Documents"("type")""")

            // Create Signatures table
            stmt.execute("""CREATE TABLE IF NOT EXISTS "Signatures" ("key" VARCHAR(255) PRIMARY KEY, "sha1" VARCHAR(40) NOT NULL)""")
        }
    }

    override val isActive: Boolean
        get() = !connection.isClosed

    override fun addDocument(document: DocumentModel) {
        // DELETE + INSERT for upsert - MERGE has type inference issues with ARRAY columns
        val deleteSql = """DELETE FROM "Documents" WHERE "sourceuri"=?"""
        connection.prepareStatement(deleteSql).use { stmt ->
            stmt.setString(1, document.sourceUri)
            stmt.executeUpdate()
        }

        val insertSql = """ INSERT INTO "Documents"
            ("sourceuri", "type", "status", "sha1", "cached", "rendered", "title", "date", "tags", "body", "properties")
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

        connection.prepareStatement(insertSql).use { stmt ->
            val tagsArray = connection.createArrayOf("VARCHAR", document.tags.toTypedArray())
            // Ruby objects already converted by AsciidoctorEngine
            val propertiesJson = objectMapper.writeValueAsString(document)

            stmt.setString(1, document.sourceUri)
            stmt.setString(2, document.type)
            stmt.setString(3, document.status)
            stmt.setString(4, document.sha1)
            stmt.setBoolean(5, document.cached ?: false)
            stmt.setBoolean(6, document.rendered)
            stmt.setString(7, document.title)
            stmt.setTimestamp(8, document.date?.let { it.let { Timestamp(it.toInstant().toEpochMilli()) } })
            stmt.setArray(9, tagsArray)
            stmt.setString(10, document.getOrDefault(ModelAttributes.DOC_BODY_RENDERED, "") as String)
            stmt.setString(11, propertiesJson)

            stmt.executeUpdate()
            ValueTracer.trace("hsqldb-insert", document, document.sourceUri)
        }
    }

    override fun getDocumentByUri(uri: String?): DocumentList<DocumentModel> {
        val sql = """SELECT * FROM "Documents" WHERE "sourceuri"=?"""
        return query(sql, uri)
    }

    override fun getDocumentStatus(uri: String?): DocumentList<DocumentModel> {
        val sql = """SELECT "sha1", "rendered" FROM "Documents" WHERE "sourceuri"=?"""
        return query(sql, uri)
    }

    override fun getDocumentCount(docType: String): Long {
        val sql = """SELECT count(*) AS "count" FROM "Documents" WHERE "type"=?"""
        return querySingleValue<Long>(sql, docType) ?: 0L
    }

    override fun getPublishedCount(docType: String): Long {
        val sql = """SELECT count(*) AS "count" FROM "Documents" WHERE "status"='published' AND "type"=?"""
        return querySingleValue<Long>(sql, docType) ?: 0L
    }

    override val publishedPosts: DocumentList<DocumentModel>
        get() = getPublishedContent("post")

    override fun getPublishedPosts(applyPaging: Boolean) =
        getPublishedContent("post", applyPaging)

    override fun getPublishedPostsByTag(tag: String?): DocumentList<DocumentModel> {
        // Fetch all published posts and filter in-memory since HSQLDB's ARRAY operations are limited
        val sql = """SELECT * FROM "Documents" WHERE "status"='published' AND "type"='post' ORDER BY "date" DESC"""
        val allPosts = query(sql)
        return DocumentList<DocumentModel>().apply {
            addAll(allPosts.filter { doc -> tag in doc.tags })
        }
    }

    override fun getPublishedDocumentsByTag(tag: String?): DocumentList<DocumentModel> {
        val documents = DocumentList<DocumentModel>()
        for (docType in DocumentTypeRegistry.documentTypes) {
            val sql = """SELECT * FROM "Documents" WHERE "status"='published' AND "type"=? ORDER BY "date" DESC"""
            val allDocs = query(sql, docType)
            documents.addAll(allDocs.filter { doc -> tag in doc.tags })
        }
        return documents
    }

    override val publishedPages: DocumentList<DocumentModel>
        get() = getPublishedContent("page")

    override fun getPublishedContent(docType: String) =
        getPublishedContent(docType, false)

    override fun getPublishedContent(docType: String, applyPaging: Boolean): DocumentList<DocumentModel> {
        var sql = """SELECT * FROM "Documents" WHERE "status"='published' AND "type"=? ORDER BY "date" DESC"""
        if (applyPaging && hasStartAndLimitBoundary())
            sql += " OFFSET $paginationOffset LIMIT $paginationLimit"
        return query(sql, docType)
    }

    override fun getAllContent(docType: String) =
        getAllContent(docType, false)

    override fun getAllContent(docType: String, applyPaging: Boolean): DocumentList<DocumentModel> {
        var sql = """SELECT * FROM "Documents" WHERE "type"=? ORDER BY "date" DESC"""
        if (applyPaging && hasStartAndLimitBoundary())
            sql += " OFFSET $paginationOffset LIMIT $paginationLimit"
        return query(sql, docType)
    }

    override val unrenderedContent: DocumentList<DocumentModel>
        get() {
            val sql = """SELECT * FROM "Documents" WHERE "rendered"=false ORDER BY "date" DESC"""
            return query(sql)
        }

    override fun deleteContent(uri: String) {
        val sql = """DELETE FROM "Documents" WHERE "sourceuri"=?"""
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, uri)
            stmt.executeUpdate()
        }
    }

    override fun markContentAsRendered(document: DocumentModel) {
        val sql = """UPDATE "Documents" SET "rendered"=true WHERE "rendered"=false AND "type"=? AND "sourceuri"=? AND "cached"=true"""
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, document.type)
            stmt.setString(2, document.sourceUri)
            stmt.executeUpdate()
        }
    }

    override fun deleteAllByDocType(docType: String) {
        val sql = """DELETE FROM "Documents" WHERE "type"=?"""
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, docType)
            stmt.executeUpdate()
        }
    }

    override val tags: MutableSet<String>
        get() = query("""SELECT "tags" FROM "Documents" WHERE "status"='published' AND "type"='post'""")
            .flatMap { it.tags }
            .toMutableSet()

    override val allTags: MutableSet<String>
        get() = DocumentTypeRegistry.documentTypes
            .flatMap { docType -> query("""SELECT "tags" FROM "Documents" WHERE "status"='published' AND "type"=?""", docType) }
            .flatMap { it.tags }
            .toMutableSet()

    override fun updateAndClearCacheIfNeeded(needed: Boolean, templateDir: File) {
        var clearCache = needed
        if (!needed)
            clearCache = updateTemplateSignatureIfChanged(templateDir)

        if (clearCache) {
            deleteAllDocumentTypes()
            updateSchema()
        }
    }

    override fun resetPagination() {
        paginationOffset = -1
        paginationLimit = -1
    }

    // Private helper methods

    private fun hasStartAndLimitBoundary() =
        (paginationOffset >= 0) && (paginationLimit > -1)

    private fun query(sql: String, vararg params: Any?): DocumentList<DocumentModel> {
        val documents = DocumentList<DocumentModel>()

        connection.prepareStatement(sql).use { stmt ->

            setParameters(stmt, params)

            stmt.executeQuery().use { rs ->

                val metadata = rs.metaData
                val columnCount = metadata.columnCount
                val trace = columnCount > 2
                val columnNames = (1..columnCount).map { metadata.getColumnName(it).lowercase() }
                log.debug { "SQL query: $sql, cols: ${columnNames.joinToString(", ")}" }

                while (rs.next()) {
                    val document = DocumentModel()

                    // Build a Document from the columns, skip `properties`.
                    columnNames.forEachIndexed { idx, colName ->
                        if (colName == "properties") return@forEachIndexed
                        val value = readColumnValue(rs, idx + 1)
                        if (value != null) document[colName] = value
                    }

                    // Use JSON from `properties` column to fill the properties not present in the individual columns.
                    if ("properties" in columnNames) {
                        rs.getString("properties") ?.let { propertiesJson ->
                            applyArbitraryPropertiesToDocument(propertiesJson, document)
                        }
                    }

                    // After merging, ensure 'date' is always OffsetDateTime if possible.
                    // This is the canonical place to ensure the type for downstream consumers (e.g., templates).
                    val dateValue = document[DOC_DATE]
                    log.debug("Date: $dateValue ${dateValue?.javaClass?.name}")
                    when (dateValue) {
                        is Date -> {
                            if (trace) ValueTracer.trace("hsqldb-query-date", document, sql)
                            document[DOC_DATE] = dateValue.toInstant().atOffset(ZoneOffset.UTC)
                        }
                        is String -> {
                            if (trace) ValueTracer.trace("hsqldb-query-string", document, sql)
                            // Try ISO first, then fallback to legacy formats
                            val parsed = parseTemporalIntoOffsetDateTime(dateValue)
                            if (parsed != null) document[DOC_DATE] = parsed
                        }
                        is OffsetDateTime -> document[DOC_DATE] = dateValue
                        // else: already OffsetDateTime or null
                    }

                    ValueTracer.trace("hsqldb-query-mapped", document, sql + "\n\t${document.keys}\n\tdate = ${document.date}")
                    documents.add(document)
                }
            }
        }

        return documents
    }

    private inline fun <reified T> querySingleValue(sql: String, vararg params: Any?): T? {
        connection.prepareStatement(sql).use { stmt ->

            setParameters(stmt, params)

            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                val raw = readColumnValue(rs, 1)
                return convertToType<T>(raw)
            }
        }
    }


    // Read and convert a single column value from the ResultSet, handling special SQL types.
    private fun readColumnValue(rs: ResultSet, columnIndex: Int): Any? {
        val md = rs.metaData
        val sqlType = md.getColumnType(columnIndex)
        return when (sqlType) {
            Types.ARRAY -> rs.getArray(columnIndex)?.let { arr ->
                val a = arr.array as? Array<*>
                a?.map { it?.toString() }?.toTypedArray()
            }
            Types.TIMESTAMP -> rs.getTimestamp(columnIndex)?.toInstant()?.atOffset(ZoneOffset.UTC)
            Types.BOOLEAN -> {
                val b = rs.getBoolean(columnIndex)
                if (rs.wasNull()) null else b
            }
            Types.BIGINT, Types.INTEGER -> {
                val l = rs.getLong(columnIndex)
                if (rs.wasNull()) null else l
            }
            Types.CLOB -> rs.getClob(columnIndex)?.let { it.getSubString(1, it.length().toInt()) }
            Types.BLOB -> rs.getBlob(columnIndex)?.let { String(it.getBytes(1, it.length().toInt())) }
            else -> rs.getObject(columnIndex)
        }
    }

    // Helper to bind parameters to a PreparedStatement (keeps the binding logic in one place)
    private fun setParameters(stmt: PreparedStatement, params: Array<out Any?>) {
        params.forEachIndexed { index, param ->
            when (param) {
                is String -> stmt.setString(index + 1, param)
                is Int -> stmt.setInt(index + 1, param)
                is Long -> stmt.setLong(index + 1, param)
                is Boolean -> stmt.setBoolean(index + 1, param)
                is Timestamp -> stmt.setTimestamp(index + 1, param)
                is Date -> stmt.setTimestamp(index + 1, Timestamp(param.time))
                null -> stmt.setNull(index + 1, Types.VARCHAR)
                else -> stmt.setObject(index + 1, param)
            }
        }
    }

    private inline fun <reified T> convertToType(value: Any?): T? {
        if (value == null) return null
        return when (T::class) {
            Long::class -> when (value) {
                is Long -> value
                is Int -> value.toLong()
                is Number -> value.toLong()
                is String -> value.toLongOrNull()
                else -> null
            } as T?
            Int::class -> when (value) {
                is Int -> value
                is Long -> value.toInt()
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            } as T?
            String::class -> value.toString() as T
            Boolean::class -> when (value) {
                is Boolean -> value
                is Number -> value.toInt() != 0
                is String -> value.toBoolean()
                else -> null
            } as T?
            else -> if (value is T) value else value as? T
        }
    }


    private fun updateTemplateSignatureIfChanged(templateDir: File): Boolean {
        val currentSignature = runCatching { FileUtil.sha1(templateDir) }.getOrElse { "" }
        val sql = """SELECT "sha1" FROM "Signatures" WHERE "key"='templates'"""

        // TODO: IIUC, this scans the whole Signatures table. Also, could be done with MERGE.
        connection.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val existingSignature = rs.getString("sha1")
                    if (existingSignature != currentSignature) {
                        updateSignature(currentSignature)
                        return true
                    }
                } else {
                    insertSignature(currentSignature)
                    return true
                }
            }
        }
        return false
    }

    private fun updateSignature(signature: String) {
        val sql = """UPDATE "Signatures" SET "sha1"=? WHERE "key"='templates'"""
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, signature)
            stmt.executeUpdate()
        }
    }

    private fun insertSignature(signature: String) {
        val sql = """INSERT INTO "Signatures"("key", "sha1") VALUES('templates', ?)"""
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, signature)
            stmt.executeUpdate()
        }
    }

    private fun deleteAllDocumentTypes() {
        for (docType in DocumentTypeRegistry.documentTypes) {
            runCatching { deleteAllByDocType(docType) }
        }
    }

    private val log: Logger by logger()
}


private val log: Logger by org.jbake.util.logger<HsqldbContentRepository>()


private fun parseTemporalIntoOffsetDateTime(dateValue: String): OffsetDateTime? {
    val parsed = runCatching { OffsetDateTime.parse(dateValue) }.getOrNull()
        ?: runCatching {
            /*// Try parsing as java.util.Date
            val d = runCatching { fmtDate1.parse(dateValue) }.getOrNull()
                ?: runCatching { fmtDate2.parse(dateValue) }.getOrNull()
            d?.toInstant()?.atOffset(ZoneOffset.UTC)*/

            optionalFormats.parse(dateValue)
        }
            //.getOrElse { log.warn("Can't parse date string: $dateValue"); null }
            .getOrElse { throw Exception("Can't parse date string: $dateValue") }

    return when (parsed) {
        is OffsetDateTime -> parsed
        is java.time.LocalDateTime -> parsed.atOffset(ZoneOffset.UTC)
        is java.time.LocalDate -> parsed.atStartOfDay().atOffset(ZoneOffset.UTC)
        else -> throw Exception("Unknown date-time type: ${parsed.javaClass.name}")
    }

}

private val fmtDate1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
private val fmtDate2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX")
private val fmtDate3 = SimpleDateFormat("yyyy-MM-dd")

private val optionalFormats = DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm[:ss]XXX]")).toFormatter()
// Other formats: "[MM/dd/yyyy][dd-MM-yyyy][yyyy-MM-dd]"


private fun applyArbitraryPropertiesToDocument(properties: String, document: DocumentModel) {
    @Suppress("UNCHECKED_CAST")
    val map = objectMapper.readValue(properties, Map::class.java) as Map<String, Any>
    // Only add properties that aren't already in the document from individual columns
    for ((key, value) in map) {
        if (key.lowercase() !in document.keys.map { it.lowercase() }) {
            document[key] = value
        }
    }
}

private val objectMapper = ObjectMapper().apply {
    configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    registerModule(JavaTimeModule())  // Support for Java 8 date/time types like OffsetDateTime
}
