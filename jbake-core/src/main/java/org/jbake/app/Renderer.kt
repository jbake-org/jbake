package org.jbake.app

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.jbake.template.DelegatingTemplateEngine
import org.jbake.template.model.RenderContext
import org.jbake.template.model.TemplateModel
import org.jbake.util.PagingHelper
import org.jbake.util.PathConstants.fS
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.file.Files
import java.util.*

/**
 * Render output to a file.
 */
class Renderer {
    private val config: JBakeConfiguration
    private val renderingEngine: DelegatingTemplateEngine
    private val db: ContentStore

    /**
     * @param templatesPath The templates folder
     */
    @Deprecated("""Use {@link #Renderer(ContentStore, JBakeConfiguration)} instead.
      Creates a new instance of Renderer with supplied references to folders.""")
    constructor(db: ContentStore, destination: File, templatesPath: File, config: CompositeConfiguration) : this(
        db,
        JBakeConfigurationFactory().createDefaultJbakeConfiguration(templatesPath.getParentFile(), config)
    ) {
        val configuration = (this.config as DefaultJBakeConfiguration)
        configuration.destinationFolder = destination
        configuration.templateFolder = templatesPath
    }

    // TODO: Should all content be made available to all templates via this class?
    /**
     * @param templatesPath   The templates folder
     * @param renderingEngine The instance of DelegatingTemplateEngine to use
     */
    @Deprecated("""Use {@link #Renderer(ContentStore, JBakeConfiguration, DelegatingTemplateEngine)} instead.
      Creates a new instance of Renderer with supplied references to folders and the instance of DelegatingTemplateEngine to use.""")
    constructor(
        db: ContentStore,
        destination: File,
        templatesPath: File,
        config: CompositeConfiguration,
        renderingEngine: DelegatingTemplateEngine
    ) : this(
        db,
        JBakeConfigurationFactory().createDefaultJbakeConfiguration(templatesPath.getParentFile(), config),
        renderingEngine
    ) {
        val configuration = (this.config as DefaultJBakeConfiguration)
        configuration.destinationFolder = destination
        configuration.templateFolder = templatesPath
    }

    /**
     * Creates a new instance of Renderer with supplied references to folders.
     *
     */
    constructor(db: ContentStore, config: JBakeConfiguration) {
        this.config = config
        this.renderingEngine = DelegatingTemplateEngine(db, config)
        this.db = db
    }

    /**
     * Creates a new instance of Renderer with supplied references to folders and the instance of DelegatingTemplateEngine to use.
     *
     * @param renderingEngine The instance of DelegatingTemplateEngine to use
     */
    constructor(db: ContentStore, config: JBakeConfiguration, renderingEngine: DelegatingTemplateEngine) {
        this.config = config
        this.renderingEngine = renderingEngine
        this.db = db
    }

    private fun findTemplateName(docType: String): String {
        return config.getTemplateByDocType(docType)!!
    }

    /**
     * Render using the new type-safe RenderContext.
     * This is the preferred method for new code.
     *
     * @param context The rendering context
     * @param outputFile The output file
     * @param templateName The template to use
     */
    fun renderWithContext(context: RenderContext, outputFile: File, templateName: String) {
        try {
            createWriter(outputFile).use { out ->
                // For now, convert to legacy model for compatibility
                val legacyModel = TemplateModel.fromContext(context)
                renderingEngine.renderDocument(legacyModel, templateName, out)
            }
            log.info("Rendering [{}]... done!", outputFile)
        } catch (e: Exception) {
            log.error("Rendering [{}]... failed!", outputFile, e)
            throw Exception("Failed to render file " + outputFile.absolutePath + ". Cause: " + e.message, e)
        }
    }

    /**
     * Render the supplied content to a file.
     *
     * @param content The content to renderDocument
     * @throws Exception if IOException or SecurityException are raised
     */
    fun render(content: DocumentModel) {
        val docType = content.type
        var outputFilename = (config.destinationFolder.path + File.separatorChar) + content.uri
        if (outputFilename.lastIndexOf('.') > outputFilename.lastIndexOf(File.separatorChar)) {
            outputFilename = outputFilename.take(outputFilename.lastIndexOf('.'))
        }

        // delete existing versions if they exist in case status has changed either way
        val outputExtension = config.getOutputExtensionByDocType(docType)
        val draftFile = File(outputFilename, config.draftSuffix + outputExtension)
        if (draftFile.exists()) {
            Files.delete(draftFile.toPath())
        }

        val publishedFile = File(outputFilename + outputExtension)
        if (publishedFile.exists()) {
            Files.delete(publishedFile.toPath())
        }

        if (content.status == ModelAttributes.Status.DRAFT) {
            outputFilename += config.draftSuffix
        }

        val outputFile = File(outputFilename + outputExtension)
        val model = TemplateModel().apply {
            this.content = content
            this.renderer = renderingEngine
        }

        try {
            createWriter(outputFile).use { out ->
                renderingEngine.renderDocument(model, findTemplateName(docType), out)
            }
            log.info("Rendering [{}]... done!", outputFile)
        } catch (e: Exception) {
            log.error("Rendering [{}]... failed!", outputFile, e)
            throw Exception("Failed to render file " + outputFile.absolutePath + ". Cause: " + e.message, e)
        }
    }

