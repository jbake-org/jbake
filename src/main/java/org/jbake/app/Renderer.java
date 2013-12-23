package org.jbake.app;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.renderer.FreemarkerRenderer;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Render output to a file.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Renderer {

    // TODO: should all content be made available to all templates via this class??

    private File destination;
    private CompositeConfiguration config;
    private final FreemarkerRenderer renderingEngine;

    /**
     * Creates a new instance of Renderer with supplied references to folders.
     *
     * @param destination   The destination folder
     * @param templatesPath The templates folder
     */
    public Renderer(ODatabaseDocumentTx db, File destination, File templatesPath, CompositeConfiguration config) {
        this.destination = destination;
        this.config = config;
        this.renderingEngine = new FreemarkerRenderer(config, db, destination, templatesPath);
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

        System.out.print("Rendering [" + outputFile + "]... ");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("content", content);

        try {
            renderingEngine.renderDocument(model, ((String) content.get("type")) + ".ftl", outputFile);
            System.out.println("done!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed!");
            throw new Exception("Failed to render file");
        }
    }

    /**
     * Render an index file using the supplied content.
     *
     * @param indexFile The name of the output file
     */
    public void renderIndex(String indexFile) {
        File outputFile = new File(destination.getPath() + File.separator + indexFile);
        System.out.print("Rendering index [" + outputFile + "]... ");
        Map<String, Object> model = new HashMap<String, Object>();

        try {
            renderingEngine.renderDocument(model, config.getString("template.index.file"), outputFile);
            System.out.println("done!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed!");
        }
    }

    /**
     * Render an XML sitemap file using the supplied content.
     *
     * @see <a href="https://support.google.com/webmasters/answer/156184?hl=en&ref_topic=8476">About Sitemaps</a>
     * @see <a href="http://www.sitemaps.org/">Sitemap protocol</a>
     */
    public void renderSitemap(String sitemapFile) {
        File outputFile = new File(destination.getPath() + File.separator + sitemapFile);
        System.out.print("Rendering sitemap [" + outputFile + "]... ");

        Map<String, Object> model = new HashMap<String, Object>();

        try {
            render(model, config.getString("template.sitemap.file"), outputFile);
            System.out.println("done!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed!");
        }
    }

    /**
     * Render an XML feed file using the supplied content.
     *
     * @param feedFile The name of the output file
     */
    public void renderFeed(String feedFile) {
        File outputFile = new File(destination.getPath() + File.separator + feedFile);
        System.out.print("Rendering feed [" + outputFile + "]... ");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("published_date", new Date());

        try {
            renderingEngine.renderDocument(model, config.getString("template.feed.file"), outputFile);
            System.out.println("done!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed!");
        }
    }

    /**
     * Render an archive file using the supplied content.
     *
     * @param archiveFile The name of the output file
     */
    public void renderArchive(String archiveFile) {
        File outputFile = new File(destination.getPath() + File.separator + archiveFile);
        System.out.print("Rendering archive [" + outputFile + "]... ");
        Map<String, Object> model = new HashMap<String, Object>();

        try {
            renderingEngine.renderDocument(model, config.getString("template.archive.file"), outputFile);
            System.out.println("done!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed!");
        }
    }

    /**
     * Render tag files using the supplied content.
     *
     * @param tags    The content to renderDocument
     * @param tagPath The output path
     */
    public void renderTags(Map<String, DocumentIterator> tags, String tagPath) {
        for (String tag : tags.keySet()) {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("tag", tag);
            // TODO: sort posts here
            DocumentIterator posts = tags.get(tag);
            model.put("tag_posts", posts);

            tag = tag.trim().replace(" ", "-");
            File outputFile = new File(destination.getPath() + File.separator + tagPath + File.separator + tag + config.getString("output.extension"));
            System.out.print("Rendering tags [" + outputFile + "]... ");

            try {
                renderingEngine.renderDocument(model, config.getString("template.tag.file"), outputFile);
                System.out.println("done!");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed!");
            }
        }
    }

}
