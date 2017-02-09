package org.jbake.app;

import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;
import org.jbake.app.configuration.JBakeConfigurationInspector;
import org.jbake.model.DocumentTypes;
import org.jbake.render.RenderingTool;
import org.jbake.template.ModelExtractors;
import org.jbake.template.ModelExtractorsDocumentTypeListener;
import org.jbake.template.RenderingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * All the baking happens in the Oven!
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Oven {

    private final static Logger LOGGER = LoggerFactory.getLogger(Oven.class);

    private Utensils utensils;
	private List<Throwable> errors = new LinkedList<Throwable>();
	private int renderedCount = 0;

    /**
     * Delegate c'tor to prevent API break for the moment.
     *
     * @param source                   Project source directory
     * @param destination              The destination folder
     * @param isClearCache             Should the cache be cleaned
     * @throws ConfigurationException  if configuration is not loaded correctly
     */
    public Oven(final File source, final File destination, final boolean isClearCache) throws ConfigurationException {
        this( JBakeConfigurationFactory.createDefaultJbakeConfiguration(source, destination, isClearCache) );
    }

    /**
     * Create an Oven instance by a {@link JBakeConfiguration}
     *
     * It creates default {@link Utensils} needed to bake sites.
     *
     * @param config The project configuration. see {@link JBakeConfiguration}
     */
    public Oven(JBakeConfiguration config) {
        this.utensils = UtensilsFactory.createDefaultUtensils(config);
    }

    /**
     * Create an Oven instance with given {@link Utensils}
     *
     * @param utensils
     */
    public Oven(Utensils utensils) {
        checkConfiguration(utensils.getConfiguration());
        this.utensils = utensils;
    }

    /**
     * Checks source path contains required sub-folders (i.e. templates) and setups up variables for them.
     * Creates destination folder if it does not exist.
     *
     * @throws JBakeException If template or contents folder don't exist
     */
    private void checkConfiguration(JBakeConfiguration configuration) {
        JBakeConfigurationInspector inspector = new JBakeConfigurationInspector(configuration);
        inspector.inspect();
    }

	/**
	 * All the good stuff happens in here...
	 */
    public void bake() {

        ContentStore contentStore = utensils.getContentStore();
        JBakeConfiguration config = utensils.getConfiguration();
        Crawler crawler = utensils.getCrawler();
        Asset asset = utensils.getAsset();

        try {

            final long start = new Date().getTime();
            LOGGER.info("Baking has started...");
            contentStore.startup();
            updateDocTypesFromConfiguration();
            contentStore.updateSchema();
            contentStore.clearCacheIfNeeded(config.getClearCache(), config.getTemplateFolder());

            // process source content
            crawler.crawl();

            // render content
            renderContent();

            // copy assets
            asset.copy();
            asset.copyAssetsFromContent(config.getContentFolder());

            errors.addAll(asset.getErrors());

            LOGGER.info("Baking finished!");
            long end = new Date().getTime();
            LOGGER.info("Baked {} items in {}ms", renderedCount, end - start);
            if (errors.size() > 0) {
                LOGGER.error("Failed to bake {} item(s)!", errors.size());
            }
        } finally {
            contentStore.close();
            contentStore.shutdown();
        }
    }

    /**
     * Iterates over the configuration, searching for keys like "template.index.file=..."
     * in order to register new document types.
     */
    private void updateDocTypesFromConfiguration() {
        resetDocumentTypesAndExtractors();
        JBakeConfiguration config = utensils.getConfiguration();

        ModelExtractorsDocumentTypeListener listener = new ModelExtractorsDocumentTypeListener();
        DocumentTypes.addListener(listener);

        for (String docType : config.getDocumentTypes()) {
            DocumentTypes.addDocumentType(docType);
        }
    }

    private void resetDocumentTypesAndExtractors() {
        DocumentTypes.resetDocumentTypes();
        ModelExtractors.getInstance().reset();
    }

    /**
     * Load {@link RenderingTool} instances and delegate rendering of documents to them
     */
    private void renderContent() {
        JBakeConfiguration config = utensils.getConfiguration();
        Renderer renderer = utensils.getRenderer();
        ContentStore contentStore = utensils.getContentStore();

        for (RenderingTool tool : ServiceLoader.load(RenderingTool.class)) {
            try {
                renderedCount += tool.render(renderer, contentStore, config);
            } catch (RenderingException e) {
                errors.add(e);
            }
        }
    }


	public List<Throwable> getErrors() {
		return new ArrayList<Throwable>(errors);
	}

    public Utensils getUtensils() {
        return utensils;
    }
}
