package org.jbake.app

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.jbake.template.DelegatingTemplateEngine
import org.jbake.template.model.JbakeTemplateModel
import org.jbake.template.model.RenderContext
import org.jbake.util.Logging.logger
import org.jbake.util.PagingHelper
import org.jbake.util.ValueTracer
import org.slf4j.Logger
import java.io.*
import java.nio.file.Files
import java.util.*

/**
 * Render output to a file.
 */
class Renderer {
    internal val config: JBakeConfiguration
    private val renderingEngine: DelegatingTemplateEngine
    private val db: ContentStore

    /**
     * Creates a new instance of Renderer with supplied configuration and the instance of DelegatingTemplateEngine to use.
     */
    constructor(db: ContentStore, config: JBakeConfiguration, renderingEngine: DelegatingTemplateEngine = DelegatingTemplateEngine(db, config)) {
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
     */
    fun renderWithContext(context: RenderContext, outputFile: File, templateName: String) {
        try {
            createWriter(outputFile).use { out ->
                // For now, convert to legacy model for compatibility
                val legacyModel = JbakeTemplateModel.fromContext(context)
                renderingEngine.renderDocument(legacyModel, templateName, out)
            }
            log.info("Rendering done: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            log.error("Rendering failed: ${outputFile.absolutePath} - ${e.message}", e)
            throw Exception("Failed to render file ${outputFile.absolutePath} - ${e.message}", e)
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
        val contentUri = content.uri
        var outputFile = config.destinationDir.resolve(contentUri)

        // Not all URIs have extensions. Only trim extension if it exists
        if (outputFile.extension.isNotEmpty()) {
            outputFile = outputFile.resolveSibling(outputFile.nameWithoutExtension)
        }

        // delete existing versions if they exist in case status has changed either way
        val outputExtension = config.getOutputExtensionByDocType(docType)
        val draftFile = outputFile.resolveSibling(outputFile.name + config.draftSuffix + outputExtension)
        if (draftFile.exists()) {
            Files.delete(draftFile.toPath())
        }

        val publishedFile = outputFile.resolveSibling(outputFile.name + outputExtension)
        if (publishedFile.exists()) {
            Files.delete(publishedFile.toPath())
        }

        val finalOutputFile = if (content.status == ModelAttributes.Status.DRAFT) {
            outputFile.resolveSibling(outputFile.name + config.draftSuffix + outputExtension)
        } else {
            publishedFile
        }

        val model = JbakeTemplateModel().apply {
            this.content = content
            this.renderer = renderingEngine
        }
        ValueTracer.trace("renderer-model", model.content, content.sourceUri)

        try {
            createWriter(finalOutputFile).use { out ->
                renderingEngine.renderDocument(model, findTemplateName(docType), out)
            }
            log.info("Rendered: $finalOutputFile")
        } catch (e: Exception) {
            log.error("Rendering failed: $finalOutputFile : ${e.message}", e)
            throw Exception("Failed to render file ${finalOutputFile.absolutePath}: ${e.message}", e)
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
            log.info("Rendering: ${renderConfig.path} -> $outputFile")
            createWriter(outputFile).use { out ->
                renderingEngine.renderDocument(renderConfig.model, renderConfig.template, out)
            }
            log.info("Rendered: ${renderConfig.name} $outputFile")
        }
        catch (e: Exception) {
            val message = "Failed to render renderConfig '${renderConfig.name}' into $outputFile: ${e.javaClass.simpleName} ${e.message}"
            log.error("$message Rethrowing", e)
            throw Exception(message, e)
        }
    }

    /**
     * Render an index file using the supplied content.
     */
    fun renderIndex(outputIndexFile: String) {
        render(DefaultRenderingConfig(outputIndexFile, MASTERINDEX_TEMPLATE_NAME))
    }

    fun renderIndex() {
        renderIndex(config.indexFileName ?: "index.html")
    }


    fun renderIndexPaging() {
        renderIndexPaging(config.indexFileName ?: "index.html")
    }

    fun renderIndexPaging(indexFile: String) {
        val totalPosts = db.getPublishedCount("post")
        val postsPerPage = config.postsPerPage
        log.debug("Rendering index $indexFile for $totalPosts posts paged by $postsPerPage.")

        if (totalPosts == 0L) { renderIndex(indexFile); return }

        val pagingHelper = PagingHelper(totalPosts, postsPerPage)

        val model = JbakeTemplateModel().apply {
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

                val contentModel = buildEmptyModelWithType(MASTERINDEX_TEMPLATE_NAME)

                if (page > 1)
                    contentModel.rootPath = "../"
                model.content = contentModel

                // Add page number to file name
                fileName = pagingHelper.getCurrentFileName(page, fileName)
                val renderConfig = ModelRenderingConfig(fileName, model, MASTERINDEX_TEMPLATE_NAME)
                ValueTracer.trace("renderer-index-page", model.content, "page-$page")

                render(renderConfig)
                pageStart += postsPerPage
                page++
            }
            db.resetPagination()
        } catch (e: Exception) {
            throw Exception("Failed to render index. Cause: " + e.message, e)
        }
    }
    fun renderSitemap() {
        renderSitemap(config.siteMapFileName ?: "sitemap.xml")
    }

    /**
     * Render an XML sitemap file using the supplied content.
     *
     * @throws Exception if can't create correct default rendering config
     * @see [About Sitemaps](https://support.google.com/webmasters/answer/156184?hl=en&ref_topic=8476)
     * @see [Sitemap protocol](http://www.sitemaps.org/)
     * @param sitemapFile configuration for site map
     */
    fun renderSitemap(sitemapFile: String) {
        render(DefaultRenderingConfig(sitemapFile, SITEMAP_TEMPLATE_NAME))
    }

    /**
     * Render an XML feed file using the supplied content.
     *
     * @throws Exception if default rendering configuration is not loaded correctly
     */
    fun renderFeed() {
        renderFeed(config.feedFileName) //  ?: "feed.xml"
    }

    fun renderFeed(outputFeedFile: String) {
        render(DefaultRenderingConfig(outputFeedFile, FEED_TEMPLATE_NAME))
    }

    /**
     * Render an archive file using the supplied content.
     *
     * @throws Exception if default rendering configuration is not loaded correctly
     */
    fun renderArchive() {
        renderArchive(config.archiveFileName!!)
    }

    fun renderArchive(outputArchiveFile: String) {
        render(DefaultRenderingConfig(outputArchiveFile, ARCHIVE_TEMPLATE_NAME))
    }

    /**
     * Render an 404 file using the predefined template.
     * This version uses the file name from the configuration.
     *
     * @throws Exception If default rendering configuration is not loaded correctly.
     */
    fun renderError404() {
        renderError404(config.error404FileName!!)
    }

    /**
     * Render an 404 file using the predefined template to a specific file.
     *
     * @param outputFile The file to render to.
     * @throws Exception If default rendering configuration is not loaded correctly.
     */
    fun renderError404(outputFile: String) {
        render(DefaultRenderingConfig(outputFile, ERROR404_TEMPLATE_NAME))
    }

    /**
     * Render tag files using the supplied content.
     *
     * @return Number of rendered tags
     */
    fun renderTags(): Int {
        return renderTags(config.tagPathName ?: "tags")
    }

    fun renderTags(outputTagFile: String): Int {
        var renderedCount = 0
        val errors: MutableList<Throwable> = LinkedList<Throwable>()

        for (tag in db.allTags) {
            try {
                val ext = config.outputExtension ?: ""
                val path = config.destinationDir.resolve(outputTagFile).resolve(tag + ext)
                val map = buildEmptyModelWithType(ModelAttributes.TAGS_CURRENT_TAG).apply {
                    rootPath = FileUtil.getUriPathToDestinationRoot(config, path)
                }
                val model = JbakeTemplateModel().apply {
                    renderer = renderingEngine
                    this.tag = tag
                    content = map
                    // Provide configuration to template model so typed extractors can access it
                    this.config = this@Renderer.config.asHashMap()
                    this.put("jbake_config", config)
                }

                val renderConfig = ModelRenderingConfig(path, ModelAttributes.TAGS_CURRENT_TAG, model, findTemplateName(ModelAttributes.TAGS_CURRENT_TAG))
                ValueTracer.trace("renderer-tag", model.content, tag)
                render(renderConfig)

                renderedCount++
            }
            catch (e: Exception) { errors.add(e) }
        }

        if (config.renderTagsIndex) {
            try {
                // Add an index file at root directory of tags. This will prevent directory listing and also provide an option to display all tags page.
                val ext = config.outputExtension ?: ""
                val path = config.destinationDir.resolve(outputTagFile).resolve("index$ext")
                val documentModel = buildEmptyModelWithType(ModelAttributes.DOC_TAGS).apply {
                    rootPath = FileUtil.getUriPathToDestinationRoot(config, path)
                }
                val model = JbakeTemplateModel().apply {
                    renderer = renderingEngine
                    content = documentModel
                    // Provide configuration to template model so typed extractors can access it
                    this.config = this@Renderer.config.asHashMap()
                    this.put("jbake_config", config)
                }

                ValueTracer.trace("renderer-tag-index", model.content, "tags-index")
                render(ModelRenderingConfig(path, "tagindex", model, findTemplateName("tagsindex")))
                renderedCount++
            }
            catch (e: Exception) { errors.add(e) }
        }

        if (errors.isNotEmpty()) {
            val message = "Failed to render tags. Cause(s):" + errors.joinToString { "\n" + it.message }
            throw Exception(message, errors[0])
        }
        return renderedCount
    }

    /** Builds simple map of values, which are exposed when rendering index/archive/sitemap/feed/tags. */
    private fun buildEmptyModelWithType(type: String): DocumentModel
        = DocumentModel().apply {
            this.type = type
            this.rootPath = ""
            // Add any more keys here that need to have a default value to prevent need to perform null check in templates.
        }

    private interface RenderingConfig {

        // TODO Possibly nullable.
        val path: File
        val name: String
        val template: String
        val model: JbakeTemplateModel
    }

    internal abstract class AbstractRenderingConfig(
        override val path: File,
        override val name: String,
        override val template: String
    ) : RenderingConfig


    private inner class ModelRenderingConfig : AbstractRenderingConfig {
        override val model: JbakeTemplateModel

        // Used only for renderIndexPaging().
        constructor(fileName: String, model: JbakeTemplateModel, templateType: String)
                : super(config.destinationDir.resolve(fileName), fileName, findTemplateName(templateType))
        {
            this.model = model
        }

        // Used only for renderTags().
        constructor(path: File, name: String, model: JbakeTemplateModel, template: String)
                : super(path, name, template)
        {
            this.model = model
        }
    }

    internal inner class DefaultRenderingConfig : AbstractRenderingConfig {
        private val content: DocumentModel

        private constructor(path: File, allInOneName: String)
            : super(path, allInOneName, findTemplateName(allInOneName))
        {
            this.content = buildEmptyModelWithType(allInOneName)
        }

        constructor(outputFile: String, allInOneName: String) : super(
            path = config.destinationDir.resolve(outputFile),
            name = allInOneName,
            template = findTemplateName(allInOneName)
        ){
            this.content = buildEmptyModelWithType(allInOneName)
        }

        /**
         * Constructor added due to known use of a allInOneName which is used for name, template and content
         */
        constructor(allInOneName: String) : this(
            config.destinationDir.resolve(allInOneName + (config.outputExtension ?: "")),
            allInOneName
        )

        override val model: JbakeTemplateModel
            get() {
                val model = JbakeTemplateModel()
                model.renderer = renderingEngine
                model.content = content

                if (config.paginateIndex) {
                    model.numberOfPages = 0
                    model.currentPageNumber = 0
                    model.previousFilename = null
                    model.nextFileName = null
                }

                ValueTracer.trace("renderer-default-config", model.content, name)
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

    private val log: Logger by logger()
}
