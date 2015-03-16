package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.template.DelegatingTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Render output to a file.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Renderer {

    private final static Logger LOGGER = LoggerFactory.getLogger(Renderer.class);

    // TODO: should all content be made available to all templates via this class??

    private final File destination;
    private final CompositeConfiguration config;
    private final DelegatingTemplateEngine renderingEngine;

    /**
     * Creates a new instance of Renderer with supplied references to folders.
     *
     * @param db            The database holding the content
     * @param destination   The destination folder
     * @param templatesPath The templates folder
     * @param config        
     */
    public Renderer(ContentStore db, File destination, File templatesPath, CompositeConfiguration config) {
        this.destination = destination;
        this.config = config;
        this.renderingEngine = new DelegatingTemplateEngine(config, db, destination, templatesPath);
    }

    private String findTemplateName(String docType) {
        return config.getString("template."+docType+".file");
    }

    /**
     * Render the supplied content to a file.
     *
     * @param content The content to renderDocument
     * @throws Exception
     */
    public void render(Map<String, Object> content) throws Exception {
    	String docType = (String) content.get("type");
        String outputFilename = destination.getPath() + File.separatorChar + (String) content.get("uri");
        outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf("."));

        // delete existing versions if they exist in case status has changed either way
        File draftFile = new File(outputFilename + config.getString(Keys.DRAFT_SUFFIX) + FileUtil.findExtension(config, docType));
        if (draftFile.exists()) {
            draftFile.delete();
        }

        File publishedFile = new File(outputFilename + FileUtil.findExtension(config, docType));
        if (publishedFile.exists()) {
            publishedFile.delete();
        }

        if (content.get("status").equals("draft")) {
            outputFilename = outputFilename + config.getString(Keys.DRAFT_SUFFIX);
        }

        File outputFile = new File(outputFilename + FileUtil.findExtension(config,docType));
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering [").append(outputFile).append("]... ");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("content", content);
        model.put("renderer", renderingEngine);

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(model, findTemplateName(docType), out);
            out.close();
            sb.append("done!");
            LOGGER.info(sb.toString());
        } catch (Exception e) {
            sb.append("failed!");
            LOGGER.error(sb.toString(), e);
            throw new Exception("Failed to render file. Cause: " + e.getMessage());
        }
    }

    private Writer createWriter(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        return new OutputStreamWriter(new FileOutputStream(file), config.getString(ConfigUtil.Keys.RENDER_ENCODING));
    }

    /**
     * Render an index file using the supplied content.
     *
     * @param indexFile The name of the output file
     * @throws Exception 
     */
    public void renderIndex(final String indexFile) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering index [").append(indexFile).append("]...");

        ContentStore db = DBUtil.createDataStore(config.getString(Keys.DB_STORE), config.getString(Keys.DB_PATH));
        long totalPosts = db.countClass("post");
        boolean paginate = config.getBoolean(Keys.PAGINATE_INDEX, false);
        int postsPerPage = config.getInt(Keys.POSTS_PER_PAGE, -1);
        int start = 0;

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("renderer", renderingEngine);
        model.put("content", buildSimpleModel("index"));
        if (paginate) {
            db.setLimit(postsPerPage);
        }

        try {
            int page = 1;
            while (start < totalPosts) {
                String fileName = indexFile;

                if (paginate) {
                    db.setStart(start);
                    int index = fileName.lastIndexOf(".");
                    if (page != 1) {
                        int prevPage = page - 1;
                        String previous = fileName.substring(0, index) + 
                                (prevPage != 1 ? page-1 : "") +
                                fileName.substring(index);
                        model.put("previousFileName", previous);
                    } else {
                        model.remove("previousFileName");
                    }
                    
                    // If this iteration won't consume the remaining posts, calculate
                    // the next file name
                    if ((start + postsPerPage) < totalPosts) {
                        model.put("nextFileName", fileName.substring(0, index) + (page+1) +
                            fileName.substring(index));
                    } else {
                        model.remove("nextFileName");
                    }
                    // Add page number to file name
                    fileName = fileName.substring(0, index) + (page > 1 ? page : "") +
                            fileName.substring(index);
                }
                
                Writer out = createWriter(new File(destination.getPath() + File.separator + fileName));
                renderingEngine.renderDocument(model, findTemplateName("index"), out);
                out.close();
                LOGGER.info(sb.toString());
                
                if (paginate) {
                    start += postsPerPage;
                    page++;
                } else {
                    break; // TODO: eww
                }
            }
            sb.append("done!");
        } catch (Exception e) {
            sb.append("failed!");
            LOGGER.error(sb.toString(), e);
            throw new Exception("Failed to render index. Cause: " + e.getMessage());
        }
    }

    /**
     * Render an XML sitemap file using the supplied content.
     * @throws Exception 
     *
     * @see <a href="https://support.google.com/webmasters/answer/156184?hl=en&ref_topic=8476">About Sitemaps</a>
     * @see <a href="http://www.sitemaps.org/">Sitemap protocol</a>
     */
    public void renderSitemap(String sitemapFile) throws Exception {
        File outputFile = new File(destination.getPath() + File.separator + sitemapFile);
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering sitemap [").append(outputFile).append("]... ");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("renderer", renderingEngine);
        model.put("content", buildSimpleModel("sitemap"));

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(model, findTemplateName("sitemap"), out);
            sb.append("done!");
            out.close();
            LOGGER.info(sb.toString());
        } catch (Exception e) {
            sb.append("failed!");
            LOGGER.error(sb.toString(), e);
            throw new Exception("Failed to render sitemap. Cause: " + e.getMessage());
        }
    }

    /**
     * Render an XML feed file using the supplied content.
     *
     * @param feedFile The name of the output file
     * @throws Exception 
     */
    public void renderFeed(String feedFile) throws Exception {
        File outputFile = new File(destination.getPath() + File.separator + feedFile);
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering feed [").append(outputFile).append("]... ");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("renderer", renderingEngine);
        model.put("content", buildSimpleModel("feed"));

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(model, findTemplateName("feed"), out);
            out.close();
            sb.append("done!");
            LOGGER.info(sb.toString());
        } catch (Exception e) {
            sb.append("failed!");
            LOGGER.error(sb.toString(), e);
            throw new Exception("Failed to render feed. Cause: " + e.getMessage());
        }
    }

    /**
     * Render an archive file using the supplied content.
     *
     * @param archiveFile The name of the output file
     * @throws Exception 
     */
    public void renderArchive(String archiveFile) throws Exception {
        File outputFile = new File(destination.getPath() + File.separator + archiveFile);
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering archive [").append(outputFile).append("]... ");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("renderer", renderingEngine);
        model.put("content", buildSimpleModel("archive"));

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(model, findTemplateName("archive"), out);
            out.close();
            sb.append("done!");
            LOGGER.info(sb.toString());
        } catch (Exception e) {
            sb.append("failed!");
            LOGGER.error(sb.toString(), e);
            throw new Exception("Failed to render archive. Cause: " + e.getMessage());
        }
    }

    /**
     * Render tag files using the supplied content.
     *
     * @param tags    The content to renderDocument
     * @param tagPath The output path
     * @throws Exception 
     */
    public void renderTags(Set<String> tags, String tagPath) throws Exception {
    	final List<String> errors = new LinkedList<String>();
        for (String tag : tags) {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("renderer", renderingEngine);
            model.put("tag", tag);
            Map<String, Object> map = buildSimpleModel("tag");
            map.put("rootpath", "../");
            model.put("content", map);

            tag = tag.trim().replace(" ", "-");
            File outputFile = new File(destination.getPath() + File.separator + tagPath + File.separator + tag + config.getString(Keys.OUTPUT_EXTENSION));
            StringBuilder sb = new StringBuilder();
            sb.append("Rendering tags [").append(outputFile).append("]... ");

            try {
                Writer out = createWriter(outputFile);
                renderingEngine.renderDocument(model, findTemplateName("tag"), out);
                out.close();
                sb.append("done!");
                LOGGER.info(sb.toString());
            } catch (Exception e) {
                sb.append("failed!");
                LOGGER.error(sb.toString(), e);
                errors.add(e.getMessage());
            }
        }
        if (!errors.isEmpty()) {
        	StringBuilder sb = new StringBuilder();
        	sb.append("Failed to render tags. Cause(s):");
        	for(String error: errors) {
        		sb.append("\n" + error);
        	}
        	throw new Exception(sb.toString());
        }
    }
    
    /**
     * Builds simple map of values, which are exposed when rendering index/archive/sitemap/feed/tags.
     * 
     * @param type
     * @return
     */
    private Map<String, Object> buildSimpleModel(String type) {
    	Map<String, Object> content = new HashMap<String, Object>();
    	content.put("type", type);
    	content.put("rootpath", "");
    	// add any more keys here that need to have a default value to prevent need to perform null check in templates
    	return content;
    }
}
