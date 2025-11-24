package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.model.BaseModel
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.jbake.template.DelegatingTemplateEngine
import java.io.Writer

open class TemplateModel : BaseModel {
    constructor()

    constructor(model: TemplateModel) {
        putAll(model)
    }

    var config: MutableMap<String, Any>
        get() = get(ModelAttributes.CONFIG) as MutableMap<String, Any>
        set(configModel) {
            put(ModelAttributes.CONFIG, configModel)
        }

    var content: DocumentModel
        get() = get(ModelAttributes.CONTENT) as DocumentModel
        set(content) {
            put(ModelAttributes.CONTENT, content)
        }

    var renderer: DelegatingTemplateEngine?
        get() = get(ModelAttributes.RENDERER) as DelegatingTemplateEngine?
        set(renderingEngine) {
            put(ModelAttributes.RENDERER, renderingEngine!!)
        }

    var numberOfPages: Int
        get() = get(ModelAttributes.NUMBER_OF_PAGES) as Int? ?: 0
        set(numberOfPages) {
            put(ModelAttributes.NUMBER_OF_PAGES, numberOfPages)
        }

    var currentPageNumber: Int
        get() = get(ModelAttributes.CURRENT_PAGE_NUMBERS) as Int? ?: 0
        set(currentPageNumber) {
            put(ModelAttributes.CURRENT_PAGE_NUMBERS, currentPageNumber)
        }

    /**
     * Previous page filename for pagination. Null when on the first page (no previous page exists).
     */
    var previousFilename: String?
        get() = get(ModelAttributes.PREVIOUS_FILENAME) as String?
        set(previousFilename) {
            if (previousFilename != null)
                put(ModelAttributes.PREVIOUS_FILENAME, previousFilename)
            else
                remove(ModelAttributes.PREVIOUS_FILENAME)
        }

    /**
     * Next page filename for pagination. Null when on the last page (no next page exists).
     */
    var nextFileName: String?
        get() = get(ModelAttributes.NEXT_FILENAME) as String?
        set(nextFileName) {
            if (nextFileName != null)
                put(ModelAttributes.NEXT_FILENAME, nextFileName)
            else
                remove(ModelAttributes.NEXT_FILENAME)
        }

    var tag: String?
        get() = get(ModelAttributes.TAG) as String?
        set(tag) {
            put(ModelAttributes.TAG, tag!!)
        }

    var taggedPosts: DocumentList<*>
        get() = get(ModelAttributes.TAGGED_POSTS) as DocumentList<*>
        set(taggedPosts) {
            put(ModelAttributes.TAGGED_POSTS, taggedPosts)
        }

    var taggedDocuments: DocumentList<*>
        get() = get(ModelAttributes.TAGGED_DOCUMENTS) as DocumentList<*>
        set(taggedDocuments) {
            put(ModelAttributes.TAGGED_DOCUMENTS, taggedDocuments)
        }

    var version: String?
        get() = get(ModelAttributes.VERSION) as String?
        set(version) {
            put(ModelAttributes.VERSION, version!!)
        }

    val writer: Writer?
        get() = get(ModelAttributes.OUT) as Writer?

    companion object {

        /** Create TemplateModel from a type-safe RenderContext. This is the new preferred way to create template models. */
        fun fromContext(context: RenderContext) = TemplateModel().apply { putAll(context.toLegacyMap()) }

        /**
         * Convert TemplateModel to RenderContext.
         * This allows gradual migration to the new architecture.
         */
        fun toContext(model: TemplateModel, db: org.jbake.app.ContentStore): RenderContext {
            val config = model.config
            val configObj = if (config.containsKey("config") && config["config"] is org.jbake.app.configuration.JBakeConfiguration) {
                config["config"] as org.jbake.app.configuration.JBakeConfiguration
            } else {
                // Fallback - this shouldn't happen but handle gracefully
                throw IllegalStateException("Configuration not found in template model")
            }

            val content = if (!model.containsKey(ModelAttributes.CONTENT)) null else model.content

            val renderer = model.renderer

            val pagination =
                if (!model.containsKey(ModelAttributes.NUMBER_OF_PAGES)) null
                else PaginationContext(
                    currentPage = model.currentPageNumber,
                    totalPages = model.numberOfPages,
                    previousFilename = model.previousFilename,
                    nextFilename = model.nextFileName
                )

            val tag =
                if (!model.containsKey(ModelAttributes.TAG)) null
                else TagContext(
                    name = model.tag ?: "",
                    taggedPosts = model.taggedPosts,
                    taggedDocuments = model.taggedDocuments
                )

            return RenderContext(configObj, db, content, renderer, pagination, tag, model.version)
        }
    }
}
