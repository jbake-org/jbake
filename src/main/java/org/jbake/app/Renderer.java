package org.jbake.app;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.apache.commons.configuration.CompositeConfiguration;
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

    private File destination;
    private CompositeConfiguration config;
    private final DelegatingTemplateEngine renderingEngine;

    /**
     * Creates a new instance of Renderer with supplied references to folders.
     *
     * @param destination   The destination folder
     * @param templatesPath The templates folder
     */
    public Renderer(ODatabaseDocumentTx db, File destination, File templatesPath, CompositeConfiguration config) {
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
//		String outputFilename = (new File((String)content.get("file")).getPath().replace(source.getPath()+File.separator+"content", destination.getPath()));
        String outputFilename = destination.getPath() + (String) content.get("uri");
        outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf("."));

        // delete existing versions if they exist in case status has changed either way
        File draftFile = new File(outputFilename + config.getString("draft.suffix") + config.getString("output.extension"));
        if (draftFile.exists()) {
            draftFile.delete();
        }
        File publishedFile = new File(outputFilename + config.getString("output.extension"));
        if (publishedFile.exists()) {
            publishedFile.delete();
        }

        if (content.get("status").equals("draft")) {
            outputFilename = outputFilename + config.getString("draft.suffix");
        }
        File outputFile = new File(outputFilename + config.getString("output.extension"));

        StringBuilder sb = new StringBuilder();
        sb.append("Rendering [").append(outputFile).append("]... ");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("content", content);
        model.put("renderer", renderingEngine);

        try {
            String docType = (String) content.get("type");
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

        return new OutputStreamWriter(new FileOutputStream(file), config.getString("render.encoding"));
    }

    /**
     * Render an index file using the supplied content.
     *
     * @param indexFile The name of the output file
     * @throws Exception 
     */
    public void renderIndex(String indexFile) throws Exception {
        File outputFile = new File(destination.getPath() + File.separator + indexFile);
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering index [").append(outputFile).append("]...");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("renderer", renderingEngine);

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(model, findTemplateName("index"), out);
            out.close();
            sb.append("done!");
            LOGGER.info(sb.toString());
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

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(model, findTemplateName("sitemap"), out);
            sb.append("done!");
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

            tag = tag.trim().replace(" ", "-");
            File outputFile = new File(destination.getPath() + File.separator + tagPath + File.separator + tag + config.getString("output.extension"));
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
}
