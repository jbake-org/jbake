package org.jbake.app;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.Crawler.Attributes.Status;
import org.jbake.model.DocumentAttributes;
import org.jbake.model.DocumentStatus;
import org.jbake.model.DocumentTypes;
import org.jbake.util.HtmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Crawls a file system looking for content.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Crawler {
    public interface Attributes {
        /**
         * Possible values of the {@link Attributes#STATUS} property
         *
         * @author ndx
         */
        interface Status {
            String PUBLISHED_DATE = "published-date";
            String PUBLISHED = "published";
            String DRAFT = "draft";
        }

        String DATE = "date";
        String STATUS = "status";
        String TYPE = "type";
        String TITLE = "title";
        String URI = "uri";
        String FILE = "file";
        String TAGS = "tags";
        String TAG = "tag";
        String ROOTPATH = "rootpath";
        String ID = "id";
        String NO_EXTENSION_URI = "noExtensionUri";
        String ALLTAGS = "alltags";
        String PUBLISHED_DATE = "published_date";
        String BODY = "body";
        String DB = "db";
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

    private CompositeConfiguration config;
    private Parser parser;
    private final ContentStore db;
    private String contentPath;

    /**
     * Creates new instance of Crawler.
     *
     * @param db     Database instance for content
     * @param source Base directory where content directory is located
     * @param config Project configuration
     */
    public Crawler(ContentStore db, File source, CompositeConfiguration config) {
        this.db = db;
        this.config = config;
        this.contentPath = FilenameUtils.concat(source.getAbsolutePath(), config.getString(ConfigUtil.Keys.CONTENT_FOLDER));
        this.parser = new Parser(config, contentPath);
    }

    /**
     * Crawl all files and folders looking for content.
     *
     * @param path Folder to start from
     */
    public void crawl(File path) {
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
                    LOGGER.info(sb.toString());
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
            e.printStackTrace();
            sha1 = "";
        }
        return sha1;
    }

    private String buildURI(final File sourceFile) {
        String uri = FileUtil.asPath(sourceFile.getPath())
                .replace(FileUtil.asPath(contentPath), "")
                // On windows we have to replace the backslash
                .replace(File.separator, "/");

        if (useNoExtensionUri(uri)) {
            // convert URI from xxx.html to xxx/index.html
            uri = createNoExtensionUri(uri);
        } else {
            uri = createUri(uri);
        }

        // strip off leading / to enable generating non-root based sites
        if (uri.startsWith("/")) {
            uri = uri.substring(1, uri.length());
        }
        return uri;
    }

    private String createUri(String uri) {
        return uri.substring(0, uri.lastIndexOf(".")) + config.getString(Keys.OUTPUT_EXTENSION);
    }

    private String createNoExtensionUri(String uri) {
        return "/"
                + FilenameUtils.getPath(uri)
                + FilenameUtils.getBaseName(uri)
                + "/index"
                + config.getString(Keys.OUTPUT_EXTENSION);
    }

    private boolean useNoExtensionUri(String uri) {
        boolean noExtensionUri = config.getBoolean(Keys.URI_NO_EXTENSION);
        String noExtensionUriPrefix = config.getString(Keys.URI_NO_EXTENSION_PREFIX);

        return noExtensionUri
                && (noExtensionUriPrefix != null)
                && (noExtensionUriPrefix.length() > 0)
                && uri.startsWith(noExtensionUriPrefix);
    }

    private void crawlSourceFile(final File sourceFile, final String sha1, final String uri) {
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

            if (config.getBoolean(Keys.URI_NO_EXTENSION)) {
                fileContents.put(Attributes.NO_EXTENSION_URI, uri.replace("/index.html", "/"));
            }
            
            
            // Prevent image source url's from breaking
            HtmlUtil.fixImageSourceUrls(fileContents,config);

            ODocument doc = new ODocument(documentType);
            doc.fromMap(fileContents);
            boolean cached = fileContents.get(DocumentAttributes.CACHED) != null ? Boolean.valueOf((String) fileContents.get(DocumentAttributes.CACHED)) : true;
            doc.field(String.valueOf(DocumentAttributes.CACHED), cached);
            doc.save();
        } else {
            LOGGER.warn("{} has an invalid header, it has been ignored!", sourceFile);
        }
    }

    public String getPathToRoot(File sourceFile) {
    	File rootPath = new File(contentPath);
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
    	if (config.getBoolean(Keys.URI_NO_EXTENSION)) {
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
}
