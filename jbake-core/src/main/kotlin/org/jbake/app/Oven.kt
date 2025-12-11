package org.jbake.app

import org.apache.commons.lang3.LocaleUtils
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.app.configuration.JBakeConfigurationInspector
import org.jbake.model.DocumentTypeRegistry
import org.jbake.model.ModelExtractorsDocumentTypeListener
import org.jbake.render.RenderingTool
import org.jbake.template.ModelExtractorsRegistry
import org.jbake.util.instantNowMs
import org.jbake.util.logger
import org.slf4j.Logger
import java.io.File
import java.time.Instant
import java.util.*
import java.util.ServiceLoader.load

/**
 * All the baking happens in the Oven!
 */
class Oven {

    @JvmField
    val utensils: Utensils
    val errors: MutableList<Throwable> = LinkedList<Throwable>()
    private var renderedCount = 0

    /**
     * @param source       Project source directory
     * @param isClearCache Should the cache be cleaned
     * @throws Exception if configuration is not loaded correctly
     */
    @Deprecated("""Use {@link #Oven(JBakeConfiguration)} instead
      Delegate c'tor to prevent API break for the moment.""")
    constructor(
        source: File,
        destination: File,
        isClearCache: Boolean
    ) : this(JBakeConfigurationFactory().createDefaultJbakeConfiguration(source, destination, isClearCache))

    /**
     * Create an Oven instance by a [JBakeConfiguration] It creates default [Utensils] needed to bake sites.
     * TODO: Refactor - weird that this one creates default utensils, while the other ctor takes utensils with .configuration.
     */
    constructor(config: JBakeConfiguration) {
        this.utensils = UtensilsFactory.createDefaultUtensils(config)
    }

    /**
     * Create an Oven instance with given [Utensils]
     *
     * @param utensils All Utensils necessary to bake.
     */
    constructor(utensils: Utensils) {
        JBakeConfigurationInspector(utensils.configuration).inspect()
        this.utensils = utensils
    }



    /**
     * Bake a single file. If the file is an asset, only copy that file. Otherwise, run a full bake.
     */
    fun bakeSingleFile(fileToBake: File) {

        if (!utensils.asset.isAssetFile(fileToBake)) {
            log.info("Not an asset, running a full bake...")
            bakeEverything()
            return
        }

        log.info("Baking a change to an asset [" + fileToBake.path + "]")
        utensils.asset.copySingleFile(fileToBake)
    }

    /**
     * All the good stuff happens in here...
     */
    fun bakeEverything() {
        val newLocale = utensils.configuration.jvmLocale?.let { LocaleUtils.toLocale(it) }
        Locale.setDefault(newLocale ?: Locale.getDefault())

        try {
            val start = instantNowMs()
            log.info("Baking has started at $start. Starting ContentStore...")

            utensils.contentStore.startup()

            val config = utensils.configuration
            updateDocTypesFromConfiguration()
            utensils.contentStore.updateSchema()
            utensils.contentStore.updateAndClearCacheIfNeeded(config.clearCache, config.templateDir)

            // Process source content.
            utensils.crawler.crawlContentDirectory()

            // Process data files.
            utensils.crawler.crawlDataFiles()

            // Render content.
            for (tool in load(RenderingTool::class.java)) {
                try {
                    renderedCount += tool.render(utensils.renderer, utensils.contentStore, utensils.configuration)
                }
                catch (e: RenderingException) { errors.add(e) }
                catch (e: Exception) { errors.add(RenderingException("Error rendering with ${tool.javaClass.simpleName}: ${e.message}", e)) }
            }

            // Copy assets.
            utensils.asset.copy()
            utensils.asset.copyAssetsFromContent(config.contentDir)

            errors.addAll(utensils.asset.errors)

            log.info("Baking finished!")
            val durationMs = java.time.Duration.between(start, Instant.now()).toMillis()
            log.info("Baked $renderedCount items in $durationMs ms.")

            if (!errors.isEmpty())
                log.error("Failed to bake ${errors.size} item(s)!")
        }
        finally {
            utensils.contentStore.close()
            utensils.contentStore.shutdown()
        }
    }

    /**
     * Iterates over the configuration, searching for keys like "template.index.file=..."
     * in order to register new document types.
     */
    private fun updateDocTypesFromConfiguration() {
        DocumentTypeRegistry.resetDocumentTypes()
        ModelExtractorsRegistry.instance.reset()

        DocumentTypeRegistry.addListener(ModelExtractorsDocumentTypeListener())

        for (docType in utensils.configuration.documentTypes) {
            DocumentTypeRegistry.addDocumentType(docType)
        }

        // Needs to be set manually as this isn't defined in same way as document types for content files.
        DocumentTypeRegistry.addDocumentType(utensils.configuration.dataFileDocType)
    }

}

private val log: Logger by logger<Oven>()
