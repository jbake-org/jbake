package org.jbake.app;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.model.DocumentStatus;
import org.jbake.model.DocumentTypes;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;

/**
 * Crawls a file system looking for content.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Crawler {

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
                    System.out.print("Processing [" + sourceFile.getPath() + "]... ");
                    String sha1 = buildHash(sourceFile);
                    String uri = buildURI(sourceFile);
                    boolean process = true;
                    DocumentStatus status = DocumentStatus.NEW;
                    for (String docType : DocumentTypes.getDocumentTypes()) {
                        status = findDocumentStatus(docType, uri, sha1);
                        switch (status) {
                            case UPDATED:
                                System.out.print(" : modified ");
                                DBUtil.update(db, "delete from " + docType + " where uri=?", uri);
                                break;
                            case IDENTICAL:
                                System.out.print(" : same ");
                                process = false;
                        }
                        if (!process) {
                            break;
                        }
                    }
                    if (DocumentStatus.NEW == status) {
                        System.out.print(" : new ");
                    }
                    if (process) { // new or updated
                        crawlSourceFile(sourceFile, sha1, uri);
                    }
                }
                if (sourceFile.isDirectory()) {
                    crawl(sourceFile);
                } else {
                    System.out.println("done!");
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
        String uri = sourceFile.getPath().replace(contentPath, "");
        uri = uri.substring(0, uri.lastIndexOf(".")) + config.getString("output.extension");
        return uri;
    }

    private void crawlSourceFile(final File sourceFile, final String sha1, final String uri) {
        Map<String, Object> fileContents = parser.processFile(sourceFile);
        fileContents.put("sha1", sha1);
        fileContents.put("rendered", false);
        if (fileContents != null) {
            if (fileContents.get("tags") != null) {
                // store them as a String[]
                String[] tags = (String[]) fileContents.get("tags");
                fileContents.put("tags", tags);
            }
            fileContents.put("file", sourceFile.getPath());
            fileContents.put("uri", uri);

            String documentType = (String) fileContents.get("type");
            if (fileContents.get("status").equals("published-date")) {
                if (fileContents.get("date") != null && (fileContents.get("date") instanceof Date)) {
                    if (new Date().after((Date) fileContents.get("date"))) {
                        fileContents.put("status", "published");
                    }
                }
            }
            ODocument doc = new ODocument(documentType);
            doc.fields(fileContents);
            doc.save();
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

    @SuppressWarnings("unchecked")
    public Map<String, DocumentIterator> getPostsByTags() {
        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from post where status='published'"));
        Map<String, List<ODocument>> tmp = new HashMap<String, List<ODocument>>();
        for (ODocument entry : query) {
            Object field = entry.field("tags");
            String[] tags;
            if (field instanceof String[]) {
                tags = (String[]) field;
            } else if (field instanceof OTrackedList) {
                tags = ((OTrackedList<String>) field).toArray(new String[((OTrackedList<String>) field).size()]);
            } else {
                tags = new String[0];
            }
            for (String tag : tags) {
                List<ODocument> list = tmp.get(tag);
                if (list == null) {
                    list = new LinkedList<ODocument>();
                    tmp.put(tag, list);
                }
                list.add(entry);
            }
        }
        Map<String, DocumentIterator> result = new HashMap<String, DocumentIterator>();
        for (Map.Entry<String, List<ODocument>> entry : tmp.entrySet()) {
            final String tag = entry.getKey();
            final Iterator<ODocument> it = entry.getValue().iterator();
            result.put(tag, new DocumentIterator(it));
        }
        return result;
    }

    private DocumentStatus findDocumentStatus(String docType, String uri, String sha1) {
        List<ODocument> match = DBUtil.query(db, "select sha1 from " + docType + " where uri=?", uri);
        if (!match.isEmpty()) {
            String oldHash = match.get(0).field("sha1");
            if (!(oldHash.equals(sha1))) {
                return DocumentStatus.UPDATED;
            } else {
                return DocumentStatus.IDENTICAL;
            }
        } else {
            return DocumentStatus.NEW;
        }
    }
}
