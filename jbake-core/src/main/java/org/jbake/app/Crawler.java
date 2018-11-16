package org.jbake.app;

import com.orientechnologies.orient.core.record.impl.ODocument;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbake.app.Crawler.Attributes.Status;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.jbake.model.DocumentAttributes;
import org.jbake.model.DocumentStatus;
import org.jbake.model.DocumentTypes;
import org.jbake.util.HtmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Crawls a file system looking for content.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

    public static final String URI_SEPARATOR_CHAR = "/";
    private final ContentStore db;
    private JBakeConfiguration config;
    private Parser parser;

    /**
     * Creates new instance of Crawler.
     *
     * @param db     Database instance for content
     * @param source Base directory where content directory is located
     * @param config Project configuration
     * @deprecated Use {@link #Crawler(ContentStore, JBakeConfiguration)} instead.
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

        LOGGER.info("Content detected:");
        for (String docType : DocumentTypes.getDocumentTypes()) {
            long count = db.getDocumentCount(docType);
            if (count > 0) {
                LOGGER.info("Parsed {} files of type: {}", count, docType);
            }
        }

    }

    /**
     * Crawl all files and folders looking for content.
     *
     * @param path Folder to start from
     */
    private void crawl(File path) {
        File[] contents = path.listFiles(FileUtil.getFileFilter());
        if (contents != null) {
            Arrays.sort(contents);
            for (File sourceFile : contents) {
                if (sourceFile.isFile()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Processing [").append(sourceFile.getPath()).append("]... ");
                    String sha1 = buildHash(sourceFile);
                    String uri = buildURI(sourceFile);
                    boolean process = true;
                    DocumentStatus status = DocumentStatus.NEW;
                    for (String docType : DocumentTypes.getDocumentTypes()) {
                        status = findDocumentStatus(docType, uri, sha1);
                        switch (status) {
                            case UPDATED:
                                sb.append(" : modified ");
                                db.deleteContent(docType, uri);
                                break;
                            case IDENTICAL:
                                sb.append(" : same ");
                                process = false;
                        }
                        if (!process) {
                            break;
                        }
                    }
                    if (DocumentStatus.NEW == status) {
                        sb.append(" : new ");
                    }
                    if (process) { // new or updated
                        crawlSourceFile(sourceFile, sha1, uri);
                    }
                    LOGGER.info("{}", sb);
                }
                if (sourceFile.isDirectory()) {
                    crawl(sourceFile);
                }
            }
        }
    }

    private String buildHash(final File sourceFile) {
        String sha1;
        try {
            sha1 = FileUtil.sha1(sourceFile);
        } catch (Exception e) {
            LOGGER.error("unable to build sha1 hash for source file '{}'", sourceFile);
            sha1 = "";
        }
        return sha1;
    }

    private String buildURI(final File sourceFile)
    {
        String contentPath = FileUtil.asPath(config.getContentFolder());

        //  /jbake-web/content/path/to/file.ext
        //                ->   path/to/file.ext
        String uri = FileUtil.asPath(sourceFile).replace(contentPath, "");

        // On windows we have to replace the backslash
        if (!File.separator.equals(URI_SEPARATOR_CHAR)) {
            uri = uri.replace(File.separator, URI_SEPARATOR_CHAR);
        }

        uri = createUri(uri, useNoExtensionUri(uri));

        // Strip off leading / to enable generating non-root based sites
        uri = StringUtils.removeStart(uri, URI_SEPARATOR_CHAR);

        return uri;
    }

    /**
     * Takes care of file name characters that need to be URL-encoded.
     * TODO: Take care of URL-encoding for directory names.
     * @param extensionlessMode  If true, converts URI from path/to/file.html to path/to/file/index.html,
     *                           so that the user URL can be "path/to/file".
     */
    private String createUri(String uri, boolean extensionlessMode) {
        try {
            return "/" + FilenameUtils.getPath(uri)
                    // commons-codec's URLCodec could be used when we add that dependency.
                    + URLEncoder.encode(FilenameUtils.getBaseName(uri), StandardCharsets.UTF_8.name())
                    // If asked to, convert URI from xxx.html to xxx/index.html
                    + (extensionlessMode ? URI_SEPARATOR_CHAR + "index" : "")
                    + config.getOutputExtension();
        }
        catch (UnsupportedEncodingException e) {
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

    private void crawlSourceFile(final File sourceFile, final String sha1, final String uri) {
        try {
            Map<String, Object> fileContents = parser.processFile(sourceFile);
            if (fileContents != null) {
                fileContents.put(Attributes.ROOTPATH, getPathToRoot(sourceFile));
                fileContents.put(String.valueOf(DocumentAttributes.SHA1), sha1);
                fileContents.put(String.valueOf(DocumentAttributes.RENDERED), false);
                if (fileContents.get(Attributes.TAGS) != null) {
                    // store them as a String[]
                    String[] tags = (String[]) fileContents.get(Attributes.TAGS);
                    fileContents.put(Attributes.TAGS, tags);
                }
                fileContents.put(Attributes.FILE, sourceFile.getPath());
                fileContents.put(String.valueOf(DocumentAttributes.SOURCE_URI), uri);
                fileContents.put(Attributes.URI, uri);

                String documentType = (String) fileContents.get(Attributes.TYPE);
                if (fileContents.get(Attributes.STATUS).equals(Status.PUBLISHED_DATE)) {
                    if (fileContents.get(Attributes.DATE) != null && (fileContents.get(Attributes.DATE) instanceof Date)) {
                        if (new Date().after((Date) fileContents.get(Attributes.DATE))) {
                            fileContents.put(Attributes.STATUS, Status.PUBLISHED);
                        }
                    }
                }

                if (config.getUriWithoutExtension()) {
                    fileContents.put(Attributes.NO_EXTENSION_URI, uri.replace("/index.html", "/"));
                }

                if (config.getImgPathUpdate()) {
                    // Prevent image source url's from breaking
                    HtmlUtil.fixImageSourceUrls(fileContents, config);
                }

                ODocument doc = new ODocument(documentType);
                doc.fromMap(fileContents);
                // This just stores true if it's not there.
                String attrCached = DocumentAttributes.CACHED.toString();
                String currentValue = (String) fileContents.get(attrCached);
                boolean isCached = BooleanUtils.toBoolean(currentValue, null, "false");
                doc.field(attrCached, isCached);
                doc.save();
            }
            else {
                LOGGER.warn("{} has an invalid header, it has been ignored!", sourceFile);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed crawling file: " + sourceFile.getPath() + " " + ex.getMessage(), ex);
        }
    }

    private String getPathToRoot(File sourceFile) {
        File rootPath = config.getContentFolder();
        File parentPath = sourceFile.getParentFile();
        int parentCount = 0;
        while (!parentPath.equals(rootPath)) {
            parentPath = parentPath.getParentFile();
            parentCount++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parentCount; i++) {
            sb.append("../");
        }
        if (config.getUriWithoutExtension()) {
            sb.append("../");
        }
        return sb.toString();
    }

    private DocumentStatus findDocumentStatus(String docType, String uri, String sha1) {
        DocumentList match = db.getDocumentStatus(docType, uri);
        if (!match.isEmpty()) {
            Map entries = match.get(0);
            String oldHash = (String) entries.get(String.valueOf(DocumentAttributes.SHA1));
            if (!(oldHash.equals(sha1)) || Boolean.FALSE.equals(entries.get(String.valueOf(DocumentAttributes.RENDERED)))) {
                return DocumentStatus.UPDATED;
            } else {
                return DocumentStatus.IDENTICAL;
            }
        } else {
            return DocumentStatus.NEW;
        }
    }

    public abstract static class Attributes {

        public static final String DATE = "date";
        public static final String STATUS = "status";
        public static final String TYPE = "type";
        public static final String TITLE = "title";
        public static final String URI = "uri";
        public static final String FILE = "file";
        public static final String TAGS = "tags";
        public static final String TAG = "tag";
        public static final String ROOTPATH = "rootpath";
        public static final String ID = "id";
        public static final String NO_EXTENSION_URI = "noExtensionUri";
        public static final String ALLTAGS = "alltags";
        public static final String PUBLISHED_DATE = "published_date";
        public static final String BODY = "body";
        public static final String DB = "db";

        /**
         * Possible values of the {@link Attributes#STATUS} property
         *
         * @author ndx
         */
        public abstract static class Status {
            public static final String PUBLISHED_DATE = "published-date";
            public static final String PUBLISHED = "published";
            public static final String DRAFT = "draft";

            private Status() {
            }
        }

        private Attributes() {
        }

    }
}
