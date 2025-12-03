package org.jbake.app

import org.apache.commons.io.FilenameUtils
import org.jbake.app.FileUtil.URI_SEPARATOR_CHAR
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypeRegistry
import org.jbake.model.ModelAttributes
import org.jbake.util.HtmlUtil
import org.jbake.util.Logging.logger
import org.slf4j.Logger
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Crawls a file system looking for content.
 */
class Crawler {

    private val db: ContentStore
    private val config: JBakeConfiguration
    private val parser: Parser


    constructor(db: ContentStore, config: JBakeConfiguration) {
        this.db = db
        this.config = config
        this.parser = Parser(this.config)
    }

    fun crawlContentDirectory() {
        crawlDirectory(config.contentDir)

        for (docType in DocumentTypeRegistry.documentTypes) {
            val count = db.getDocumentCount(docType)
            if (count > 0) log.info("Parsed {} files of type: {}", count, docType)
        }
    }

    /**
     * Crawl all files and directorys looking for content.
     */
    private fun crawlDirectory(startFromDirectory: File) {
        val contents = startFromDirectory.listFiles(FileUtil.getFileFilter(config)) ?: return

        Arrays.sort(contents)
        for (sourceFile in contents) {
            when {
                sourceFile.isFile -> crawlFile(sourceFile)
                sourceFile.isDirectory -> crawlDirectory(sourceFile)
            }
        }
    }

    private fun crawlFile(sourceFile: File) {
        val sha1 = runCatching { FileUtil.sha1(sourceFile) }
            .getOrElse { log.error("Unable to build SHA1 hash for source file '$sourceFile'"); "" }

        val uri = buildURI(sourceFile)

        when (findDocumentStatus(uri, sha1)) {
            DocumentStatus.UPDATED -> { log.info("MODIFIED:" + sourceFile.path); db.deleteContent(uri) }
            DocumentStatus.IDENTICAL -> log.info("SAME:    " + sourceFile.path)
            DocumentStatus.NEW -> log.info("NEW:     " + sourceFile.path)
        }

        if (findDocumentStatus(uri, sha1) != DocumentStatus.IDENTICAL)
            processSourceFile(sourceFile, sha1, uri)
    }

    /**
     * Crawl all files and directorys looking for data files.
     */
    fun crawlDataFiles() {
        crawlDataFiles(config.dataDir)

        val count = db.getDocumentCount(config.dataFileDocType)
        if (count > 0) log.info("Parsed {} files", count)
    }

    private fun crawlDataFiles(startFromDirectory: File) {
        val contents = startFromDirectory.listFiles(FileUtil.dataFileFilter) ?: return

        for (sourceFile in contents.sorted()) {
            if (sourceFile.isDirectory) {
                crawlDataFiles(sourceFile)
                continue
            }

            val sha1 = runCatching { FileUtil.sha1(sourceFile) }
                .getOrElse { log.error("Unable to build SHA1 hash for source file '$sourceFile'"); "" }

            val uri = buildDataFileUri(sourceFile)

            when (findDocumentStatus(uri, sha1)) {
                DocumentStatus.UPDATED -> { log.info("MODIFIED:" + sourceFile.path); db.deleteContent(uri) }
                DocumentStatus.IDENTICAL -> log.info("SAME:    " + sourceFile.path).also { continue }
                DocumentStatus.NEW -> log.info("NEW:     " + sourceFile.path)
            }

            crawlDataFile(sourceFile, sha1, uri, config.dataFileDocType)
        }
    }

    private fun buildURI(sourceFile: File): String {
        val uri = FileUtil.asPath(sourceFile).replace(FileUtil.asPath(config.contentDir), "")

        val processedUri =
            // Convert URI from xxx.html to xxx/index.html .
            if (useNoExtensionUri(uri))
                createNoExtensionUri(uri)
            else createUri(uri)

        // Strip off leading / to enable generating non-root based sites.
        return processedUri.removePrefix(URI_SEPARATOR_CHAR)
    }

