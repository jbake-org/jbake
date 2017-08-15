package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.Crawler.Attributes;
import org.jbake.template.DelegatingTemplateEngine;
import org.jbake.util.PagingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Render output to a file.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Renderer {

    private interface RenderingConfig {

        File getPath();

        String getName();

        String getTemplate();

        Map<String, Object> getModel();
    }

    private static abstract class AbstractRenderingConfig implements RenderingConfig {

        protected final File path;
        protected final String name;
        protected final String template;

        public AbstractRenderingConfig(File path, String name, String template) {
            super();
            this.path = path;
            this.name = name;
            this.template = template;
        }

        @Override
        public File getPath() {
            return path;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTemplate() {
            return template;
        }

    }

    public class ModelRenderingConfig extends AbstractRenderingConfig {
        private final Map<String, Object> model;

        public ModelRenderingConfig(String fileName, Map<String, Object> model, String templateType) {
            super(new File(destination.getPath() + File.separator + fileName), fileName, findTemplateName(templateType));
            this.model = model;
        }

        public ModelRenderingConfig(File path, String name, Map<String, Object> model, String template) {
            super(path, name, template);
            this.model = model;
        }

        @Override
        public Map<String, Object> getModel() {
            return model;
        }
    }

    class DefaultRenderingConfig extends AbstractRenderingConfig {

        private final Object content;

        private DefaultRenderingConfig(File path, String allInOneName) {
            super(path, allInOneName, findTemplateName(allInOneName));
            this.content = buildSimpleModel(allInOneName);
        }

        public DefaultRenderingConfig(String filename, String allInOneName) {
            super(new File(destination.getPath() + File.separator + filename), allInOneName, findTemplateName(allInOneName));
            this.content = buildSimpleModel(allInOneName);
        }

        /**
         * Constructor added due to known use of a allInOneName which is used for name, template and content
         *
         * @param allInOneName
         */
        public DefaultRenderingConfig(String allInOneName) {
            this(new File(destination.getPath() + File.separator + allInOneName + config.getString(Keys.OUTPUT_EXTENSION)),
                    allInOneName);
        }

        @Override
        public Map<String, Object> getModel() {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("renderer", renderingEngine);
            model.put("content", content);

            if ( config.containsKey(Keys.PAGINATE_INDEX) && config.getBoolean(Keys.PAGINATE_INDEX) ) {
                model.put("numberOfPages", 0);
                model.put("currentPageNumber", 0);
                model.put("previousFileName", "");
                model.put("nextFileName", "");
            }

            return model;
        }

    }

    private final static Logger LOGGER = LoggerFactory.getLogger(Renderer.class);

    // TODO: should all content be made available to all templates via this class??

    private final File destination;
    private final CompositeConfiguration config;
    private final DelegatingTemplateEngine renderingEngine;
    private final ContentStore db;

    /**
     * Creates a new instance of Renderer with supplied references to folders.
     *
     * @param db            The database holding the content
     * @param destination   The destination folder
     * @param templatesPath The templates folder
     * @param config        Project configuration
     */
    public Renderer(ContentStore db, File destination, File templatesPath, CompositeConfiguration config) {
        this.destination = destination;
        this.config = config;
        this.renderingEngine = new DelegatingTemplateEngine(config, db, destination, templatesPath);
        this.db = db;
    }
    
    /**
     * Creates a new instance of Renderer with supplied references to folders and the instance of DelegatingTemplateEngine to use.
     *
     * @param db                The database holding the content
     * @param destination       The destination folder
     * @param templatesPath     The templates folder
     * @param config            Project configuration
     * @param renderingEngine   The instance of DelegatingTemplateEngine to use
     */
    public Renderer(ContentStore db, File destination, File templatesPath, CompositeConfiguration config, DelegatingTemplateEngine renderingEngine) {
        this.destination = destination;
        this.config = config;
        this.renderingEngine = renderingEngine;
        this.db = db;
    }

    private String findTemplateName(String docType) {
        String templateKey = "template." + docType + ".file";
        String returned = config.getString(templateKey);
        return returned;
    }

    /**
     * Render the supplied content to a file.
     *
     * @param content       The content to renderDocument
     * @throws Exception    if IOException or SecurityException are raised
     */
    public void render(Map<String, Object> content) throws Exception {
        String docType = (String) content.get(Crawler.Attributes.TYPE);
        String outputFilename = destination.getPath() + File.separatorChar + ((String)content.get(Attributes.URI)).replace("/",File.separator);
        if (outputFilename.lastIndexOf(".") > outputFilename.lastIndexOf(File.separatorChar)) {
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

        if (content.get(Crawler.Attributes.STATUS).equals(Crawler.Attributes.Status.DRAFT)) {
            outputFilename = outputFilename + config.getString(Keys.DRAFT_SUFFIX);
        }

        File outputFile = new File(outputFilename + FileUtil.findExtension(config, docType));
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
            throw new Exception("Failed to render file " + outputFile.getAbsolutePath() + ". Cause: " + e.getMessage(), e);
        }
    }

    private Writer createWriter(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        return new OutputStreamWriter(new FileOutputStream(file), config.getString(ConfigUtil.Keys.RENDER_ENCODING));
    }

    private void render(RenderingConfig renderConfig) throws Exception {
        File outputFile = renderConfig.getPath();
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering ").append(renderConfig.getName()).append(" [").append(outputFile).append("]...");

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(renderConfig.getModel(), renderConfig.getTemplate(), out);
            out.close();
            sb.append("done!");
            LOGGER.info(sb.toString());
        } catch (Exception e) {
            sb.append("failed!");
            LOGGER.error(sb.toString(), e);
            throw new Exception("Failed to render " + renderConfig.getName(), e);
        }
    }

    /**
     * Render an index file using the supplied content.
     *
     * @param indexFile The name of the output file
     * @throws Exception if IOException or SecurityException are raised
     */
    public void renderIndex(String indexFile) throws Exception {

        render(new DefaultRenderingConfig(indexFile, "masterindex"));
    }

    public void renderIndexPaging(String indexFile) throws Exception {
        long totalPosts = db.getPublishedCount("post");
        int postsPerPage = config.getInt(Keys.POSTS_PER_PAGE, 5);

        if (totalPosts == 0) {
            //paging makes no sense. render single index file instead
            renderIndex(indexFile);
        } else {
            PagingHelper pagingHelper = new PagingHelper(totalPosts, postsPerPage);

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("renderer", renderingEngine);
            model.put("numberOfPages", pagingHelper.getNumberOfPages());
            
            try {
                db.setLimit(postsPerPage);
                for (int pageStart = 0, page = 1; pageStart < totalPosts; pageStart += postsPerPage, page++) {
                    String fileName = indexFile;

                    db.setStart(pageStart);
                    model.put("currentPageNumber", page);
                    String previous = pagingHelper.getPreviousFileName(page, fileName);
                    model.put("previousFileName", previous);
                    String nextFileName = pagingHelper.getNextFileName(page, fileName);
                    model.put("nextFileName", nextFileName);
                    
                    Map<String, Object> contentModel =  buildSimpleModel("masterindex");
                    
                    if(page > 1){
                    	contentModel.put(Attributes.ROOTPATH, "../");
                    }
                    model.put("content", contentModel);

                    // Add page number to file name
                    fileName = pagingHelper.getCurrentFileName(page, fileName);
                    ModelRenderingConfig renderConfig = new ModelRenderingConfig(fileName, model, "masterindex");
                    render(renderConfig);
                }
                db.resetPagination();
            } catch (Exception e) {
                throw new Exception("Failed to render index. Cause: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Render an XML sitemap file using the supplied content.
     *
     * @param sitemapFile       configuration for site map
     * @throws Exception        if can't create correct default rendering config
     *
     * @see <a href="https://support.google.com/webmasters/answer/156184?hl=en&ref_topic=8476">About Sitemaps</a>
     * @see <a href="http://www.sitemaps.org/">Sitemap protocol</a>
     */
    public void renderSitemap(String sitemapFile) throws Exception {
        render(new DefaultRenderingConfig(sitemapFile, "sitemap"));
    }

    /**
     * Render an XML feed file using the supplied content.
     *
     * @param feedFile      The name of the output file
     * @throws Exception    if default rendering configuration is not loaded correctly
     */
    public void renderFeed(String feedFile) throws Exception {
        render(new DefaultRenderingConfig(feedFile, "feed"));
    }

    /**
     * Render an archive file using the supplied content.
     *
     * @param archiveFile   The name of the output file
     * @throws Exception    if default rendering configuration is not loaded correctly
     */
    public void renderArchive(String archiveFile) throws Exception {
        render(new DefaultRenderingConfig(archiveFile, "archive"));
    }

    /**
     * Render tag files using the supplied content.
     *
     * @param tagPath The output path
     * @return Number of rendered tags
     * @throws Exception if cannot render tags correctly
     */
    public int renderTags(String tagPath) throws Exception {
    	int renderedCount = 0;
    	final List<Throwable> errors = new LinkedList<Throwable>();
        for (String tag : db.getAllTags()) {
            try {
                Map<String, Object> model = new HashMap<String, Object>();
                model.put("renderer", renderingEngine);
                model.put(Attributes.TAG, tag);
                Map<String, Object> map = buildSimpleModel(Attributes.TAG);
                map.put(Attributes.ROOTPATH, "../");
                model.put("content", map);

            	File path = new File(destination.getPath() + File.separator + tagPath + File.separator + tag + config.getString(Keys.OUTPUT_EXTENSION));
            	render(new ModelRenderingConfig(path, Attributes.TAG, model, findTemplateName(Attributes.TAG)));
                renderedCount++;
            } catch (Exception e) {
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to render tags. Cause(s):");
            for (Throwable error : errors) {
                sb.append("\n").append(error.getMessage());
            }
            throw new Exception(sb.toString(), errors.get(0));
        } else {
            return renderedCount;
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
        content.put(Attributes.TYPE, type);
        content.put(Attributes.ROOTPATH, "");
        // add any more keys here that need to have a default value to prevent need to perform null check in templates
        return content;
    }
}
