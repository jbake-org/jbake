package org.jbake.app;


import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.Crawler.Attributes.Status;
import org.jbake.model.DocumentStatus;
import org.jbake.model.DocumentTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.io.File.separator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Crawls a file system looking for content.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Crawler {
	public static interface Attributes {
		/**
		 * Possible values of the {@link Attributes#STATUS} property
		 * @author ndx
		 *
		 */
		public static interface Status {

			static final String PUBLISHED_DATE = "published-date";
			static final String PUBLISHED = "published";
			static final String DRAFT = "draft";
		}
		static final String CACHED = "cached";
		static final String DATE = "date";
		static final String STATUS = "status";
		static final String TYPE = "type";
		static final String TITLE = "title";
		static final String URI = "uri";
		static final String SOURCE_URI = "sourceuri";
		static final String FILE = "file";
		static final String TAGS = "tags";
		static final String TAG = "tag";
		static final String RENDERED = "rendered";
		static final String SHA1 = "sha1";
		static final String ROOTPATH = "rootpath";
		static final String ID = "id";
		static final String NO_EXTENSION_URI = "noExtensionUri";
		static final String PERMALINK = "permalink";
		
	}
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
    	String uri = FileUtil.asPath(sourceFile.getPath()).replace(FileUtil.asPath( contentPath), "");
    	
    	boolean noExtensionUri = config.getBoolean(Keys.URI_NO_EXTENSION);
    	String noExtensionUriPrefix = config.getString(Keys.URI_NO_EXTENSION_PREFIX);
    	if (noExtensionUri && noExtensionUriPrefix != null && noExtensionUriPrefix.length() > 0) {
        	// convert URI from xxx.html to xxx/index.html
    		if (uri.startsWith(noExtensionUriPrefix)) {
    			uri = "/" + FilenameUtils.getPath(uri) + FilenameUtils.getBaseName(uri) + "/index" + config.getString(Keys.OUTPUT_EXTENSION);
    		}
        } else {
            uri = uri.substring(0, uri.lastIndexOf(".")) + config.getString(Keys.OUTPUT_EXTENSION);
        }
    	
        // strip off leading / to enable generating non-root based sites
    	if (uri.startsWith("/")) {
    		uri = uri.substring(1, uri.length());
    	}
        return uri;
    }

    private void crawlSourceFile(final File sourceFile, final String sha1, final String uri) {
        Map<String, Object> fileContents = parser.processFile(sourceFile);
        if (fileContents != null) {
        	fileContents.put(Attributes.ROOTPATH, getPathToRoot(sourceFile));
            fileContents.put(Attributes.SHA1, sha1);
            fileContents.put(Attributes.RENDERED, false);
            if (fileContents.get(Attributes.TAGS) != null) {
                // store them as a String[]
                String[] tags = (String[]) fileContents.get(Attributes.TAGS);
                fileContents.put(Attributes.TAGS, tags);
            }
            fileContents.put(Attributes.FILE, sourceFile.getPath());
            fileContents.put(Attributes.SOURCE_URI, uri);
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
           
           String permalink = buildPermalink(fileContents);
           fileContents.put(Attributes.PERMALINK, permalink);
            
            ODocument doc = new ODocument(documentType);
            doc.fields(fileContents);
            boolean cached = fileContents.get(Attributes.CACHED) != null ? Boolean.valueOf((String)fileContents.get(Attributes.CACHED)):true;
            doc.field(Attributes.CACHED, cached);
            doc.save();
        } else {
            LOGGER.warn("{} has an invalid header, it has been ignored!", sourceFile);
        }
    }
    
    /**
     * This function generates permalinks if they are enabled in configuration.
     * 
     * Default pattern is /:filepath
     * 
     * Conditions -
     * 	1. String ending with ':' is treated as static strings. For example, permalink = /:blogdata:/:filepath, will generate all urls as /blogdata/{actual file path}
     *  2. :filepath is reserved to add actual source file path (relative to content root)
     *  3. :filename is reserved to add name of source file.
     *  4. If the keyword values is array then all values of array are used for generation. For example, if /:tags is used and post has two tags tagA, tagB then url would be /tagA/tagB
     *  5. :YEAR, :MONTH, :DAY are reserved to pull related part of content published date.
     *  
     * If uri.noExtension is enabled then permalink generation will use it to generate extension less urls.
     * 
     * on front end, permalinks can be accessed as {content.permalink}
     * 
     * @param fileContents
     * @author Manik Magar
     * @return
     */
    private String buildPermalink(Map<String, Object> fileContents){
    	String permalink = "";
    	String separator = File.separator;
    	String permalinkPattern = config.getString(Attributes.PERMALINK,"/:filepath");
    	if(config.containsKey(Attributes.PERMALINK +"."+ fileContents.get(Attributes.TYPE))){
    		permalinkPattern = config.getString(Attributes.PERMALINK +"."+ fileContents.get(Attributes.TYPE));
    	}
    	if (Objects.nonNull(permalinkPattern) && !permalinkPattern.trim().isEmpty()) {
    		
    		String pattern = permalinkPattern;
    		if(pattern.startsWith(":")) pattern = separator+pattern;
    		String[] parts = pattern.split("/:");
    		List<String> pLink = new ArrayList<String>();
    		for (String part : parts){
    			part = part.trim().replace("/", "");
    			if (part.endsWith(":")){
    				pLink.add(part.replace(":", ""));
    			} else if(part.equalsIgnoreCase("filepath")) {
    				String path = FileUtil.asPath(fileContents.get(Attributes.FILE).toString()).replace(FileUtil.asPath( contentPath), "");
    				path = FilenameUtils.removeExtension(path);
    				// strip off leading / to enable generating non-root based sites
    		    	if (path.startsWith("/")) {
    		    		path = path.substring(1, path.length());
    		    	}
    				pLink.add(path);
    			} else if(part.equalsIgnoreCase("filename")) {
    				String sourcePath = (String) fileContents.get(Attributes.SOURCE_URI);
    				String fileName = FilenameUtils.getBaseName(sourcePath);
    				pLink.add(fileName);
    			} else if(fileContents.containsKey(part)){
    				Object value = fileContents.get(part);
    				if (value instanceof String){
    					pLink.add(value.toString());
    				} else if (value.getClass().equals(String[].class)){
    					pLink.addAll(Arrays.asList((String[])value));
    				}
    			} else if (Arrays.asList("YEAR","MONTH","DAY").contains(part.toUpperCase())) {
    				Date publishedDate = (Date) fileContents.get("date");
    				if(Objects.nonNull(publishedDate)){
    					String dateValue = null;
    					if(part.equalsIgnoreCase("YEAR")){
    						dateValue = DateFormatUtils.format(publishedDate, "yyyy");
    					}
    					if(part.equalsIgnoreCase("MONTH")){
    						dateValue = DateFormatUtils.format(publishedDate, "MM");
    					}
    					if(part.equalsIgnoreCase("DAY")){
    						dateValue = DateFormatUtils.format(publishedDate, "dd");
    					}
    					pLink.add(dateValue);
    				}
    			} 
    		}
    		
			permalink = String.join(separator, pLink);
			permalink = sanitize(permalink).concat(separator);
			String uri = permalink;
			boolean noExtensionUri = config.getBoolean(Keys.URI_NO_EXTENSION);
	    	if (noExtensionUri) {
	    		uri = uri + "index.html";
	    	} else {
	    		permalink = permalink.substring(0, permalink.length() -1 );
	    		permalink = permalink + config.getString(Keys.OUTPUT_EXTENSION);
	    		uri = permalink;
	    	}
	    	if(uri.startsWith("/")){
	    		uri = uri.substring(1);
	    	}
	    	fileContents.put(Attributes.URI, uri);
	    	
	    	//Calculate the root path based on the permalink
	    	File permaFile = new File(contentPath,uri);
	    	String rootPath = getPathToRoot(permaFile);
	    	fileContents.put(Attributes.ROOTPATH,rootPath);
        }
    		
    	
    	return permalink;
    }
    
    /**
     * Replace the spaces with hyphens
     * @return
     */
    private String sanitize(String input){
    	return input.replace(" ", "-");
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
            String[] tags = DBUtil.toStringArray(document.field(Attributes.TAGS));
            Collections.addAll(result, tags);
        }
        return result;
    }

    private DocumentStatus findDocumentStatus(String docType, String uri, String sha1) {
        List<ODocument> match = db.getDocumentStatus(docType, uri);
        if (!match.isEmpty()) {
            ODocument entries = match.get(0);
            String oldHash = entries.field(Attributes.SHA1);
            if (!(oldHash.equals(sha1)) || Boolean.FALSE.equals(entries.field(Attributes.RENDERED))) {
                return DocumentStatus.UPDATED;
            } else {
                return DocumentStatus.IDENTICAL;
            }
        } else {
            return DocumentStatus.NEW;
        }
    }
}
