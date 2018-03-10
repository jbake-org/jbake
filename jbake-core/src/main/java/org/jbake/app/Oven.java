package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Oven.class);

    private Utensils utensils;
    private List<Throwable> errors = new LinkedList<>();
    private int renderedCount = 0;

    /**
     * @param source       Project source directory
     * @param destination  The destination folder
     * @param isClearCache Should the cache be cleaned
     * @throws Exception if configuration is not loaded correctly
     * @deprecated Use {@link #Oven(JBakeConfiguration)} instead
     * Delegate c'tor to prevent API break for the moment.
     */
    @Deprecated
    public Oven(final File source, final File destination, final boolean isClearCache) throws Exception {
        this(new JBakeConfigurationFactory().createDefaultJbakeConfiguration(source, destination, isClearCache));
    }

    /**
     * @param source       Project source directory
     * @param destination  The destination folder
     * @param config       Project configuration
     * @param isClearCache Should the cache be cleaned
     * @throws Exception if configuration is not loaded correctly
     * @deprecated Use {@link #Oven(JBakeConfiguration)} instead
     * Creates a new instance of the Oven with references to the source and destination folders.
     */
    @Deprecated
    public Oven(final File source, final File destination, final CompositeConfiguration config, final boolean isClearCache) throws Exception {
        this(new JBakeConfigurationFactory().createDefaultJbakeConfiguration(source, destination, config, isClearCache));
    }

    /**
     * Create an Oven instance by a {@link JBakeConfiguration}
     * <p>
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
     * @param utensils All Utensils necessary to bake
     */
    public Oven(Utensils utensils) {
        checkConfiguration(utensils.getConfiguration());
        this.utensils = utensils;
    }

    @Deprecated
    public CompositeConfiguration getConfig() {
        return ((DefaultJBakeConfiguration) utensils.getConfiguration()).getCompositeConfiguration();
    }

    // TODO: do we want to use this. Else, config could be final
    @Deprecated
    public void setConfig(final CompositeConfiguration config) {
        ((DefaultJBakeConfiguration) utensils.getConfiguration()).setCompositeConfiguration(config);
    }

    /**
     * Checks source path contains required sub-folders (i.e. templates) and setups up variables for them.
     *
     * @deprecated There is no need for this method anymore. Validation is now part of the instantiation.
     * Can be removed with 3.0.0.
     */
    @Deprecated
    public void setupPaths() {
        /* nothing to do here */
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
            contentStore.updateAndClearCacheIfNeeded(config.getClearCache(), config.getTemplateFolder());

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
            if (!errors.isEmpty()) {
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
        return new ArrayList<>(errors);
    }

    public Utensils getUtensils() {
        return utensils;
    }
}
