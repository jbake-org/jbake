package org.jbake.app

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.io.FilenameUtils
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentStatus
import org.jbake.model.DocumentTypes
import org.jbake.model.ModelAttributes
import org.jbake.util.HtmlUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.UnsupportedEncodingException
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

    /**
     * @param source Base directory where content directory is located
     */
    @Deprecated("""Use {@link #Crawler(ContentStore, JBakeConfiguration)} instead.
      <p>
      Creates new instance of Crawler.""")
    constructor(db: ContentStore, source: File, config: CompositeConfiguration) {
        this.db = db
        this.config = JBakeConfigurationFactory().createDefaultJbakeConfiguration(source, config)
        this.parser = Parser(this.config)
    }

    /**
     * Creates new instance of Crawler.
     *
     */
    constructor(db: ContentStore, config: JBakeConfiguration) {
        this.db = db
        this.config = config
        this.parser = Parser(config)
    }

    fun crawl() {
        crawl(config.contentFolder)

        log.info("Content detected:")
        for (docType in DocumentTypes.documentTypes) {
            val count = db.getDocumentCount(docType)
            if (count > 0) {
                log.info("Parsed {} files of type: {}", count, docType)
            }
        }
    }

    fun crawlDataFiles() {
        crawlDataFiles(config.dataFolder)

        log.info("Data files detected:")
        val docType = config.dataFileDocType
        val count = db.getDocumentCount(docType)
        if (count > 0) {
            log.info("Parsed {} files", count)
        }
    }

    /**
     * Crawl all files and folders looking for content.
     */
    private fun crawl(startFromDirectory: File) {
        val contents = startFromDirectory.listFiles(FileUtil.getFileFilter(config)) ?: return

        Arrays.sort(contents)
        for (sourceFile in contents) {
            when {
                sourceFile.isFile -> crawlFile(sourceFile)
                sourceFile.isDirectory -> crawl(sourceFile)
            }
        }
    }

    private fun crawlFile(sourceFile: File) {
        val sha1 = buildHash(sourceFile)
        val uri = buildURI(sourceFile)
        val status = findDocumentStatus(uri, sha1)

        val message = buildString {
            append("Processing [").append(sourceFile.path).append("]... ")
            when (status) {
                DocumentStatus.UPDATED -> { append(" : modified "); db.deleteContent(uri) }
                DocumentStatus.IDENTICAL -> append(" : same ")
                DocumentStatus.NEW -> append(" : new ")
            }
        }

        log.info("{}", message)

        if (status != DocumentStatus.IDENTICAL) {
            processSourceFile(sourceFile, sha1, uri)
        }
    }

    /**
     * Crawl all files and folders looking for data files.
     */
    private fun crawlDataFiles(startFromDirectory: File) {
        val contents = startFromDirectory.listFiles(FileUtil.dataFileFilter) ?: return

        Arrays.sort(contents)
        for (sourceFile in contents) {
            if (sourceFile.isDirectory) {
                crawlDataFiles(sourceFile)
                continue
            }

            val sha1 = buildHash(sourceFile)
            val uri = buildDataFileURI(sourceFile)

            when (findDocumentStatus(uri, sha1)) {
                DocumentStatus.UPDATED -> { log.info("MODIFIED:" + sourceFile.path); db.deleteContent(uri) }
                DocumentStatus.IDENTICAL -> log.info("SAME:    " + sourceFile.path).also { continue }
                DocumentStatus.NEW -> log.info("NEW:     " + sourceFile.path)
            }

            crawlDataFile(sourceFile, sha1, uri, config.dataFileDocType)
        }
    }

    private fun buildHash(sourceFile: File): String {
        return try {
            FileUtil.sha1(sourceFile)
        } catch (_: Exception) {
            "".also { log.error("Unable to build SHA1 hash for source file '$sourceFile'") }
        }
    }

    private fun buildURI(sourceFile: File): String {
        val uri = FileUtil.asPath(sourceFile).replace(FileUtil.asPath(config.contentFolder), "")

        val processedUri = if (useNoExtensionUri(uri)) {
            // Convert URI from xxx.html to xxx/index.html .
            createNoExtensionUri(uri)
        } else {
            createUri(uri)
        }

        // Strip off leading / to enable generating non-root based sites.
        return processedUri.removePrefix(FileUtil.URI_SEPARATOR_CHAR)
    }

    private fun buildDataFileURI(sourceFile: File): String {
        val uri = FileUtil.asPath(sourceFile).replace(FileUtil.asPath(config.dataFolder), "")
        // strip off leading /
        return uri.removePrefix(FileUtil.URI_SEPARATOR_CHAR)
    }

    // TODO: Refactor - parametrize the following two methods into one.
    // commons-codec's URLCodec could be used when we add that dependency.
    private fun createUri(uri: String): String {
        try {
            return (FileUtil.URI_SEPARATOR_CHAR
                    + FilenameUtils.getPath(uri)
                    + URLEncoder.encode(FilenameUtils.getBaseName(uri), StandardCharsets.UTF_8.name())
                    + config.outputExtension)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Missing UTF-8 encoding??", e) // Won't happen unless JDK is broken.
        }
    }

    private fun createNoExtensionUri(uri: String): String {
        try {
            return (FileUtil.URI_SEPARATOR_CHAR
                    + FilenameUtils.getPath(uri)
                    + URLEncoder.encode(FilenameUtils.getBaseName(uri), StandardCharsets.UTF_8.name())
                    + FileUtil.URI_SEPARATOR_CHAR
                    + "index"
                    + config.outputExtension)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Missing UTF-8 encoding??", e) // Won't happen unless JDK is broken.
        }
    }

    private fun useNoExtensionUri(uri: String): Boolean {
        val noExtensionUri = config.uriWithoutExtension
        val noExtensionUriPrefix = config.prefixForUriWithoutExtension

        return noExtensionUri
                && (noExtensionUriPrefix != null)
                && (noExtensionUriPrefix.isNotEmpty())
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

        if (!DocumentTypes.contains(document.type)) {
            log.warn(
                "{} has an unknown document type '{}' and has been ignored!",
                sourceFile,
                document.type
            )
            return
        }

        addAdditionalDocumentAttributes(document, sourceFile, sha1, uri)

        if (config.imgPathUpdate) {
            // Prevent image source url's from breaking
            HtmlUtil.fixImageSourceUrls(document, config)
        }

        db.addDocument(document)
    }

    private fun addAdditionalDocumentAttributes(document: DocumentModel, sourceFile: File, sha1: String, uri: String) {
        document.rootPath = getPathToRoot(sourceFile)
        document.sha1 = sha1
        document.rendered = false
        document.file = sourceFile.path
        document.sourceUri = uri
        document.uri = uri
        document.cached = true

        if (document.status == ModelAttributes.Status.PUBLISHED_DATE
            && (document.date != null)
            && Date().after(document.date)
        ) {
            document.status = ModelAttributes.Status.PUBLISHED
        }

        if (config.uriWithoutExtension) {
            document.noExtensionUri = uri.replace("/index.html", "/")
        }
    }

    private fun getPathToRoot(sourceFile: File): String {
        return FileUtil.getUriPathToContentRoot(config, sourceFile)
    }

    private fun findDocumentStatus(uri: String, sha1: String): DocumentStatus {
        val match = db.getDocumentStatus(uri)
        if (match.isEmpty()) return DocumentStatus.NEW

        val document = match[0]
        val oldHash = document.sha1

        return if (oldHash != sha1 || !document.rendered) DocumentStatus.UPDATED
            else DocumentStatus.IDENTICAL
    }

    private val log: Logger = LoggerFactory.getLogger(Crawler::class.java)
}
