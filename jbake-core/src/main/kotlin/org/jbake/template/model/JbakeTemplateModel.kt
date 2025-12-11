package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.model.BaseModel
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.jbake.template.DelegatingTemplateEngine
import org.jbake.util.ValueTracer
import java.io.Writer

open class JbakeTemplateModel : BaseModel {
    constructor()

    constructor(model: Map<String, Any>) {
        putAll(model)
    }

    @Suppress("UNCHECKED_CAST")
    var config: MutableMap<String, Any>
        get() = get(ModelAttributes.TMPL_JBAKE_CONFIG) as MutableMap<String, Any>
        set(configModel) { put(ModelAttributes.TMPL_JBAKE_CONFIG, configModel) }

    var content: DocumentModel
        get() = get(ModelAttributes.TMPL_CONTENT_MODEL) as DocumentModel
        set(content) { put(ModelAttributes.TMPL_CONTENT_MODEL, content) }

    var renderer: DelegatingTemplateEngine?
        get() = get(ModelAttributes.TMPL_ENGINE) as DelegatingTemplateEngine?
        set(renderingEngine) { put(ModelAttributes.TMPL_ENGINE, renderingEngine!!) }

    var numberOfPages: Int
        get() = get(ModelAttributes.PAGI_TOTAL_PAGES_COUNT) as Int? ?: 0
        set(numberOfPages) { put(ModelAttributes.PAGI_TOTAL_PAGES_COUNT, numberOfPages) }

    var currentPageNumber: Int
        get() = get(ModelAttributes.PAGI_CUR_PAGE_NUMBER) as Int? ?: 0
        set(currentPageNumber) { put(ModelAttributes.PAGI_CUR_PAGE_NUMBER, currentPageNumber) }

    /**
     * Previous page filename for pagination. Null when on the first page (no previous page exists).
     */
    var previousFilename: String?
        get() = get(ModelAttributes.PAGI_PREV_FILENAME) as String?
        set(previousFilename) {
            if (previousFilename != null)
                put(ModelAttributes.PAGI_PREV_FILENAME, previousFilename)
            else
                remove(ModelAttributes.PAGI_PREV_FILENAME)
        }

    /**
     * Next page filename for pagination. Null when on the last page (no next page exists).
     */
    var nextFileName: String?
        get() = get(ModelAttributes.PAGI_NEXT_FILENAME) as String?
        set(nextFileName) {
            if (nextFileName != null)
                put(ModelAttributes.PAGI_NEXT_FILENAME, nextFileName)
            else
                remove(ModelAttributes.PAGI_NEXT_FILENAME)
        }

    var tag: String?
        get() = get(ModelAttributes.TAGS_CURRENT_TAG) as String?
        set(tag) { put(ModelAttributes.TAGS_CURRENT_TAG, tag!!) }

    var taggedPosts: DocumentList<*>
        get() = get(ModelAttributes.TAGS_POSTS_TAGGED_CUR) as DocumentList<*>
        set(taggedPosts) { put(ModelAttributes.TAGS_POSTS_TAGGED_CUR, taggedPosts) }

    var taggedDocuments: DocumentList<*>
        get() = get(ModelAttributes.TAGS_DOCS_TAGGED_CUR) as DocumentList<*>
        set(taggedDocuments) { put(ModelAttributes.TAGS_DOCS_TAGGED_CUR, taggedDocuments) }

    var version: String?
        get() = get(ModelAttributes.GLOB_JBAKE_VERSION) as String?
        set(version) { put(ModelAttributes.GLOB_JBAKE_VERSION, version!!) }

    val writer: Writer?
        get() = get(ModelAttributes.TMPL_OUT_WRITER) as Writer?

    companion object {

        fun fromMap(map: Map<*, *>): JbakeTemplateModel {
            val model = JbakeTemplateModel()
            val violations = mutableListOf<String>()
            for ((k, v) in map) {
                if (v == null) continue
                if (k !is String) violations += "${k?.javaClass?.name ?: "null"}: '${k}'"
                else model.put(k, v)
            }
            if (violations.isEmpty()) return model
            throw IllegalArgumentException("TemplateModel: all keys must be Strings; found:\n" + violations.joinToString("\n") { "  * $it" })
        }


        /** Create TemplateModel from a type-safe RenderContext. This is the new preferred way to create template models. */
        @Suppress("DEPRECATION")
        /// Was: : TemplateModel().apply { putAll(context.toLegacyMap()) }
        fun fromContext(context: RenderContext): JbakeTemplateModel {
            val model = JbakeTemplateModel()
            model.renderer = context.renderer
            context.content?.let { model.content = it }
            model.config = context.config.asHashMap()
            model.put("jbake_config", context.config)
            ValueTracer.trace("template-model-from-context", model.content)
            return model
        }

        /**
         * Convert TemplateModel to RenderContext.
         * This allows gradual migration to the new architecture.
         */
        fun toContext(model: JbakeTemplateModel, db: org.jbake.app.ContentStore): RenderContext {
            val config = model.config
            val configObj = if (config.containsKey("config") && config["config"] is org.jbake.app.configuration.JBakeConfiguration) {
                config["config"] as org.jbake.app.configuration.JBakeConfiguration
            }
            else throw IllegalStateException("Configuration not found in template model") // Shouldn't happen but handle gracefully.

            val content = if (!model.containsKey(ModelAttributes.TMPL_CONTENT_MODEL)) null else model.content

            val renderer = model.renderer

            val pagination = if (!model.containsKey(ModelAttributes.PAGI_TOTAL_PAGES_COUNT)) null
                else PaginationContext(
                    currentPage = model.currentPageNumber,
                    totalPages = model.numberOfPages,
                    previousFilename = model.previousFilename,
                    nextFilename = model.nextFileName
                )

            val tag = if (!model.containsKey(ModelAttributes.TAGS_CURRENT_TAG)) null
                else TagContext(
                    name = model.tag ?: "",
                    taggedPosts = model.taggedPosts,
                    taggedDocuments = model.taggedDocuments
                )

            return RenderContext(configObj, db, content, renderer, pagination, tag, model.version)
        }
    }
}
