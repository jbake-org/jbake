package org.jbake.app;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentStatus;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes;
import org.jbake.util.HtmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Crawls a file system looking for content.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Crawler {

    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private final ContentStore db;
    private final ExecutorService executor;
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
        this.executor = Executors.newFixedThreadPool(100);
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
        this.executor = Executors.newFixedThreadPool(100);
    }

    public void crawl() throws InterruptedException {
        crawlContentFiles();
        crawlDataFiles();
        shutdown();

        for (String docType : DocumentTypes.getDocumentTypes()) {
            long count = db.getDocumentCount(docType);
            if (count > 0) {
                logger.info("Parsed {} files of type: {}", count, docType);
            }
        }
    }

    private void crawlContentFiles() {
        crawl(config.getContentFolder(), FileUtil.getFileFilter(config));
    }

    protected void crawlDataFiles() {
        crawl(config.getDataFolder(), FileUtil.getDataFileFilter());
        logger.info("Data files detected:");
    }

    /**
     * Crawl all files and folders looking for content.
     *
     * @param path Folder to start from
     */
    private void crawl(File path, FileFilter filter) {
        File[] filteredFiles = path.listFiles(filter);
        if (filteredFiles != null) {
            Arrays.stream(filteredFiles).parallel().forEach(source -> {
                if (source.isFile()) {
                    crawlFile(source);
                } else if (source.isDirectory()) {
                    crawl(source, filter);
                }
            });
        } else {
            logger.debug("filter does not apply");
        }
    }


    private void crawlFile(File sourceFile) {
        executor.execute(new CrawlAgent(sourceFile, db));
    }

    public void shutdown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }

    class CrawlAgent implements Runnable {

        private final ContentStore db;
        private final File sourceFile;

        CrawlAgent(File sourceFile, ContentStore db) {
            this.sourceFile = sourceFile;
            this.db = db;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            try {
                sb.append("Processing [").append(sourceFile.getPath()).append("]... ");
                String sha1 = parser.buildHash(sourceFile);
                String uri = parser.buildURI(sourceFile);
                DocumentStatus status = findDocumentStatus(uri, sha1);
                if (status == DocumentStatus.UPDATED) {
                    sb.append(" : modified ");
                    db.deleteContent(uri);
                } else if (status == DocumentStatus.IDENTICAL) {
                    sb.append(" : same ");
                } else if (DocumentStatus.NEW == status) {
                    sb.append(" : new ");
                }

                if (status != DocumentStatus.IDENTICAL) {
                    processSourceFile(sourceFile, sha1, uri);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            } finally {
                long end = System.currentTimeMillis();
                long delta = end - start;
                logger.info("{} ({} ms)", sb, delta);
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
            document.setSha1(sha1);
            document.setRendered(true);
            document.setCached(true);
            document.setFile(sourceFile.getPath());
            document.setSourceUri(uri);

            if ( !document.getType().equals(config.getDataFileDocType())) {
                document.setRootPath(getPathToRoot(sourceFile));
                document.setUri(uri);
                document.setRendered(false);

                if (document.getStatus().equals(ModelAttributes.Status.PUBLISHED_DATE)
                    && (document.getDate() != null)
                    && new Date().after(document.getDate())) {
                    document.setStatus(ModelAttributes.Status.PUBLISHED);
                }

                if (config.getUriWithoutExtension()) {
                    document.setNoExtensionUri(uri.replace("/index.html", "/"));
                }
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
}
