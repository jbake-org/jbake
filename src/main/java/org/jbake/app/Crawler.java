package org.jbake.app;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.model.DocumentStatus;
import org.jbake.model.DocumentTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.io.File.separator;
import org.jbake.model.LocaleTypes;

/**
 * Crawls a file system looking for content.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Crawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

    private CompositeConfiguration config;
    private Parser parser;
    private final ODatabaseDocumentTx db;
    private String contentPath;

    /**
     * Creates new instance of Crawler.
     */
    public Crawler(ODatabaseDocumentTx db, File source, CompositeConfiguration config) {
        this.db = db;
        this.config = config;
        this.contentPath = source.getPath() + separator + config.getString("content.folder");
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
                    String locale = LocaleUtil.getLocaleFromUri(uri);
                    if (LocaleTypes.hasLocale(locale)) {
                        uri = LocaleUtil.getPathLocalePart(locale) + LocaleUtil.stripLocaleSuffix(uri, locale);
                        LOGGER.info("Localized uri: {}", uri);
                    }
                    else {
                        locale = null; //not configured
                    }
                    boolean process = true;
                    DocumentStatus status = DocumentStatus.NEW;
                    for (String docType : DocumentTypes.getDocumentTypes()) {
                        status = findDocumentStatus(docType, uri, sha1);
                        switch (status) {
                            case UPDATED:
                                sb.append(" : modified ");
                                DBUtil.update(db, "delete from " + docType + " where uri=?", uri);
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
                        crawlSourceFile(sourceFile, sha1, uri, locale);
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
    	String uri = FileUtil.asPath(sourceFile.getPath()).replace(FileUtil.asPath( contentPath), "");
        uri = uri.substring(0, uri.lastIndexOf(".")) + config.getString("output.extension");
        return uri;
    }

    private void crawlSourceFile(final File sourceFile, final String sha1, final String uri) {
        crawlSourceFile(sourceFile, sha1, uri, null);
    }
    
    private void crawlSourceFile(final File sourceFile, final String sha1, final String uri, final String locale) {
        Map<String, Object> fileContents = parser.processFile(sourceFile);
        if (fileContents != null) {
            fileContents.put("sha1", sha1);
            fileContents.put("rendered", false);
            if (fileContents.get("tags") != null) {
                // store them as a String[]
                String[] tags = (String[]) fileContents.get("tags");
                fileContents.put("tags", tags);
            }
            fileContents.put("file", sourceFile.getPath());
            fileContents.put("uri", uri);

            String documentType = (String) fileContents.get("type");
            documentType = LocaleUtil.getLocaleSuffix(locale) + LocaleUtil.stripLocaleSuffix(documentType, locale); //in case if it was supplied twice

            if (DocumentTypes.hasDocumentType(documentType)) {
                fileContents.put("type", documentType); //otherwise keeping whatever was supplied in the file
            }
            
            if (fileContents.get("status").equals("published-date")) {
                if (fileContents.get("date") != null && (fileContents.get("date") instanceof Date)) {
                    if (new Date().after((Date) fileContents.get("date"))) {
                        fileContents.put("status", "published");
                    }
                }
            }
            ODocument doc = new ODocument(documentType);
            doc.fields(fileContents);
            boolean cached = fileContents.get("cached") != null ? Boolean.valueOf((String)fileContents.get("cached")):true;
            doc.field("cached", cached);
            doc.save();
        } else {
            LOGGER.warn("{} has an invalid header, it has been ignored!", sourceFile);
        }
    }

    public int getDocumentCount(String docType) {
        return (int) db.countClass(docType);
    }

    public int getPostCount() {
        return getDocumentCount("post");
    }

    public int getPageCount() {
        return getDocumentCount("page");
    }

    public Set<String> getTags() {
        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select tags from post where status='published'"));
        Set<String> result = new HashSet<String>();
        for (ODocument document : query) {
            String[] tags = DBUtil.toStringArray(document.field("tags"));
            Collections.addAll(result, tags);
        }
        return result;
    }

    private DocumentStatus findDocumentStatus(String docType, String uri, String sha1) {
        List<ODocument> match = DBUtil.query(db, "select sha1,rendered from " + docType + " where uri=?", uri);
        if (!match.isEmpty()) {
            ODocument entries = match.get(0);
            String oldHash = entries.field("sha1");
            if (!(oldHash.equals(sha1)) || Boolean.FALSE.equals(entries.field("rendered"))) {
                return DocumentStatus.UPDATED;
            } else {
                return DocumentStatus.IDENTICAL;
            }
        } else {
            return DocumentStatus.NEW;
        }
    }
}
