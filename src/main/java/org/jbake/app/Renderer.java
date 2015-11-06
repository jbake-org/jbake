package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.template.DelegatingTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

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
    public Renderer(ContentStore db, File destination, File templatesPath, CompositeConfiguration config) {
        this.destination = destination;
        this.config = config;
        this.renderingEngine = new DelegatingTemplateEngine(config, db, destination, templatesPath);
    }

    private String findTemplateName(String docType) {
        return config.getString("template." + docType + ".file");
    }

    /**
     * Render the supplied content to a file.
     *
     * @param content The content to renderDocument
     * @throws Exception
     */
    public void render(Map<String, Object> content) throws Exception {
        String docType = (String) content.get("type");
        String outputFilename = destination.getPath() + File.separatorChar + content.get("uri");
        if (outputFilename.lastIndexOf(".") > 0) {
            outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf("."));
        }

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

        File outputFile = new File(outputFilename + FileUtil.findExtension(config, docType));
        render(outputFile, docType, content);
    }

    /**
     * Render an index file using the supplied content.
     *
     * @param indexFile The name of the output file
     * @throws Exception
     */
    public void renderIndex(String indexFile) throws Exception {
        render(indexFile, "masterindex");
    }

    /**
     * Render an XML sitemap file using the supplied content.
     *
     * @throws Exception
     * @see <a href="https://support.google.com/webmasters/answer/156184?hl=en&ref_topic=8476">About Sitemaps</a>
     * @see <a href="http://www.sitemaps.org/">Sitemap protocol</a>
     */
    public void renderSitemap(String sitemapFile) throws Exception {
        render(sitemapFile, "sitemap");
    }

    /**
     * Render an XML feed file using the supplied content.
     *
     * @param feedFile The name of the output file
     * @throws Exception
     */
    public void renderFeed(String feedFile) throws Exception {
        render(feedFile, "feed");
    }

    /**
     * Render an archive file using the supplied content.
     *
     * @param archiveFile The name of the output file
     * @throws Exception
     */
    public void renderArchive(String archiveFile) throws Exception {
        render(archiveFile, "archive");
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

            File outputFile = new File(destination.getPath() + File.separator + tagPath + File.separator + tag + config.getString(Keys.OUTPUT_EXTENSION));
            StringBuilder sb = new StringBuilder();
            sb.append("Rendering tags [").append(outputFile).append("]... ");

            try {
                write(outputFile, model, "tag");
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
            for (String error : errors) {
                sb.append("\n" + error);
            }
            throw new Exception(sb.toString());
        }
    }

    /**
     * Builds simple map of values, which are exposed when rendering index/archive/sitemap/feed/tags.
     */
    private Map<String, Object> buildSimpleModel(String type) {
        Map<String, Object> content = new HashMap<String, Object>();
        content.put("type", type);
        content.put("rootpath", "");
        // add any more keys here that need to have a default value to prevent need to perform null check in templates
        return content;
    }

    private void render(String fileName, String type) throws Exception {
        render(new File(destination.getPath() + File.separator + fileName), type, buildSimpleModel(type));
    }

    private void render(File outputFile, String type, Map<String, Object> content) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering ").append(type).append(" [").append(outputFile).append("]... ");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("renderer", renderingEngine);
        model.put("content", content);

        try {
            write(outputFile, model, type);
            sb.append("done!");
            LOGGER.info(sb.toString());
        } catch (Exception e) {
            sb.append("failed!");
            LOGGER.error(sb.toString(), e);
            throw new Exception("Failed to render " + type + ". Cause: " + e.getMessage());
        }
    }

    private void write(File outputFile, Map<String, Object> model, String type) throws Exception {
        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
        }

        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), config.getString(Keys.RENDER_ENCODING));
        renderingEngine.renderDocument(model, findTemplateName(type), out);
        out.close();
    }
}
