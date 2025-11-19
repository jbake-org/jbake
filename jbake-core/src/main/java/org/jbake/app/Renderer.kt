package org.jbake.app

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.jbake.template.DelegatingTemplateEngine
import org.jbake.template.model.TemplateModel
import org.jbake.util.PagingHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.file.Files
import java.util.*

/**
 * Render output to a file.
 *
 * @author Jonathan Bullock [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
 */
class Renderer {
    private val logger: Logger = LoggerFactory.getLogger(Renderer::class.java)
    private val config: JBakeConfiguration
    private val renderingEngine: DelegatingTemplateEngine
    private val db: ContentStore

    /**
     * @param db            The database holding the content
     * @param destination   The destination folder
     * @param templatesPath The templates folder
     * @param config        Project configuration
     */
    @Deprecated(
        """Use {@link #Renderer(ContentStore, JBakeConfiguration)} instead.
      Creates a new instance of Renderer with supplied references to folders."""
    )
    constructor(db: ContentStore, destination: File?, templatesPath: File, config: CompositeConfiguration?) : this(
        db,
        JBakeConfigurationFactory().createDefaultJbakeConfiguration(templatesPath.getParentFile(), config)
    ) {
        val configuration = (this.config as DefaultJBakeConfiguration)
        configuration.setDestinationFolder(destination)
        configuration.setTemplateFolder(templatesPath)
    }

    // TOqDO: should all content be made available to all templates via this class??
    /**
     * @param db              The database holding the content
     * @param destination     The destination folder
     * @param templatesPath   The templates folder
     * @param config          Project configuration
     * @param renderingEngine The instance of DelegatingTemplateEngine to use
     */
    @Deprecated(
        """Use {@link #Renderer(ContentStore, JBakeConfiguration, DelegatingTemplateEngine)} instead.
      Creates a new instance of Renderer with supplied references to folders and the instance of DelegatingTemplateEngine to use."""
    )
    constructor(
        db: ContentStore,
        destination: File?,
        templatesPath: File,
        config: CompositeConfiguration?,
        renderingEngine: DelegatingTemplateEngine
    ) : this(
        db,
        JBakeConfigurationFactory().createDefaultJbakeConfiguration(templatesPath.getParentFile(), config),
        renderingEngine
    ) {
        val configuration = (this.config as DefaultJBakeConfiguration)
        configuration.setDestinationFolder(destination)
        configuration.setTemplateFolder(templatesPath)
    }

    /**
     * Creates a new instance of Renderer with supplied references to folders.
     *
     * @param db     The database holding the content
     * @param config Project configuration
     */
    constructor(db: ContentStore, config: JBakeConfiguration) {
        this.config = config
        this.renderingEngine = DelegatingTemplateEngine(db, config)
        this.db = db
    }

    /**
     * Creates a new instance of Renderer with supplied references to folders and the instance of DelegatingTemplateEngine to use.
     *
     * @param db              The database holding the content
     * @param config          The application specific configuration
     * @param renderingEngine The instance of DelegatingTemplateEngine to use
     */
    constructor(db: ContentStore, config: JBakeConfiguration, renderingEngine: DelegatingTemplateEngine) {
        this.config = config
        this.renderingEngine = renderingEngine
        this.db = db
    }

    private fun findTemplateName(docType: String): String? {
        return config.getTemplateByDocType(docType)
    }

    /**
     * Render the supplied content to a file.
     *
     * @param content The content to renderDocument
     * @throws Exception if IOException or SecurityException are raised
     */
    @Throws(Exception::class)
    fun render(content: DocumentModel) {
        val docType = content.type
        var outputFilename = (config.destinationFolder!!.getPath() + File.separatorChar).toString() + content.getUri()
        if (outputFilename.lastIndexOf('.') > outputFilename.lastIndexOf(File.separatorChar)) {
            outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf('.'))
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

        if (content.getStatus() == ModelAttributes.Status.DRAFT) {
            outputFilename = outputFilename + config.draftSuffix
        }

        val outputFile = File(outputFilename + outputExtension)
        val model = TemplateModel()
        model.setContent(content)
        model.setRenderer(renderingEngine)

        try {
            createWriter(outputFile).use { out ->
                renderingEngine.renderDocument(model, findTemplateName(docType), out)
            }
            logger.info("Rendering [{}]... done!", outputFile)
        } catch (e: Exception) {
            logger.error("Rendering [{}]... failed!", outputFile, e)
            throw Exception("Failed to render file " + outputFile.getAbsolutePath() + ". Cause: " + e.message, e)
        }
    }

