package org.jbake.app

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.lang3.LocaleUtils
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.app.configuration.JBakeConfigurationInspector
import org.jbake.model.DocumentTypes
import org.jbake.render.RenderingTool
import org.jbake.template.ModelExtractors
import org.jbake.template.ModelExtractorsDocumentTypeListener
import org.jbake.template.RenderingException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

/**
 * All the baking happens in the Oven!
 *
 * @author Jonathan Bullock [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
 */
class Oven {
    @JvmField
    val utensils: Utensils
    private val errors: MutableList<Throwable?> = LinkedList<Throwable?>()
    private var renderedCount = 0

    /**
     * @param source       Project source directory
     * @param destination  The destination folder
     * @param isClearCache Should the cache be cleaned
     * @throws Exception if configuration is not loaded correctly
     */
    @Deprecated(
        """Use {@link #Oven(JBakeConfiguration)} instead
      Delegate c'tor to prevent API break for the moment."""
    )
    constructor(
        source: File,
        destination: File?,
        isClearCache: Boolean
    ) : this(JBakeConfigurationFactory().createDefaultJbakeConfiguration(source, destination, isClearCache))

    /**
     * @param source       Project source directory
     * @param destination  The destination folder
     * @param config       Project configuration
     * @param isClearCache Should the cache be cleaned
     * @throws Exception if configuration is not loaded correctly
     */
    @Deprecated(
        """Use {@link #Oven(JBakeConfiguration)} instead
      Creates a new instance of the Oven with references to the source and destination folders."""
    )
    constructor(source: File?, destination: File?, config: CompositeConfiguration?, isClearCache: Boolean) : this(
        JBakeConfigurationFactory().createDefaultJbakeConfiguration(source, destination, config, isClearCache)
    )

    /**
     * Create an Oven instance by a [JBakeConfiguration]
     *
     *
     * It creates default [Utensils] needed to bake sites.
     *
     * @param config The project configuration. see [JBakeConfiguration]
     */
    constructor(config: JBakeConfiguration) {
        this.utensils = UtensilsFactory.createDefaultUtensils(config)
    }

    /**
     * Create an Oven instance with given [Utensils]
     *
     * @param utensils All Utensils necessary to bake
     */
    constructor(utensils: Utensils) {
        checkConfiguration(utensils.configuration)
        this.utensils = utensils
    }

    @get:Deprecated("")
    @set:Deprecated("")
    var config: CompositeConfiguration
        get() = (utensils.configuration as DefaultJBakeConfiguration).compositeConfiguration
        // TODO: do we want to use this. Else, config could be final
        set(config) {
            (utensils.configuration as DefaultJBakeConfiguration).compositeConfiguration = config
        }

    /**
     * Checks source path contains required sub-folders (i.e. templates) and setups up variables for them.
     *
     */
    @Deprecated(
        """There is no need for this method anymore. Validation is now part of the instantiation.
      Can be removed with 3.0.0."""
    )
    fun setupPaths() {
        /* nothing to do here */
    }

    /**
     * Checks source path contains required sub-folders (i.e. templates) and setups up variables for them.
     * Creates destination folder if it does not exist.
     *
     * @throws JBakeException If template or contents folder don't exist
     */
    private fun checkConfiguration(configuration: JBakeConfiguration) {
        val inspector = JBakeConfigurationInspector(configuration)
        inspector.inspect()
    }

    /**
     * Sets the Locale for the JVM
     *
     */
    private fun setLocale() {
        val localeString = this.utensils.configuration.jvmLocale
        val locale = if (localeString != null) LocaleUtils.toLocale(localeString) else Locale.getDefault()
        Locale.setDefault(locale)
    }

    /**
     * Responsible for incremental baking, typically a single file at a time.
     *
     * @param fileToBake The file to bake
     */
    fun bake(fileToBake: File) {
        val asset = utensils.asset
        if (asset.isAssetFile(fileToBake)) {
            LOGGER.info("Baking a change to an asset [" + fileToBake.getPath() + "]")
            asset.copySingleFile(fileToBake)
        } else {
            LOGGER.info("Playing it safe and running a full bake...")
            bake()
        }
    }

    /**
     * All the good stuff happens in here...
     */
    fun bake() {
        val contentStore = utensils.contentStore
        val config = utensils.configuration
        val crawler = utensils.crawler
        val asset = utensils.asset
        setLocale()

        try {
            val start = Date().getTime()
            LOGGER.info("Baking has started...")
            contentStore.startup()
            updateDocTypesFromConfiguration()
            contentStore.updateSchema()
            contentStore.updateAndClearCacheIfNeeded(config.clearCache, config.templateFolder)

            // process source content
            crawler.crawl()

            // process data files
            crawler.crawlDataFiles()

            // render content
            renderContent()

            // copy assets
            asset.copy()
            asset.copyAssetsFromContent(config.contentFolder)

            errors.addAll(asset.getErrors())

            LOGGER.info("Baking finished!")
            val end = Date().getTime()
            LOGGER.info("Baked {} items in {}ms", renderedCount, end - start)
            if (!errors.isEmpty()) {
                LOGGER.error("Failed to bake {} item(s)!", errors.size)
            }
        } finally {
            contentStore.close()
            contentStore.shutdown()
        }
    }

    /**
     * Iterates over the configuration, searching for keys like "template.index.file=..."
     * in order to register new document types.
     */
    private fun updateDocTypesFromConfiguration() {
        resetDocumentTypesAndExtractors()
        val config = utensils.configuration

        val listener = ModelExtractorsDocumentTypeListener()
        DocumentTypes.addListener(listener)

        for (docType in config.documentTypes) {
            DocumentTypes.addDocumentType(docType)
        }

        // needs manually setting as this isn't defined in same way as document types for content files
        DocumentTypes.addDocumentType(config.dataFileDocType)
    }

    private fun resetDocumentTypesAndExtractors() {
        DocumentTypes.resetDocumentTypes()
        ModelExtractors.Loader.instance.reset()
    }

    /**
     * Load [RenderingTool] instances and delegate rendering of documents to them
     */
    private fun renderContent() {
        val config = utensils.configuration
        val renderer = utensils.renderer
        val contentStore = utensils.contentStore

        for (tool in ServiceLoader.load<RenderingTool>(RenderingTool::class.java)) {
            try {
                renderedCount += tool.render(renderer, contentStore, config)
            } catch (e: RenderingException) {
                errors.add(e)
            }
        }
    }

    fun getErrors(): MutableList<Throwable?> {
        return ArrayList<Throwable?>(errors)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Oven::class.java)
    }
}
