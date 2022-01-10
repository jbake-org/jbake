package org.jbake.app;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentStatus;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes;
import org.jbake.util.HtmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

/**
 * Crawls a file system looking for content.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Crawler {

    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private final ContentStore db;
    private final JBakeConfiguration config;
    private final Parser parser;

    /**
     * @param db     Database instance for content
     * @param source Base directory where content directory is located
     * @param config Project configuration
     * @deprecated Use {@link #Crawler(ContentStore, JBakeConfiguration)} instead.
     * <p>
     * Creates new instance of Crawler.
     */
    @Deprecated
    public Crawler(ContentStore db, File source, CompositeConfiguration config) {
        this.db = db;
        this.config = new JBakeConfigurationFactory().createDefaultJbakeConfiguration(source, config);
        this.parser = new Parser(this.config);
    }

    /**
     * Creates new instance of Crawler.
     *
     * @param db     Database instance for content
     * @param config Project configuration
     */
    public Crawler(ContentStore db, JBakeConfiguration config) {
        this.db = db;
        this.config = config;
        this.parser = new Parser(config);
    }

    public void crawl() {
        crawl(config.getContentFolder());

        logger.info("Content detected:");
        for (String docType : DocumentTypes.getDocumentTypes()) {
            long count = db.getDocumentCount(docType);
            if (count > 0) {
                logger.info("Parsed {} files of type: {}", count, docType);
            }
        }
    }

    public void crawlDataFiles() {
        crawlDataFiles(config.getDataFolder());

        logger.info("Data files detected:");
        String docType = config.getDataFileDocType();
        long count = db.getDocumentCount(docType);
        if (count > 0) {
            logger.info("Parsed {} files", count);
        }
    }

    /**
     * Crawl all files and folders looking for content.
     *
     * @param path Folder to start from
     */
    private void crawl(File path) {
        File[] contents = path.listFiles(FileUtil.getFileFilter(config));
        if (contents != null) {
            Arrays.sort(contents);
            for (File sourceFile : contents) {
                if (sourceFile.isFile()) {
                    crawlFile(sourceFile);
                } else if (sourceFile.isDirectory()) {
                    crawl(sourceFile);
                }
            }
        }
    }

    private void crawlFile(File sourceFile) {

        StringBuilder sb = new StringBuilder();
        sb.append("Processing [").append(sourceFile.getPath()).append("]... ");
        String sha1 = buildHash(sourceFile);
        String uri = buildURI(sourceFile);
        DocumentStatus status = findDocumentStatus(uri, sha1);
        if (status == DocumentStatus.UPDATED) {
            sb.append(" : modified ");
            db.deleteContent(uri);
        } else if (status == DocumentStatus.IDENTICAL) {
            sb.append(" : same ");
        } else if (DocumentStatus.NEW == status) {
            sb.append(" : new ");
        }

        logger.info("{}", sb);

        if (status != DocumentStatus.IDENTICAL) {
            processSourceFile(sourceFile, sha1, uri);
        }
    }

    /**
     * Crawl all files and folders looking for data files.
     *
     * @param path Folder to start from
     */
    private void crawlDataFiles(File path) {
        File[] contents = path.listFiles(FileUtil.getDataFileFilter());
        if (contents != null) {
            Arrays.sort(contents);
            for (File sourceFile : contents) {
                if (sourceFile.isFile()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Processing [").append(sourceFile.getPath()).append("]... ");
                    String sha1 = buildHash(sourceFile);
                    String uri = buildDataFileURI(sourceFile);
                    boolean process = true;
                    DocumentStatus status = DocumentStatus.NEW;
                    String docType = config.getDataFileDocType();
                    status = findDocumentStatus(uri, sha1);
                    if (status == DocumentStatus.UPDATED) {
                        sb.append(" : modified ");
                        db.deleteContent(uri);
                    } else if (status == DocumentStatus.IDENTICAL) {
                        sb.append(" : same ");
                        process = false;
                    }
                    if (!process) {
                        break;
                    }
                    if (DocumentStatus.NEW == status) {
                        sb.append(" : new ");
                    }
                    if (process) { // new or updated
                        crawlDataFile(sourceFile, sha1, uri, docType);
                    }
                    logger.info("{}", sb);
                }
                if (sourceFile.isDirectory()) {
                    crawlDataFiles(sourceFile);
                }
            }
        }
    }

    private String buildHash(final File sourceFile) {
        String sha1;
        try {
            sha1 = FileUtil.sha1(sourceFile);
        } catch (Exception e) {
            logger.error("unable to build sha1 hash for source file '{}'", sourceFile);
            sha1 = "";
        }
        return sha1;
    }

    private String buildURI(final File sourceFile) {
        String uri = FileUtil.asPath(sourceFile).replace(FileUtil.asPath(config.getContentFolder()), "");

        if (useNoExtensionUri(uri)) {
            // convert URI from xxx.html to xxx/index.html
            uri = createNoExtensionUri(uri);
        } else {
            uri = createUri(uri);
        }

        // strip off leading / to enable generating non-root based sites
        if (uri.startsWith(FileUtil.URI_SEPARATOR_CHAR)) {
            uri = uri.substring(1);
        }

        return uri;
    }

    private String buildDataFileURI(final File sourceFile) {
        String uri = FileUtil.asPath(sourceFile).replace(FileUtil.asPath(config.getDataFolder()), "");
        // strip off leading /
        if (uri.startsWith(FileUtil.URI_SEPARATOR_CHAR)) {
            uri = uri.substring(1, uri.length());
        }
        return uri;
    }

    // TODO: Refactor - parametrize the following two methods into one.
    // commons-codec's URLCodec could be used when we add that dependency.
    private String createUri(String uri) {
        try {
            return FileUtil.URI_SEPARATOR_CHAR
                + FilenameUtils.getPath(uri)
                + URLEncoder.encode(FilenameUtils.getBaseName(uri), StandardCharsets.UTF_8.name())
                + config.getOutputExtension();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Missing UTF-8 encoding??", e); // Won't happen unless JDK is broken.
        }
    }

    private String createNoExtensionUri(String uri) {
        try {
            return FileUtil.URI_SEPARATOR_CHAR
                + FilenameUtils.getPath(uri)
                + URLEncoder.encode(FilenameUtils.getBaseName(uri), StandardCharsets.UTF_8.name())
                + FileUtil.URI_SEPARATOR_CHAR
                + "index"
                + config.getOutputExtension();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Missing UTF-8 encoding??", e); // Won't happen unless JDK is broken.
        }
    }

    private boolean useNoExtensionUri(String uri) {
        boolean noExtensionUri = config.getUriWithoutExtension();
        String noExtensionUriPrefix = config.getPrefixForUriWithoutExtension();

        return noExtensionUri
            && (noExtensionUriPrefix != null)
            && (noExtensionUriPrefix.length() > 0)
            && uri.startsWith(noExtensionUriPrefix);
    }

    private void crawlDataFile(final File sourceFile, final String sha1, final String uri, final String documentType) {
        try {
            DocumentModel document = parser.processFile(sourceFile);
            if (document != null) {
                document.setSha1(sha1);
                document.setRendered(true);
                document.setFile(sourceFile.getPath());
                document.setSourceUri(uri);
                document.setType(documentType);

                db.addDocument(document);
            } else {
                logger.warn("{} couldn't be parsed so it has been ignored!", sourceFile);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed crawling file: " + sourceFile.getPath() + " " + ex.getMessage(), ex);
        }
    }

    private void processSourceFile(final File sourceFile, final String sha1, final String uri) {
        DocumentModel document = parser.processFile(sourceFile);

        if (document != null) {
            if (DocumentTypes.contains(document.getType())) {
                addAdditionalDocumentAttributes(document, sourceFile, sha1, uri);

                if (config.getImgPathUpdate()) {
                    // Prevent image source url's from breaking
                    HtmlUtil.fixImageSourceUrls(document, config);
                }

                db.addDocument(document);
            } else {
                logger.warn("{} has an unknown document type '{}' and has been ignored!", sourceFile, document.getType());
            }
        } else {
            logger.warn("{} has an invalid header, it has been ignored!", sourceFile);
        }
    }

    private void addAdditionalDocumentAttributes(DocumentModel document, File sourceFile, String sha1, String uri) {
        document.setRootPath(getPathToRoot(sourceFile));
        document.setSha1(sha1);
        document.setRendered(false);
        document.setFile(sourceFile.getPath());
        document.setSourceUri(uri);
        document.setUri(uri);
        document.setCached(true);

        if (document.getStatus().equals(ModelAttributes.Status.PUBLISHED_DATE)
                && (document.getDate() != null)
                && new Date().after(document.getDate())) {
            document.setStatus(ModelAttributes.Status.PUBLISHED);
        }

        if (config.getUriWithoutExtension()) {
            document.setNoExtensionUri(uri.replace("/index.html", "/"));
        }
    }

    private String getPathToRoot(File sourceFile) {
        return FileUtil.getUriPathToContentRoot(config, sourceFile);
    }

    private DocumentStatus findDocumentStatus(String uri, String sha1) {
        DocumentList<DocumentModel> match = db.getDocumentStatus(uri);
        if (!match.isEmpty()) {
            DocumentModel document = match.get(0);
            String oldHash = document.getSha1();
            if (!oldHash.equals(sha1) || !document.getRendered()) {
                return DocumentStatus.UPDATED;
            } else {
                return DocumentStatus.IDENTICAL;
            }
        } else {
            return DocumentStatus.NEW;
        }
    }

}