    @Throws(IOException::class)
    private fun createWriter(file: File): Writer {
        if (!file.exists()) {
            file.getParentFile().mkdirs()
            file.createNewFile()
        }

        return OutputStreamWriter(FileOutputStream(file), config.renderEncoding)
    }

    @Throws(Exception::class)
    private fun render(renderConfig: RenderingConfig) {
        val outputFile = renderConfig.path
        try {
            createWriter(outputFile).use { out ->
                renderingEngine.renderDocument(
                    renderConfig.model,
                    renderConfig.template, out
                )
            }
            logger.info("Rendering {} [{}]... done!", renderConfig.name, outputFile)
        } catch (e: Exception) {
            logger.error("Rendering {} [{}]... failed!", renderConfig.name, outputFile, e)
            throw Exception("Failed to render " + renderConfig.name, e)
        }
    }

    /**
     * Render an index file using the supplied content.
     *
     * @param indexFile The name of the output file
     * @throws Exception if IOException or SecurityException are raised
     */
    @Throws(Exception::class)
    fun renderIndex(indexFile: String?) {
        render(DefaultRenderingConfig(indexFile, MASTERINDEX_TEMPLATE_NAME))
    }

    @Throws(Exception::class)
    fun renderIndexPaging(indexFile: String?) {
        val totalPosts = db.getPublishedCount("post")
        val postsPerPage = config.postsPerPage

        if (totalPosts == 0L) {
            //paging makes no sense. render single index file instead
            renderIndex(indexFile)
        } else {
            val pagingHelper = PagingHelper(totalPosts, postsPerPage)

            val model = TemplateModel()
            model.setRenderer(renderingEngine)
            model.setNumberOfPages(pagingHelper.getNumberOfPages())

            try {
                db.setLimit(postsPerPage)
                var pageStart = 0
                var page = 1
                while (pageStart < totalPosts) {
                    var fileName = indexFile

                    db.setStart(pageStart)
                    model.setCurrentPageNuber(page)
                    val previous = pagingHelper.getPreviousFileName(page)
                    model.setPreviousFilename(previous)
                    val nextFileName = pagingHelper.getNextFileName(page)
                    model.setNextFileName(nextFileName)

                    val contentModel = buildSimpleModel(MASTERINDEX_TEMPLATE_NAME)

                    if (page > 1) {
                        contentModel.setRootPath("../")
                    }
                    model.setContent(contentModel)

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
    @Throws(Exception::class)
    fun renderSitemap(sitemapFile: String?) {
        render(DefaultRenderingConfig(sitemapFile, SITEMAP_TEMPLATE_NAME))
    }

    /**
     * Render an XML feed file using the supplied content.
     *
     * @param feedFile The name of the output file
     * @throws Exception if default rendering configuration is not loaded correctly
     */
    @Throws(Exception::class)
    fun renderFeed(feedFile: String?) {
        render(DefaultRenderingConfig(feedFile, FEED_TEMPLATE_NAME))
    }

    /**
     * Render an archive file using the supplied content.
     *
     * @param archiveFile The name of the output file
     * @throws Exception if default rendering configuration is not loaded correctly
     */
    @Throws(Exception::class)
    fun renderArchive(archiveFile: String?) {
        render(DefaultRenderingConfig(archiveFile, ARCHIVE_TEMPLATE_NAME))
    }

    /**
     * Render an 404 file using the predefined template.
     *
     * @param errorFile      The name of the output file
     * @throws Exception    if default rendering configuration is not loaded correctly
     */
    @Throws(Exception::class)
    fun renderError404(errorFile: String?) {
        render(DefaultRenderingConfig(errorFile, ERROR404_TEMPLATE_NAME))
    }

    /**
     * Render tag files using the supplied content.
     *
     * @param tagPath The output path
     * @return Number of rendered tags
     * @throws Exception if cannot render tags correctly
     */
    @Throws(Exception::class)
    fun renderTags(tagPath: String?): Int {
        var renderedCount = 0
        val errors: MutableList<Throwable> = LinkedList<Throwable>()

        for (tag in db.getAllTags()) {
            try {
                val model = TemplateModel()
                model.setRenderer(renderingEngine)
                model.setTag(tag)
                val map = buildSimpleModel(ModelAttributes.TAG.toString())
                val path =
                    File(config.destinationFolder + File.separator + tagPath + File.separator + tag + config.outputExtension)

                map.setRootPath(FileUtil.getUriPathToDestinationRoot(config, path))
                model.setContent(map)

                render(
                    ModelRenderingConfig(
                        path,
                        ModelAttributes.TAG.toString(),
                        model,
                        findTemplateName(ModelAttributes.TAG.toString())
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
                val model = TemplateModel()
                model.setRenderer(renderingEngine)
                val map = buildSimpleModel(ModelAttributes.TAGS.toString())
                val path =
                    File(config.destinationFolder + File.separator + tagPath + File.separator + "index" + config.outputExtension)

                map.setRootPath(FileUtil.getUriPathToDestinationRoot(config, path))
                model.setContent(map)


                render(ModelRenderingConfig(path, "tagindex", model, findTemplateName("tagsindex")))
                renderedCount++
            } catch (e: Exception) {
                errors.add(e)
            }
        }

        if (!errors.isEmpty()) {
            val sb = StringBuilder()
            sb.append("Failed to render tags. Cause(s):")
            for (error in errors) {
                sb.append("\n").append(error.message)
            }
            throw Exception(sb.toString(), errors.get(0))
        } else {
            return renderedCount
        }
    }

    /**
     * Builds simple map of values, which are exposed when rendering index/archive/sitemap/feed/tags.
     *
     * @param type
     * @return a basic [DocumentModel]
     */
    private fun buildSimpleModel(type: String?): DocumentModel {
        val content = DocumentModel()
        content.type = type
        content.rootPath = ""
        // add any more keys here that need to have a default value to prevent need to perform null check in templates
        return content
    }

    private interface RenderingConfig {
        val path: File?

        val name: String?

        val template: String?

        val model: TemplateModel?
    }

    private abstract class AbstractRenderingConfig(
        override val path: File?,
        override val name: String?,
        override val template: String?
    ) : RenderingConfig

    inner class ModelRenderingConfig : AbstractRenderingConfig {
        override val model: TemplateModel?

        constructor(
            fileName: String?,
            model: TemplateModel?,
            templateType: String?
        ) : super(File(config.destinationFolder, fileName), fileName, findTemplateName(templateType)) {
            this.model = model
        }

        constructor(path: File?, name: String?, model: TemplateModel?, template: String?) : super(
            path,
            name,
            template
        ) {
            this.model = model
        }
    }

    internal inner class DefaultRenderingConfig : AbstractRenderingConfig {
        private val content: DocumentModel

        private constructor(path: File?, allInOneName: String?) : super(
            path,
            allInOneName,
            findTemplateName(allInOneName)
        ) {
            this.content = buildSimpleModel(allInOneName)
        }

        constructor(filename: String?, allInOneName: String?) : super(
            File(
                config.destinationFolder,
                File.separator + filename
            ), allInOneName, findTemplateName(allInOneName)
        ) {
            this.content = buildSimpleModel(allInOneName)
        }

        /**
         * Constructor added due to known use of a allInOneName which is used for name, template and content
         *
         * @param allInOneName
         */
        constructor(allInOneName: String?) : this(
            File(config.destinationFolder!!.getPath() + File.separator + allInOneName + config.outputExtension),
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
                    model.previousFilename = ""
                    model.nextFileName = ""
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
}
