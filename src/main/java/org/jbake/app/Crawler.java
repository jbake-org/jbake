package org.jbake.app;

import static org.jbake.app.ContentStatus.*;
import static org.jbake.app.ContentTag.*;

import com.orientechnologies.orient.core.record.impl.ODocument;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.Parser.PostParsingProcessor;
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
import java.util.Set;

import static java.io.File.separator;
import static java.lang.Boolean.FALSE;

/**
 * Crawls a file system looking for content.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Crawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

    private CompositeConfiguration config;
    private Parser parser;
    private final ContentStore db;
    private String contentPath;

    /**
     * Creates new instance of Crawler.
     */
    public Crawler(ContentStore db, File source, CompositeConfiguration config) {
        this.db = db;
        this.config = config;
        this.contentPath = source.getPath() + separator + config.getString(ConfigUtil.Keys.CONTENT_FOLDER);
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
                    DocumentStatus docStatus = DocumentStatus.NEW;
                    for (String docType : DocumentTypes.getDocumentTypes()) {
                        docStatus = findDocumentStatus(docType, uri, sha1);
                        switch (docStatus) {
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
                    if (DocumentStatus.NEW == docStatus) {
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
    	String uri = FileUtil.asPath(sourceFile.getPath()).replace(FileUtil.asPath( contentPath), "");
    	// strip off leading / to enable generating non-root based sites
    	if (uri.startsWith("/")) {
    		uri = uri.substring(1, uri.length());
    	}
        return uri;
    }

    private void crawlSourceFile(final File sourceFile, final String sha1, final String uri) {
        Content fileContents = parser.process(sourceFile, getPostParsingProcessor(sourceFile, sha1, uri));
        if (fileContents == null) {
            LOGGER.warn("{} has an invalid header, it has been ignored!", sourceFile);
            return;
        }
        String documentType = fileContents.getString(type, null);
        ODocument doc = new ODocument(documentType);
        doc.fields(fileContents.getContentAsMap());
        doc.field("cached", fileContents.getBoolean(cached, true));
        doc.save();
    }
    
    public PostParsingProcessor getPostParsingProcessor(final File file, final String sourceSha1, final String sourceUri) {
    	return new PostParsingProcessor() {
    		@Override
    		public void doProcess(final Content contents) {
    		    final File sourceFile = file;
            	contents.put(rootpath, getPathToRoot(sourceFile));
                contents.put(sha1, sourceSha1);
                contents.put(rendered, false);
                if (contents.get(tags) != null) {
                    // store them as a String[]
                    String[] contentTags = (String[]) contents.get(tags);
                    contents.put(tags, contentTags);
                }
                contents.put(ContentTag.file, sourceFile.getPath());
                contents.put(uri, sourceUri.substring(0, sourceUri.lastIndexOf(".")) 
                		+ FileUtil.findExtension(config, contents.getString(type, null)));

                if (contents.getStatus().equals(publishedDate)
                    && contents.get(date) != null 
                    && (contents.get(date) instanceof Date)
                    && new Date().after((Date) contents.get(date))) {
                	contents.setStatus(published);
                }
     			
    		}
    	};
    }

    public String getPathToRoot(File sourceFile) {
    	File rootPath = new File(contentPath);
    	File parentPath = sourceFile.getParentFile();
    	int parentCount = 0;
    	while (!parentPath.equals(rootPath)) {
    		parentPath = parentPath.getParentFile();
    		parentCount++;
    	}
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < parentCount; i++) {
    		sb.append("../");
    	}
    	return sb.toString();
    }
    
    public int getDocumentCount(String docType) {
        return (int) db.countClass(docType);
    }

    public Set<String> getTags() {
        List<ODocument> query = db.getAllTagsFromPublishedPosts(); //query(new OSQLSynchQuery<ODocument>("select tags from post where status='published'"));
        Set<String> result = new HashSet<String>();
        for (ODocument document : query) {
            String[] tags = DBUtil.toStringArray(document.field("tags"));
            Collections.addAll(result, tags);
        }
        return result;
    }

    private DocumentStatus findDocumentStatus(String docType, String uri, String souceSha1) {
        List<ODocument> match = db.getDocumentStatus(docType, uri);
        if (!match.isEmpty()) {
            ODocument entries = match.get(0);
            String oldHash = entries.field(sha1.name());
            if (!(oldHash.equals(souceSha1)) || FALSE.equals(entries.field("rendered"))) {
                return DocumentStatus.UPDATED;
            } else {
                return DocumentStatus.IDENTICAL;
            }
        } else {
            return DocumentStatus.NEW;
        }
    }
}
