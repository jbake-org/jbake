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
 *
 * @author Jonathan Bullock [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
 */
class Crawler {
    private val db: ContentStore
    private val config: JBakeConfiguration
    private val parser: Parser

    /**
     * @param db     Database instance for content
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
     * @param db     Database instance for content
     */
    constructor(db: ContentStore, config: JBakeConfiguration) {
        this.db = db
        this.config = config
        this.parser = Parser(config)
    }

    fun crawl() {
        crawl(config.contentFolder)

        logger.info("Content detected:")
        for (docType in DocumentTypes.documentTypes) {
            val count = db.getDocumentCount(docType)
            if (count > 0) {
                logger.info("Parsed {} files of type: {}", count, docType)
            }
        }
    }

    fun crawlDataFiles() {
        crawlDataFiles(config.dataFolder)

        logger.info("Data files detected:")
        val docType = config.dataFileDocType
        val count = db.getDocumentCount(docType)
        if (count > 0) {
            logger.info("Parsed {} files", count)
        }
    }

    /**
     * Crawl all files and folders looking for content.
     *
     * @param path Folder to start from
     */
    private fun crawl(path: File) {
        val contents = path.listFiles(FileUtil.getFileFilter(config))
        if (contents != null) {
            Arrays.sort(contents)
            for (sourceFile in contents) {
                if (sourceFile.isFile()) {
                    crawlFile(sourceFile)
                } else if (sourceFile.isDirectory()) {
                    crawl(sourceFile)
                }
            }
        }
    }

    private fun crawlFile(sourceFile: File) {
        val sb = StringBuilder()
        sb.append("Processing [").append(sourceFile.path).append("]... ")
        val sha1 = buildHash(sourceFile)
        val uri = buildURI(sourceFile)
        val status = findDocumentStatus(uri, sha1)
        if (status == DocumentStatus.UPDATED) {
            sb.append(" : modified ")
            db.deleteContent(uri)
        } else if (status == DocumentStatus.IDENTICAL) {
            sb.append(" : same ")
        } else if (DocumentStatus.NEW == status) {
            sb.append(" : new ")
        }

        logger.info("{}", sb)

        if (status != DocumentStatus.IDENTICAL) {
            processSourceFile(sourceFile, sha1, uri)
        }
    }

    /**
     * Crawl all files and folders looking for data files.
     *
     * @param path Folder to start from
     */
    private fun crawlDataFiles(path: File) {
        val contents = path.listFiles(FileUtil.dataFileFilter)
        if (contents != null) {
            Arrays.sort(contents)
            for (sourceFile in contents) {
                if (sourceFile.isFile()) {
                    val sb = StringBuilder()
                    sb.append("Processing [").append(sourceFile.path).append("]... ")
                    val sha1 = buildHash(sourceFile)
                    val uri = buildDataFileURI(sourceFile)
                    val docType = config.dataFileDocType
                    val status = findDocumentStatus(uri, sha1)

                    when (status) {
                        DocumentStatus.UPDATED -> {
                            sb.append(" : modified ")
                            db.deleteContent(uri)
                        }
                        DocumentStatus.IDENTICAL -> {
                            sb.append(" : same ")
                            logger.info("{}", sb)
                            continue
                        }
                        DocumentStatus.NEW -> {
                            sb.append(" : new ")
                        }
                    }

                    crawlDataFile(sourceFile, sha1, uri, docType)
                    logger.info("{}", sb)
                }
                if (sourceFile.isDirectory()) {
                    crawlDataFiles(sourceFile)
                }
            }
        }
    }

    private fun buildHash(sourceFile: File): String {
        var sha1: String
        try {
            sha1 = FileUtil.sha1(sourceFile)
        } catch (_: Exception) {
            logger.error("unable to build sha1 hash for source file '{}'", sourceFile)
            sha1 = ""
        }
        return sha1
    }

    private fun buildURI(sourceFile: File): String {
        var uri: String = FileUtil.asPath(sourceFile).replace(FileUtil.asPath(config.contentFolder), "")

        uri = if (useNoExtensionUri(uri)) {
            // convert URI from xxx.html to xxx/index.html
            createNoExtensionUri(uri)
        } else createUri(uri)

        // strip off leading / to enable generating non-root based sites
        if (uri.startsWith(FileUtil.URI_SEPARATOR_CHAR)) {
            uri = uri.substring(1)
        }

        return uri
    }

    private fun buildDataFileURI(sourceFile: File): String {
        var uri: String = FileUtil.asPath(sourceFile).replace(FileUtil.asPath(config.dataFolder), "")
        // strip off leading /
        if (uri.startsWith(FileUtil.URI_SEPARATOR_CHAR)) {
            uri = uri.substring(1, uri.length)
        }
        return uri
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
            val document = parser.processFile(sourceFile)
            if (document != null) {
                document.sha1 = sha1
                document.rendered = true
                document.file = sourceFile.path
                document.sourceUri = uri
                document.type = documentType

                db.addDocument(document)
            } else {
                logger.warn("{} couldn't be parsed so it has been ignored!", sourceFile)
            }
        } catch (ex: Exception) {
            throw RuntimeException("Failed crawling file: " + sourceFile.path + " " + ex.message, ex)
        }
    }

    private fun processSourceFile(sourceFile: File, sha1: String, uri: String) {
        val document = parser.processFile(sourceFile)

        if (document != null) {
            if (DocumentTypes.contains(document.type)) {
                addAdditionalDocumentAttributes(document, sourceFile, sha1, uri)

                if (config.imgPathUpdate) {
                    // Prevent image source url's from breaking
                    HtmlUtil.fixImageSourceUrls(document, config)
                }

                db.addDocument(document)
            } else {
                logger.warn(
                    "{} has an unknown document type '{}' and has been ignored!",
                    sourceFile,
                    document.type
                )
            }
        } else {
            logger.warn("{} has an invalid header, it has been ignored!", sourceFile)
        }
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

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Crawler::class.java)
    }
}