    @Throws(IOException::class)
    private fun createWriter(file: File): Writer {
        if (!file.exists()) {
            file.getParentFile().mkdirs()
            file.createNewFile()
        }

        return OutputStreamWriter(FileOutputStream(file), (config.renderEncoding ?: "UTF-8"))
    }

    private fun render(renderConfig: RenderingConfig) {
        val outputFile = renderConfig.path
        try {
            createWriter(outputFile).use { out ->
                renderingEngine.renderDocument(
                    renderConfig.model,
                    renderConfig.template, out
                )
            }
            log.info("Rendering {} [{}]... done!", renderConfig.name, outputFile)
        } catch (e: Exception) {
            log.error("Rendering {} [{}]... failed!", renderConfig.name, outputFile, e)
            throw Exception("Failed to render " + renderConfig.name, e)
        }
    }

    /**
     * Render an index file using the supplied content.
     *
     * @param indexFile The name of the output file
     * @throws Exception if IOException or SecurityException are raised
     */
    fun renderIndex(indexFile: String) {
        render(DefaultRenderingConfig(indexFile, MASTERINDEX_TEMPLATE_NAME))
    }

    fun renderIndexPaging(indexFile: String) {
        val totalPosts = db.getPublishedCount("post")
        val postsPerPage = config.postsPerPage

        if (totalPosts == 0L) {
            //paging makes no sense. render single index file instead
            renderIndex(indexFile)
            return
        }

        val pagingHelper = PagingHelper(totalPosts, postsPerPage)
        val model = TemplateModel().apply {
            renderer = renderingEngine
            numberOfPages = pagingHelper.numberOfPages
        }

        try {
            db.paginationLimit = postsPerPage
            var pageStart = 0
            var page = 1
            while (pageStart < totalPosts) {
                var fileName = indexFile

                db.paginationOffset = pageStart
                model.currentPageNumber = page
                model.previousFilename = pagingHelper.getPreviousFileName(page)
                model.nextFileName = pagingHelper.getNextFileName(page)

                val contentModel = buildSimpleModel(MASTERINDEX_TEMPLATE_NAME)

                if (page > 1)
                    contentModel.rootPath = "../"
                model.content = contentModel

                // Add page number to file name
                fileName = pagingHelper.getCurrentFileName(page, fileName)
                val renderConfig = ModelRenderingConfig(fileName, model, MASTERINDEX_TEMPLATE_NAME)
                render(renderConfig)
                pageStart += postsPerPage
                page++
            }
            db.resetPagination()
        } catch (e: Exception) {
            throw Exception("Failed to render index. Cause: " + e.message, e)
        }
    }
    /**
     * Render an XML sitemap file using the supplied content.
     *
     * @param sitemapFile configuration for site map
     * @throws Exception if can't create correct default rendering config
     * @see [About Sitemaps](https://support.google.com/webmasters/answer/156184?hl=en&ref_topic=8476)
     *
     * @see [Sitemap protocol](http://www.sitemaps.org/)
     */
    fun renderSitemap(sitemapFile: String) {
        render(DefaultRenderingConfig(sitemapFile, SITEMAP_TEMPLATE_NAME))
    }

    /**
     * Render an XML feed file using the supplied content.
     *
     * @param feedFile The name of the output file
     * @throws Exception if default rendering configuration is not loaded correctly
     */
    fun renderFeed(feedFile: String) {
        render(DefaultRenderingConfig(feedFile, FEED_TEMPLATE_NAME))
    }

    /**
     * Render an archive file using the supplied content.
     *
     * @param archiveFile The name of the output file
     * @throws Exception if default rendering configuration is not loaded correctly
     */
    fun renderArchive(archiveFile: String) {
        render(DefaultRenderingConfig(archiveFile, ARCHIVE_TEMPLATE_NAME))
    }

    /**
     * Render an 404 file using the predefined template.
     *
     * @param errorFile      The name of the output file
     * @throws Exception    if default rendering configuration is not loaded correctly
     */
    fun renderError404(errorFile: String) {
        render(DefaultRenderingConfig(errorFile, ERROR404_TEMPLATE_NAME))
    }