    private fun buildDataFileUri(sourceFile: File): String {
        return FileUtil.asPath(sourceFile)
            .replace(FileUtil.asPath(config.dataDir), "")
            .removePrefix(URI_SEPARATOR_CHAR)
    }

    // TODO: Refactor - parametrize the following two methods into one. commons-codec's URLCodec could be used when we add that dependency.
    private fun createUri(uri: String): String {
        return (URI_SEPARATOR_CHAR
                + FilenameUtils.getPath(uri)
                + URLEncoder.encode(FilenameUtils.getBaseName(uri), StandardCharsets.UTF_8.name())
                + config.outputExtension)
    }

    private fun createNoExtensionUri(uri: String): String {
        return (URI_SEPARATOR_CHAR
                + FilenameUtils.getPath(uri)
                + URLEncoder.encode(FilenameUtils.getBaseName(uri), StandardCharsets.UTF_8.name())
                + URI_SEPARATOR_CHAR
                + "index"
                + config.outputExtension)
    }

    private fun useNoExtensionUri(uri: String): Boolean {
        val noExtensionUri = config.uriWithoutExtension
        val noExtensionUriPrefix = config.prefixForUriWithoutExtension

        return noExtensionUri
                && noExtensionUriPrefix != null
                && noExtensionUriPrefix.isNotEmpty()
                && uri.startsWith(noExtensionUriPrefix)
    }

    private fun crawlDataFile(sourceFile: File, sha1: String, uri: String, documentType: String) {
        try {
            val document = parser.processFile(sourceFile) ?: run {
                log.warn("{} couldn't be parsed so it has been ignored!", sourceFile)
                return
            }

            document.sha1 = sha1
            document.rendered = true
            document.file = sourceFile.path
            document.sourceUri = uri
            document.type = documentType

            db.addDocument(document)
        } catch (ex: Exception) {
            throw RuntimeException("Failed crawling file: " + sourceFile.path + " " + ex.message, ex)
        }
    }

    private fun processSourceFile(sourceFile: File, sha1: String, uri: String) {
        val document = parser.processFile(sourceFile) ?: run {
            log.warn("{} has an invalid header, it has been ignored!", sourceFile)
            return
        }

        if (!DocumentTypeRegistry.contains(document.type)) {
            log.warn("{} has an unknown document type '{}' and has been ignored!", sourceFile, document.type)
            return
        }

        addAdditionalDocumentAttributes(document, sourceFile, sha1, uri)

        // Prevent image source URL's from breaking.
        if (config.imgPathUpdate)
            HtmlUtil.fixImageSourceUrls(document, config)

        db.addDocument(document)
        db.addDocument(document)
    }

    private fun addAdditionalDocumentAttributes(document: DocumentModel, sourceFile: File, sha1: String, uri: String) {
        document.rootPath = FileUtil.getUriPathToSourceRoot(config, sourceFile)
        document.sha1 = sha1
        document.rendered = false
        document.file = sourceFile.path
        document.sourceUri = uri
        document.uri = uri
        document.cached = true

        if (document.status == ModelAttributes.Status.PUBLISHED_DATE
                && document.date != null
                && Date().after(document.date)
        )
            document.status = ModelAttributes.Status.PUBLISHED

        if (config.uriWithoutExtension)
            document.noExtensionUri = uri.replace("/index.html", "/")
    }

    private fun findDocumentStatus(uri: String, sha1: String): DocumentStatus {
        val match = db.getDocumentStatus(uri)
        if (match.isEmpty()) return DocumentStatus.NEW

        val document = match[0]
        val oldHash = document.sha1

        return if (oldHash != sha1 || !document.rendered) DocumentStatus.UPDATED
            else DocumentStatus.IDENTICAL
    }

    private val log: Logger by logger()
}


/** Enumeration used to determine whether rendering of a document should be done. */
enum class DocumentStatus {
    NEW,
    UPDATED,
    IDENTICAL
}