    /**
     * Render tag files using the supplied content.
     *
     * @param tagPath The output path
     * @return Number of rendered tags
     * @throws Exception if cannot render tags correctly
     */
    fun renderTags(tagPath: String): Int {
        var renderedCount = 0
        val errors: MutableList<Throwable> = LinkedList<Throwable>()

        for (tag in db.allTags) {
            try {
                val ext = config.outputExtension ?: ""
                val path = File(config.destinationFolder, tagPath + fS + tag + ext)
                val map = buildSimpleModel(ModelAttributes.TAG).apply {
                    rootPath = FileUtil.getUriPathToDestinationRoot(config, path)
                }
                val model = TemplateModel().apply {
                    renderer = renderingEngine
                    this.tag = tag
                    content = map
                    // Provide configuration to template model so typed extractors can access it
                    this.config = this@Renderer.config.asHashMap()
                    this.put("jbake_config", config)
                }

                render(
                    ModelRenderingConfig(
                        path,
                        ModelAttributes.TAG,
                        model,
                        findTemplateName(ModelAttributes.TAG)
                    )
                )

                renderedCount++
            } catch (e: Exception) {
                errors.add(e)
            }
        }

        if (config.renderTagsIndex) {
            try {
                // Add an index file at root folder of tags.
                // This will prevent directory listing and also provide an option to
                // display all tags page.
                val ext = config.outputExtension ?: ""
                val path = File(config.destinationFolder, tagPath + fS + "index" + ext)
                val map = buildSimpleModel(ModelAttributes.TAGS).apply {
                    rootPath = FileUtil.getUriPathToDestinationRoot(config, path)
                }
                val model = TemplateModel().apply {
                    renderer = renderingEngine
                    content = map
                    // Provide configuration to template model so typed extractors can access it
                    this.config = this@Renderer.config.asHashMap()
                    this.put("jbake_config", config)
                }

                render(ModelRenderingConfig(path, "tagindex", model, findTemplateName("tagsindex")))
                renderedCount++
            } catch (e: Exception) {
                errors.add(e)
            }
        }

        if (errors.isNotEmpty()) {
            val message = buildString {
                append("Failed to render tags. Cause(s):")
                for (error in errors) {
                    append("\n").append(error.message)
                }
            }
            throw Exception(message, errors[0])
        }
        return renderedCount
    }

    /**
     * Builds simple map of values, which are exposed when rendering index/archive/sitemap/feed/tags.
     *
     * @param type
     * @return a basic [DocumentModel]
     */
    private fun buildSimpleModel(type: String): DocumentModel {
        val content = DocumentModel()
        content.type = type
        content.rootPath = ""
        // add any more keys here that need to have a default value to prevent need to perform null check in templates
        return content
    }

    private interface RenderingConfig {

        // TODO Possibly nullable.
        val path: File

        val name: String

        val template: String

        val model: TemplateModel
    }

    internal abstract class AbstractRenderingConfig(
        override val path: File,
        override val name: String,
        override val template: String
    ) : RenderingConfig


    private inner class ModelRenderingConfig : AbstractRenderingConfig {
        override val model: TemplateModel

        constructor(fileName: String, model: TemplateModel, templateType: String)
                : super(File(config.destinationFolder, fileName), fileName, findTemplateName(templateType))
        {
            this.model = model
        }

        constructor(path: File, name: String, model: TemplateModel, template: String) : super(path, name, template) {
             this.model = model
         }
    }

    internal inner class DefaultRenderingConfig : AbstractRenderingConfig {
        private val content: DocumentModel

        private constructor(path: File, allInOneName: String)
            : super(path, allInOneName, findTemplateName(allInOneName))
        {
            this.content = buildSimpleModel(allInOneName)
        }

        constructor(filename: String, allInOneName: String) : super(
                File(config.destinationFolder, fS + filename),
                 allInOneName,
            findTemplateName(allInOneName)
             )
        {
          this.content = buildSimpleModel(allInOneName)
        }

        /**
         * Constructor added due to known use of a allInOneName which is used for name, template and content
         */
        constructor(allInOneName: String) : this(
            File(config.destinationFolder.path + fS + allInOneName + (config.outputExtension ?: "")),
            allInOneName
        )

        override val model: TemplateModel
            get() {
                val model = TemplateModel()
                model.renderer = renderingEngine
                model.content = content

                if (config.paginateIndex) {
                    model.numberOfPages = 0
                    model.currentPageNumber = 0
                    model.previousFilename = null
                    model.nextFileName = null
                }

                return model
            }
    }

    companion object {
        private const val MASTERINDEX_TEMPLATE_NAME = "masterindex"
        private const val SITEMAP_TEMPLATE_NAME = "sitemap"
        private const val FEED_TEMPLATE_NAME = "feed"
        private const val ARCHIVE_TEMPLATE_NAME = "archive"
        private const val ERROR404_TEMPLATE_NAME = "error404"
    }

    private val log: Logger = LoggerFactory.getLogger(Renderer::class.java)
}
